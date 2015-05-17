package japgolly.scalajs.react.extra.router2

import org.scalajs.dom
import scalaz.Equal
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.assertWarn

// =====================================================================================================================
// URLs

/**
 * The prefix of all routes on a page.
 *
 * The router expects this to be a full URL.
 * Examples: `BaseUrl("http://www.blah.com/hello")`,  `BaseUrl.fromWindowOrigin / "hello"`.
 */
final case class BaseUrl(value: String) {
  assertWarn(value contains "://", s"$this doesn't seem to be a valid URL. It's missing '://'. Consider using BaseUrl.fromWindowOrigin.")

  def +(p: String)            : BaseUrl = BaseUrl(value + p)
  def /(p: String)            : BaseUrl = BaseUrl(value + "/" + p)
  def map(f: String => String): BaseUrl = BaseUrl(f(value))
  def apply(p: Path)          : AbsUrl  = AbsUrl(value + p.value)
  def abs                     : AbsUrl  = AbsUrl(value)
}
object BaseUrl {
  def fromWindowOrigin = BaseUrl(dom.window.location.origin)
}

/**
 * The portion of the url after the [[japgolly.scalajs.react.extra.router.BaseUrl]].
 */
final case class Path(value: String) {
  def abs(implicit base: BaseUrl): AbsUrl = base apply this
}
object Path {
  implicit val equality: Equal[Path] = Equal.equalA
  def root = Path("")
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

// =====================================================================================================================
// Actions

// If we don't extend Product with Serializable here, a method that returns both a Renderer[P] and a Redirect[P] will
// be type-inferred to "Product with Serializable with Action[P]" which breaks the Renderable & Actionable implicits.
sealed trait Action[P] extends Product with Serializable

final case class Renderer[P](f: RouterCtl[P] => ReactElement) extends Action[P] {
  @inline def apply(ctl: RouterCtl[P]) = f(ctl)
}

sealed trait Redirect[P] extends Action[P]

object Redirect {
  sealed trait Method

  /** The current URL will not be recorded in history. User can't hit ''Back'' button to reach it. */
  case object Replace extends Method

  /** The current URL will be recorded in history. User can hit ''Back'' button to reach it. */
  case object Push extends Method
}

final case class RedirectToPage[P](page: P, method: Redirect.Method) extends Redirect[P]

final case class RedirectToPath[P](path: Path, method: Redirect.Method) extends Redirect[P]

// =====================================================================================================================
// Other

/**
 * Result of the router resolving a URL and reaching a conclusion about what to render.
 *
 * @param page Data representation (or command) of what will be drawn.
 * @param render The render function provided by the rules and logic in [[RouterConfig]].
 */
final case class Resolution[P](page: P, render: () => ReactElement)
