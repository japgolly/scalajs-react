package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.CallbackTo
import japgolly.scalajs.react.extra.router.RouterConfig.Parsed

final case class RoutingRules[Page](parse: Path => CallbackTo[Parsed[Page]],
                                    path: Page => Path,
                                    action: (Path, Page) => CallbackTo[Action[Page]])

object RoutingRules {

  def fromRule[Page](rule          : Rule[Page],
                     fallbackPath  : Page => Path,
                     fallbackAction: (Path, Page) => Action[Page],
                     whenNotFound  : Path => Parsed[Page]): RoutingRules[Page] = {

    def prepareParseFn(rule: Rule[Page]): Path => CallbackTo[Option[Parsed[Page]]] =
      rule match {
        case r: Rule.Atom       [Page] => r.parse.andThen(CallbackTo.pure)
        case r: Rule.AutoCorrect[Page] => prepareParseFn(r.underlying)
        case r: Rule.Or         [Page] => prepareParseFn(r.lhs) || prepareParseFn(r.rhs)
        case r: Rule.Conditional[Page] =>
          val parse = prepareParseFn(r.underlying)
//          p => r.condition(p).flatMap(ok => if (ok) onOk(p) else CallbackTo.pure(None))
          parse(_).flatMap {
            case Some(x) => xxx
            case None => xxx
          }
      }

    def preparePathFn(rule: Rule[Page]): Page => Option[Path] =
      rule match {
        case r: Rule.Atom       [Page] => r.path
        case r: Rule.Conditional[Page] => preparePathFn(r.underlying)
        case r: Rule.AutoCorrect[Page] => preparePathFn(r.underlying)
        case r: Rule.Or         [Page] => preparePathFn(r.lhs) || preparePathFn(r.rhs)
      }

    def prepareActionFn(rule: Rule[Page]): (Path, Page) => CallbackTo[Option[Action[Page]]] =
      rule match {
        case r: Rule.Atom       [Page] => (path, page) => CallbackTo.pure(r.action(path, page))
        case r: Rule.Or         [Page] => prepareActionFn(r.lhs) || prepareActionFn(r.rhs)

        case r: Rule.AutoCorrect[Page] =>
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

        case r: Rule.Conditional[Page] =>
          val onOk = prepareActionFn(r.underlying)
          (path, page) =>
            r.condition(page).flatMap {
              case true  => onOk(path, page)
              case false => CallbackTo.pure(r.otherwise(page))
            }
      }

    val parse: Path => CallbackTo[Parsed[Page]] = {
      val attempt = prepareParseFn(rule)
      p => attempt(p).map(_.getOrElse(whenNotFound(p)))
    }

    val path: Page => Path =
      preparePathFn(rule) | fallbackPath

    val action: (Path, Page) => CallbackTo[Action[Page]] = {
      val attempt = prepareActionFn(rule)
      (path, page) => attempt(path, page).map(_.getOrElse(fallbackAction(path, page)))
    }

    apply(parse, path, action)
  }

}
