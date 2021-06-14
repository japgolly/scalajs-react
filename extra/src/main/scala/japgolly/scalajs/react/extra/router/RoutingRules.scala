package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.extra.router.RouterConfig.Parsed
import japgolly.scalajs.react.util.DefaultEffects.Sync

/** A complete set of routing rules that allow the router to handle every all routes without further input.
  *
  * @tparam Page The type of legal pages. Most commonly, a sealed trait that you've created, where all subclasses
  *              represent a page in your SPA.
  */
final case class RoutingRules[Page, Props](parseMulti    : Path => List[StaticOrDynamic[Option[Parsed[Page]]]],
                                           path          : Page => Path,
                                           actionMulti   : (Path, Page) => List[StaticOrDynamic[Option[Action[Page, Props]]]],
                                           fallbackAction: (Path, Page) => Action[Page, Props],
                                           whenNotFound  : Path => Sync[Parsed[Page]]) {
  import RoutingRules.SharedLogic._

  def parse(path: Path): Sync[Parsed[Page]] =
    Sync.flatMap(selectParsed(path, parseMulti)) {
      case Some(a) => Sync.pure(a)
      case None    => whenNotFound(path)
    }

  def action(path: Path, page: Page): Sync[Action[Page, Props]] =
    Sync.map(selectAction(path, page, actionMulti))(_.getOrElse(fallbackAction(path, page)))
}

object RoutingRules {
  import StaticOrDynamic.Helpers._

  final class Exception(msg: String) extends RuntimeException(msg)

  private[router] object SharedLogic {

    def selectParsed[Page](path : Path,
                           parse: Path => List[StaticOrDynamic[Option[Parsed[Page]]]]): Sync[Option[Parsed[Page]]] =
      unambiguousRule(parse(path))(
        as => s"Multiple (${as.size}) (unconditional) routes specified for path ${path.value}",
        as => s"Multiple (${as.size}) conditional routes active for path ${path.value}")

    def selectAction[Page, Props](path  : Path,
                           page  : Page,
                           action: (Path, Page) => List[StaticOrDynamic[Option[Action[Page, Props]]]],
                     ): Sync[Option[Action[Page, Props]]] =
      unambiguousRule(action(path, page))(
        as => s"Multiple (${as.size}) (unconditional) actions specified for $page at path ${path.value}",
        as => s"Multiple (${as.size}) conditional actions active for $page at path ${path.value}")

    private def unambiguousRule[A](xs       : List[StaticOrDynamic[Option[A]]])
                                  (staticErr: List[A] => String,
                                   dynErr   : List[A] => String): Sync[Option[A]] = {
      val (statics, dynamics) = StaticOrDynamic.partition(xs)

      Sync.flatMap(Sync.sequenceList(dynamics)              )(dynamicOptions =>
      Sync.flatMap(unambiguousOption(dynamicOptions)(dynErr))(dynamicOption =>
      Sync.map   (unambiguousOption(statics)(staticErr)     )(staticOption =>
        dynamicOption.orElse(staticOption)
      )))
    }

    private def unambiguousOption[A](input: List[Option[A]])(errMsg: List[A] => String): Sync[Option[A]] = {
      val as = input.collect { case Some(a) => a }
      as match {
        case Nil      => Sync.pure(None)
        case a :: Nil => Sync.pure(Some(a))
        case as       => Sync.delay(throw new Exception(errMsg(as)))
      }
    }
  }

  def fromRule[Page, Props](rule          : RoutingRule[Page, Props],
                            fallbackPath  : Page => Path,
                            fallbackAction: (Path, Page) => Action[Page, Props],
                            whenNotFound  : Path => Sync[Parsed[Page]]): RoutingRules[Page, Props] = {

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

    def prepareActionFn(rule: RoutingRule[Page, Props]): (Path, Page) => List[StaticOrDynamic[Option[Action[Page, Props]]]] =
      rule match {
        case r: RoutingRule.Atom[Page, Props] =>
          (path, page) => static(r.action(path, page)) :: Nil

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
                  static[Option[Action[Page, Props]]](Some(RedirectToPath(expectedPath, r.redirectVia))) :: Nil
              case None =>
                Nil
            }

        case r: RoutingRule.Conditional[Page, Props] =>
          prepareActionFn(RoutingRule.ConditionalP(_ => r.condition, r.underlying, r.otherwise))

        case r: RoutingRule.ConditionalP[Page, Props] =>
          val underlying = prepareActionFn(r.underlying)
          (path, page) =>
            dynamic[Option[Action[Page, Props]]] {
              Sync.flatMap(SharedLogic.selectAction(path, page, underlying)) {
                case ok@Some(_) =>
                  Sync.map(r.condition(page)) {
                    case true  => ok
                    case false => r.otherwise(page)
                  }
                case None =>
                  Sync.pure(None)
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
  def bulk[Page, Props](toPage  : Path => Option[Parsed[Page]],
                        fromPage: Page => (Path, Action[Page, Props]),
                        notFound: Path => Parsed[Page]): RoutingRules[Page, Props] =
    apply[Page, Props](
      parseMulti     = p => static(toPage(p)) :: Nil,
      path           = fromPage(_)._1,
      actionMulti    = (_, p) => static(Option(fromPage(p)._2)) :: Nil,
      fallbackAction = (_, _) => RedirectToPath(Path.root, SetRouteVia.HistoryPush), // won't happen
      whenNotFound   = notFound.andThen(Sync.pure(_)))

  /** Create routing rules all at once, with compiler proof that all `Page`s will have a `Path` and `Action`
    * associated.
    *
    * The trade-off here is that care will need to be taken to ensure that path-parsing aligns with paths
    * generated for pages. It is recommended that you call [[RouterConfig.verify]] as a sanity-check.
    */
  def bulkDynamic[Page, Props](toPage  : Path => Sync[Option[Parsed[Page]]],
                               fromPage: Page => (Path, Sync[Action[Page, Props]]),
                               notFound: Path => Parsed[Page]): RoutingRules[Page, Props] =
    apply[Page, Props](
      parseMulti     = p => dynamic(toPage(p)) :: Nil,
      path           = fromPage(_)._1,
      actionMulti    = (_, p) => dynamic(Sync.map(fromPage(p)._2)(Option(_))) :: Nil,
      fallbackAction = (_, _) => RedirectToPath(Path.root, SetRouteVia.HistoryPush), // won't happen
      whenNotFound   = notFound.andThen(Sync.pure(_)))
}
