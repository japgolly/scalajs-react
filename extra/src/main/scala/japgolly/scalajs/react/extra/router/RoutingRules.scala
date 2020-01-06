package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.CallbackTo
import japgolly.scalajs.react.extra.router.RouterConfig.Parsed

/** A complete set of routing rules that allow the router to handle every all routes without further input.
  *
  * @tparam Page The type of legal pages. Most commonly, a sealed trait that you've created, where all subclasses
  *              represent a page in your SPA.
  */
final case class RoutingRules[Page](parseMulti    : Path => List[StaticOrDynamic[Option[Parsed[Page]]]],
                                    path          : Page => Path,
                                    actionMulti   : (Path, Page) => List[StaticOrDynamic[Option[Action[Page]]]],
                                    fallbackAction: (Path, Page) => Action[Page],
                                    whenNotFound  : Path => CallbackTo[Parsed[Page]]) {

  def parse(path: Path): CallbackTo[Parsed[Page]] =
    unambiguousRule(parseMulti(path))(
      as => s"Multiple (${as.size}) (unconditional) routes specified for path ${path.value}",
      as => s"Multiple (${as.size}) conditional routes active for path ${path.value}"
    ).flatMap {
      case Some(a) => CallbackTo.pure(a)
      case None    => whenNotFound(path)
    }

  def action(path: Path, page: Page): CallbackTo[Action[Page]] =
    unambiguousRule(actionMulti(path, page))(
      as => s"Multiple (${as.size}) (unconditional) actions specified for $page at path ${path.value}",
      as => s"Multiple (${as.size}) conditional actions active for $page at path ${path.value}"
    ).map(_.getOrElse(fallbackAction(path, page)))

  private def unambiguousRule[A](xs       : List[StaticOrDynamic[Option[A]]])
                                (staticErr: List[A] => String,
                                 dynErr   : List[A] => String): CallbackTo[Option[A]] = {
    val (statics, dynamics) = StaticOrDynamic.partition(xs)

    for {
      dynamicOptions <- CallbackTo.sequence(dynamics)
      dynamicOption  <- unambiguousOption(dynamicOptions)(dynErr)
      staticOption   <- unambiguousOption(statics)(staticErr)
    } yield dynamicOption.orElse(staticOption)
  }

  private def unambiguousOption[A](input: List[Option[A]])(errMsg: List[A] => String): CallbackTo[Option[A]] = {
    val as = input.collect { case Some(a) => a }
    as match {
      case Nil      => CallbackTo.pure(None)
      case a :: Nil => CallbackTo.pure(Some(a))
      case as       => CallbackTo.throwException(new RuntimeException(errMsg(as)))
    }
  }
}

object RoutingRules {
  import StaticOrDynamic.Helpers._

  def fromRule[Page](rule          : RoutingRule[Page],
                     fallbackPath  : Page => Path,
                     fallbackAction: (Path, Page) => Action[Page],
                     whenNotFound  : Path => CallbackTo[Parsed[Page]]): RoutingRules[Page] = {

    def prepareParseFn(rule: RoutingRule[Page]): Path => List[StaticOrDynamic[Option[Parsed[Page]]]] =
      rule match {
        case r: RoutingRule.Atom[Page] =>
          p => static(r.parse(p)) :: Nil

        case r: RoutingRule.AutoCorrect[Page] =>
          prepareParseFn(r.underlying)

        case r: RoutingRule.Or[Page] =>
          val x = prepareParseFn(r.lhs)
          val y = prepareParseFn(r.rhs)
          p => y(p).reverse_:::(x(p))

        case r: RoutingRule.Conditional[Page] =>
          val underlying = prepareParseFn(r.underlying)
          p =>
            underlying(p).map(u =>
              dynamic(
                for {
                  c <- r.condition
                  o <- u.merge
                } yield if (c) o else None
              )
            )

        case r: RoutingRule.ConditionalP[Page] =>
          val underlying = prepareParseFn(r.underlying)
          p =>
            underlying(p).map(u =>
              dynamic(
                for {
                  c <- r.condition(p)
                  o <- u.merge
                } yield if (c) o else None
              )
            )
//          parse(_).flatMap {
//            case ok@Some(Right(page)) =>
//              r.condition(page).map {
//                case true => ok
//                case false => None
//              }
//            case other => CallbackTo.pure(other)
//          }

          ???

          }
      }

