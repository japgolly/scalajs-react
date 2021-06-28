package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.extra.router.RouterConfig.Parsed
import japgolly.scalajs.react.util.Effect.Sync

/** A complete set of routing rules that allow the router to handle every all routes without further input.
  *
  * @tparam Page The type of legal pages. Most commonly, a sealed trait that you've created, where all subclasses
  *              represent a page in your SPA.
  */
final case class RoutingRulesF[F[_], Page, Props](
    parseMulti    : Path => List[StaticOrDynamic[Option[Parsed[Page]]]],
    path          : Page => Path,
    actionMulti   : (Path, Page) => List[StaticOrDynamic[Option[ActionF[F, Page, Props]]]],
    fallbackAction: (Path, Page) => ActionF[F, Page, Props],
    whenNotFound  : Path => F[Parsed[Page]])
   (implicit F: Sync[F]) {

  import RoutingRulesF.SharedLogic._

  def parse(path: Path): F[Parsed[Page]] =
    F.flatMap(selectParsed(path, parseMulti)) {
      case Some(a) => F.pure(a)
      case None    => whenNotFound(path)
    }

  def action(path: Path, page: Page): F[ActionF[F, Page, Props]] =
    F.map(selectAction(path, page, actionMulti))(_.getOrElse(fallbackAction(path, page)))
}

// =====================================================================================================================

object RoutingRulesF {
  import StaticOrDynamic.Helpers._

  final class Exception(msg: String) extends RuntimeException(msg)

  private[router] object SharedLogic {

    def selectParsed[F[_], Page](path : Path,
                                 parse: Path => List[StaticOrDynamic[Option[Parsed[Page]]]])
                                (implicit F: Sync[F]): F[Option[Parsed[Page]]] =
      unambiguousRule(parse(path))(
        as => s"Multiple (${as.size}) (unconditional) routes specified for path ${path.value}",
        as => s"Multiple (${as.size}) conditional routes active for path ${path.value}")

    def selectAction[F[_], Page, Props](path  : Path,
                                        page  : Page,
                                        action: (Path, Page) => List[StaticOrDynamic[Option[ActionF[F, Page, Props]]]],
                                       )(implicit F: Sync[F]): F[Option[ActionF[F, Page, Props]]] =
      unambiguousRule(action(path, page))(
        as => s"Multiple (${as.size}) (unconditional) actions specified for $page at path ${path.value}",
        as => s"Multiple (${as.size}) conditional actions active for $page at path ${path.value}")

    private def unambiguousRule[F[_], A](xs       : List[StaticOrDynamic[Option[A]]])
                                        (staticErr: List[A] => String,
                                         dynErr   : List[A] => String)
                                        (implicit F: Sync[F]): F[Option[A]] = {
      val (statics, dynamics) = StaticOrDynamic.partition(xs)(F)

      F.flatMap(F.sequenceList(dynamics)                 )(dynamicOptions =>
      F.flatMap(unambiguousOption(dynamicOptions)(dynErr))(dynamicOption =>
      F.map    (unambiguousOption(statics)(staticErr)    )(staticOption =>
        dynamicOption.orElse(staticOption)
      )))
    }

    private def unambiguousOption[F[_], A](input: List[Option[A]])(errMsg: List[A] => String)
                                          (implicit F: Sync[F]): F[Option[A]] = {
      val as = input.collect { case Some(a) => a }
      as match {
        case Nil      => F.pure(None)
        case a :: Nil => F.pure(Some(a))
        case as       => F.delay(throw new Exception(errMsg(as)))
      }
    }
  }

