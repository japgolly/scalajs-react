package japgolly.scalajs.react.extra.router

import org.scalajs.dom
import scalaz.std.string.stringInstance
import scalaz.{\/, Equal}
import japgolly.scalajs.react.extra.assertWarn

/**
 * The prefix of all routes in a set.
 *
 * The router expects this to be a full URL.
 * Examples: `BaseUrl("http://www.blah.com/hello")`,  `BaseUrl.fromWindow / "hello"`.
 */
final case class BaseUrl(value: String) {
  assertWarn(value contains "://", s"$this doesn't seem to be a valid URL. It's missing '://'. Consider using BaseUrl.fromWindow.")

  def +(p: String): BaseUrl = BaseUrl(value + p)
  def /(p: String): BaseUrl = BaseUrl(value + "/" + p)
  def /(p: Path)  : AbsUrl  = AbsUrl(value + p.value)
  def abs         : AbsUrl  = AbsUrl(value)
}
object BaseUrl {
  def fromWindow = BaseUrl(dom.window.location.origin)
}

/**
 * The portion of the url after the [[japgolly.scalajs.react.extra.router.BaseUrl]].
 */
final case class Path(value: String) {
  def abs(implicit base: BaseUrl): AbsUrl = base / this
}

/**
 * An absolute URL.
 */
final case class AbsUrl(value: String) {
  assertWarn(value contains "://", s"$this doesn't seem to be a valid URL. It's missing '://'. Consider using AbsUrl.fromWindow.")
}
object AbsUrl {
  def fromWindow: AbsUrl = AbsUrl(dom.window.location.href)
}

/**
 * A valid path in route set P.
 */
sealed trait ApprovedPath[P] {
  def path: Path
}

sealed trait RouteAction[P]

final case class Location[P] private[router] (path: Path, render: Renderer[P]) extends RouteAction[P] with ApprovedPath[P]
object Location {
  implicit def equivalence[P]: Equal[Location[P]] = Equal.equalBy(_.path.value)
}

final case class Redirect[P] private[router] (to: Redirect.Target[P], method: Redirect.Method) extends RouteAction[P]
object Redirect {
  type Target[P] = Path \/ Location[P]

  sealed trait Method
  /** The current URL will not be recorded in history. User can't hit ''Back'' button to reach it. */
  case object Replace extends Method
  /** The current URL will be recorded in history. User can hit ''Back'' button to reach it. */
  case object Push extends Method
}

/**
 * A dynamically-generated route presumed by the routing rules to be correct.
 */
final case class DynamicLocation[P] private[router] (path: Path) extends ApprovedPath[P]