    def preparePathFn(rule: RoutingRule[Page]): Page => Option[Path] =
      rule match {
        case r: RoutingRule.Atom        [Page] => r.path
        case r: RoutingRule.Conditional [Page] => preparePathFn(r.underlying)
        case r: RoutingRule.ConditionalP[Page] => preparePathFn(r.underlying)
        case r: RoutingRule.AutoCorrect [Page] => preparePathFn(r.underlying)
        case r: RoutingRule.Or          [Page] => preparePathFn(r.lhs) || preparePathFn(r.rhs)
      }

    def prepareActionFn(rule: RoutingRule[Page]): (Path, Page) => List[StaticOrDynamic[Option[Action[Page]]]] =
      rule match {
        case r: RoutingRule.Atom       [Page] => (path, page) => CallbackTo.pure(r.action(path, page))
        case r: RoutingRule.Or         [Page] => prepareActionFn(r.lhs) || prepareActionFn(r.rhs)

        case r: RoutingRule.AutoCorrect[Page] =>
          val path = preparePathFn(r.underlying)
          val action = prepareActionFn(r.underlying)
          (actualPath, page) =>
            path(page) match {
              case Some(expectedPath) =>
                if (expectedPath == actualPath)
                  action(actualPath, page)
                else
                  CallbackTo.pure(Some(RedirectToPath(expectedPath, r.redirectVia)))
              case None =>
                CallbackTo.pure(None)
            }

        case r: RoutingRule.Conditional[Page] =>
          prepareActionFn(RoutingRule.ConditionalP(_ => r.condition, r.underlying, r.otherwise))

        case r: RoutingRule.ConditionalP[Page] =>
          val onOk = prepareActionFn(r.underlying)
          (path, page) =>
            onOk(path, page).flatMap {
              case ok@ Some(_) =>
                r.condition(page) map {
                  case true  => ok
                  case false => r.otherwise(page)
                }
              case None => CallbackTo.pure(None)
            }
      }

    apply(
      parseMulti     = prepareParseFn,
      path           = preparePathFn(rule) | fallbackPath,
      actionMulti    = prepareActionFn,
      fallbackAction = fallbackAction,
      whenNotFound   = whenNotFound)
  }

  /** Create routing rules all at once, with compiler proof that all `Page`s will have a `Path` and `Action`
    * associated.
    *
    * The trade-off here is that care will need to be taken to ensure that path-parsing aligns with paths
    * generated for pages. It is recommended that you call [[RouterConfig.verify]] as a sanity-check.
    */
  def bulk[Page](toPage  : Path => Option[Parsed[Page]],
                 fromPage: Page => (Path, Action[Page]),
                 notFound: Path => Parsed[Page]): RoutingRules[Page] =
    bulkDynamic(
      toPage.andThen(CallbackTo.pure),
      page => {val (p, a) = fromPage(page); (p, CallbackTo.pure(a))},
      notFound)

  /** Create routing rules all at once, with compiler proof that all `Page`s will have a `Path` and `Action`
    * associated.
    *
    * The trade-off here is that care will need to be taken to ensure that path-parsing aligns with paths
    * generated for pages. It is recommended that you call [[RouterConfig.verify]] as a sanity-check.
    */
  def bulkDynamic[Page](toPage  : Path => CallbackTo[Option[Parsed[Page]]],
                        fromPage: Page => (Path, CallbackTo[Action[Page]]),
                        notFound: Path => Parsed[Page]): RoutingRules[Page] =
    new RoutingRules[Page](
      p => toPage(p).map(_.getOrElse(notFound(p))),
      fromPage(_)._1,
      (_, p) => fromPage(p)._2)
}