  def fromRule[F[_], Page, Props](rule          : RoutingRule[Page, Props],
                                  fallbackPath  : Page => Path,
                                  fallbackAction: (Path, Page) => ActionF[F, Page, Props],
                                  whenNotFound  : Path => F[Parsed[Page]])
                                 (implicit F: Sync[F]): RoutingRulesF[F, Page, Props] = {

    implicit def optionTransAction(o: Option[Action[Page, Props]]): Option[ActionF[F, Page, Props]] =
      o.map(_.withEffect[F])

    def prepareParseFn(rule: RoutingRule[Page, Props]): Path => List[StaticOrDynamic[Option[Parsed[Page]]]] =
      rule match {
        case r: RoutingRule.Atom[Page, Props] =>
          p => static(r.parse(p)) :: Nil

        case r: RoutingRule.AutoCorrect[Page, Props] =>
          prepareParseFn(r.underlying)

        case r: RoutingRule.Or[Page, Props] =>
          val x = prepareParseFn(r.lhs)
          val y = prepareParseFn(r.rhs)
          p => y(p).reverse_:::(x(p))

        case r: RoutingRule.Conditional[Page, Props] =>
          // Page condition is checked in prepareActionFn
          prepareParseFn(r.underlying)

        case r: RoutingRule.ConditionalP[Page, Props] =>
          // Page condition is checked in prepareActionFn
          prepareParseFn(r.underlying)
      }

    def preparePathFn(rule: RoutingRule[Page, Props]): Page => Option[Path] =
      rule match {
        case r: RoutingRule.Atom        [Page, Props] => r.path
        case r: RoutingRule.Conditional [Page, Props] => preparePathFn(r.underlying)
        case r: RoutingRule.ConditionalP[Page, Props] => preparePathFn(r.underlying)
        case r: RoutingRule.AutoCorrect [Page, Props] => preparePathFn(r.underlying)
        case r: RoutingRule.Or          [Page, Props] => preparePathFn(r.lhs) || preparePathFn(r.rhs)
      }

    def prepareActionFn(rule: RoutingRule[Page, Props]): (Path, Page) => List[StaticOrDynamic[Option[ActionF[F, Page, Props]]]] =
      rule match {
        case r: RoutingRule.Atom[Page, Props] =>
          (path, page) =>
            val action = optionTransAction(r.action(path, page))
            static(action) :: Nil

        case r: RoutingRule.Or[Page, Props] =>
          val x = prepareActionFn(r.lhs)
          val y = prepareActionFn(r.rhs)
          (path, page) => y(path, page).reverse_:::(x(path, page))

        case r: RoutingRule.AutoCorrect[Page, Props] =>
          val path = preparePathFn(r.underlying)
          val action = prepareActionFn(r.underlying)
          (actualPath, page) =>
            path(page) match {
              case Some(expectedPath) =>
                if (expectedPath == actualPath)
                  action(actualPath, page)
                else
                  static[Option[ActionF[F, Page, Props]]](Some(RedirectToPath(expectedPath, r.redirectVia))) :: Nil
              case None =>
                Nil
            }

        case r: RoutingRule.Conditional[Page, Props] =>
          prepareActionFn(RoutingRule.ConditionalP(_ => r.condition, r.underlying, r.otherwise))

        case r: RoutingRule.ConditionalP[Page, Props] =>
          val underlying = prepareActionFn(r.underlying)
          (path, page) =>
            dynamic[F, Option[ActionF[F, Page, Props]]] {
              val step1 = SharedLogic.selectAction[F, Page, Props](path, page, underlying)
              F.flatMap(step1) {
                case ok@Some(_) =>
                  F.map(F.fromJsFn0(r.condition(page))) {
                    case true  => ok
                    case false => r.otherwise(page)
                  }
                case None =>
                  F.pure(None)
              }
            } :: Nil
      }

    apply(
      parseMulti     = prepareParseFn(rule),
      path           = preparePathFn(rule) | fallbackPath,
      actionMulti    = prepareActionFn(rule),
      fallbackAction = fallbackAction,
      whenNotFound   = whenNotFound)
  }

  /** Create routing rules all at once, with compiler proof that all `Page`s will have a `Path` and `Action`
    * associated.
    *
    * The trade-off here is that care will need to be taken to ensure that path-parsing aligns with paths
    * generated for pages. It is recommended that you call [[RouterConfig.verify]] as a sanity-check.
    */
  def bulk[F[_], Page, Props](toPage  : Path => Option[Parsed[Page]],
                              fromPage: Page => (Path, ActionF[F, Page, Props]),
                              notFound: Path => Parsed[Page])
                             (implicit F: Sync[F]): RoutingRulesF[F, Page, Props] =
    apply[F, Page, Props](
      parseMulti     = p => static(toPage(p)) :: Nil,
      path           = fromPage(_)._1,
      actionMulti    = (_, p) => static(Option(fromPage(p)._2)) :: Nil,
      fallbackAction = (_, _) => RedirectToPath(Path.root, SetRouteVia.HistoryPush), // won't happen
      whenNotFound   = notFound.andThen(F.pure(_)))

  /** Create routing rules all at once, with compiler proof that all `Page`s will have a `Path` and `Action`
    * associated.
    *
    * The trade-off here is that care will need to be taken to ensure that path-parsing aligns with paths
    * generated for pages. It is recommended that you call [[RouterConfig.verify]] as a sanity-check.
    */
  def bulkDynamic[F[_], Page, Props](toPage  : Path => F[Option[Parsed[Page]]],
                                     fromPage: Page => (Path, F[ActionF[F, Page, Props]]),
                                     notFound: Path => Parsed[Page])
                                    (implicit F: Sync[F]): RoutingRulesF[F, Page, Props] =
    apply[F, Page, Props](
      parseMulti     = p => dynamic(toPage(p)) :: Nil,
      path           = fromPage(_)._1,
      actionMulti    = (_, p) => dynamic(F.map(fromPage(p)._2)(Option(_))) :: Nil,
      fallbackAction = (_, _) => RedirectToPath(Path.root, SetRouteVia.HistoryPush), // won't happen
      whenNotFound   = notFound.andThen(F.pure(_)))
}
