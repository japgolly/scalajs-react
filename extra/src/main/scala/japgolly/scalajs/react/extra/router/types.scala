package japgolly.scalajs.react.extra.router

import org.scalajs.dom
import japgolly.scalajs.react.extra.internal.assertWarn
import japgolly.scalajs.react.vdom.VdomElement

// =====================================================================================================================
// URLs

abstract class PathLike[Self <: PathLike[Self]] {
  this: Self =>
  protected def make(s: String): Self
  protected def str(s: Self): String

  final def map(f: String => String): Self    = make(f(str(this)))
  final def +(p: String)            : Self    = map(_ + p)
  final def +(p: Self)              : Self    = this + str(p)
  final def /(p: String)            : Self    = endWith_/ + p
  final def /(p: Self)              : Self    = this / str(p)
  final def endWith_/               : Self    = map(_.replaceFirst("/*$", "/"))
  final def rtrim_/                 : Self    = map(_.replaceFirst("/+$", ""))
  final def isEmpty                 : Boolean = str(this).isEmpty
  final def nonEmpty                : Boolean = str(this).nonEmpty
}

/**
 * The prefix of all routes on a page.
 *
 * The router expects this to be a full URL.
 * Examples: `BaseUrl("http://www.blah.com/hello")`,  `BaseUrl.fromWindowOrigin / "hello"`.
 */
final case class BaseUrl(value: String) extends PathLike[BaseUrl] {
  assertWarn(value contains "://", s"$this doesn't seem to be a valid URL. It's missing '://'. Consider using BaseUrl.fromWindowOrigin.")

  override protected def make(s: String) = BaseUrl(s)
  override protected def str(s: BaseUrl) = s.value

  def apply(p: Path): AbsUrl = AbsUrl(value + p.value)
  def abs           : AbsUrl = AbsUrl(value)
}

object BaseUrl {
  def fromWindowOrigin: BaseUrl = {
    val l = dom.window.location
    var url = l.protocol + "//" + l.hostname
    if (!l.port.matches("^(?:80)?$"))
      url += ":" + l.port
    BaseUrl(url)
  }

  def fromWindowOrigin_/ : BaseUrl =
    fromWindowOrigin.endWith_/

  def fromWindowUrl(f: String => String): BaseUrl =
    BaseUrl(f(dom.window.location.href))

  def until(stopAt: String): BaseUrl =
    fromWindowUrl { u =>
    val i = u indexOf stopAt
    if (i < 0) u else u.take(i)
  }

  def until_# : BaseUrl =
    until("#")
}

/**
 * The portion of the URL after the [[BaseUrl]].
 */
final case class Path(value: String) extends PathLike[Path] {
  override protected def make(s: String) = Path(s)
  override protected def str(s: Path) = s.value

  def abs(implicit base: BaseUrl): AbsUrl = base apply this

  /**
   * Attempts to remove an exact prefix and return a non-empty suffix.
   */
  def removePrefix(prefix: String): Option[Path] = {
    val l = prefix.length
    if (value.length > l && value.startsWith(prefix))
      Some(Path(value substring l))
    else
      None
  }
}
object Path {
  def root = Path("")
}

/**
 * An absolute URL.
 */
final case class AbsUrl(value: String) extends PathLike[AbsUrl] {
  assertWarn(value contains "://", s"$this doesn't seem to be a valid URL. It's missing '://'. Consider using AbsUrl.fromWindow.")
  override protected def make(s: String) = AbsUrl(s)
  override protected def str(s: AbsUrl) = s.value
}
object AbsUrl {
  def fromWindow = AbsUrl(dom.window.location.href)
}

// =====================================================================================================================
// Actions

// If we don't extend Product with Serializable here, a method that returns both a Renderer[P] and a Redirect[P] will
// be type-inferred to "Product with Serializable with Action[P]" which breaks the Renderable & Actionable implicits.
sealed trait Action[P] extends Product with Serializable {
  def map[A](f: P => A): Action[A]
}

final case class Renderer[P](f: RouterCtl[P] => VdomElement) extends Action[P] {
  def apply(ctl: RouterCtl[P]) = f(ctl)

  override def map[A](g: P => A): Renderer[A] =
    Renderer(r => f(r contramap g))
}

sealed trait Redirect[P] extends Action[P] {
  override def map[A](f: P => A): Redirect[A]
}

object Redirect {
  sealed trait Method

  /** The current URL will not be recorded in history. User can't hit ''Back'' button to reach it. */
  case object Replace extends Method

  /** The current URL will be recorded in history. User can hit ''Back'' button to reach it. */
  case object Push extends Method

  /** `window.location.href` will be programmatically set to the new URL.
    * If the new URL is part of the current SPA, the entire SPA will be reloaded.
    *
    * The current URL will be recorded in history. User can hit ''Back'' button to reach it.
    */
  case object Force extends Method
}

final case class RedirectToPage[P](page: P, method: Redirect.Method) extends Redirect[P] {
  override def map[A](f: P => A): RedirectToPage[A] =
    RedirectToPage(f(page), method)
}

final case class RedirectToPath[P](path: Path, method: Redirect.Method) extends Redirect[P] {
  override def map[A](f: P => A): RedirectToPath[A] =
    RedirectToPath(path, method)
}

// =====================================================================================================================
// Other

/**
 * Result of the router resolving a URL and reaching a conclusion about what to render.
 *
 * @param page Data representation (or command) of what will be drawn.
 * @param render The render function provided by the rules and logic in [[RouterConfig]].
 */
final case class Resolution[P](page: P, render: () => VdomElement)
