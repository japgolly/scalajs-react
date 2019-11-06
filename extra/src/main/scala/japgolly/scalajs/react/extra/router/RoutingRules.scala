package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.CallbackTo
import japgolly.scalajs.react.extra.router.RouterConfig.Parsed

final case class RoutingRules[Page](parse: Path => CallbackTo[Parsed[Page]],
                                    path: Page => Path,
                                    action: (Path, Page) => CallbackTo[Action[Page]])

object RoutingRules {

  def fromRule[Page](rule          : RoutingRule[Page],
                     fallbackPath  : Page => Path,
                     fallbackAction: (Path, Page) => Action[Page],
                     whenNotFound  : Path => CallbackTo[Parsed[Page]]): RoutingRules[Page] = {

    def prepareParseFn(rule: RoutingRule[Page]): Path => CallbackTo[Option[Parsed[Page]]] =
      rule match {
        case r: RoutingRule.Atom        [Page] => r.parse.andThen(CallbackTo.pure)
        case r: RoutingRule.AutoCorrect [Page] => prepareParseFn(r.underlying)
        case r: RoutingRule.Or          [Page] => prepareParseFn(r.lhs) || prepareParseFn(r.rhs)

        case r: RoutingRule.Conditional[Page] =>
          val onOk = prepareParseFn(r.underlying)
          p => r.condition.flatMap(ok => if (ok) onOk(p) else CallbackTo.pure(None))

        case r: RoutingRule.ConditionalP[Page] =>
          val parse = prepareParseFn(r.underlying)
          parse(_).flatMap {
            case ok@ Some(Right(page)) =>
              r.condition(page).map {
                case true  => ok
                case false => None
              }
            case other => CallbackTo.pure(other)
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

    def prepareActionFn(rule: RoutingRule[Page]): (Path, Page) => CallbackTo[Option[Action[Page]]] =
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

    val parse: Path => CallbackTo[Parsed[Page]] = {
      val attempt = prepareParseFn(rule)
      p => attempt(p).flatMap {
        case Some(r) => CallbackTo.pure(r)
        case None    => whenNotFound(p)
      }
    }

    val path: Page => Path =
      preparePathFn(rule) | fallbackPath

    val action: (Path, Page) => CallbackTo[Action[Page]] = {
      val attempt = prepareActionFn(rule)
      (path, page) => attempt(path, page).map(_.getOrElse(fallbackAction(path, page)))
    }

    println(rule)

    apply(parse, path, action)
  }

}
