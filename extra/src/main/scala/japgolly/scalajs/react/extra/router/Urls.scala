package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.internal.Util.devAssertWarn
import org.scalajs.dom

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

/** The prefix of all routes on a page.
  *
  * The router expects this to be a full URL.
  * Examples: `BaseUrl("http://www.blah.com/hello")`,  `BaseUrl.fromWindowOrigin / "hello"`.
  */
final case class BaseUrl(value: String) extends PathLike[BaseUrl] {
  devAssertWarn(
    value contains "://",
    s"$this doesn't seem to be a valid URL. It's missing '://'. Consider using BaseUrl.fromWindowOrigin.")

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

/** The portion of the URL after the [[BaseUrl]]. */
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

/** An absolute URL. */
final case class AbsUrl(value: String) extends PathLike[AbsUrl] {
  devAssertWarn(
    value contains "://",
    s"$this doesn't seem to be a valid URL. It's missing '://'. Consider using AbsUrl.fromWindow.")

  override protected def make(s: String) = AbsUrl(s)
  override protected def str(s: AbsUrl) = s.value
}

object AbsUrl {
  def fromWindow = AbsUrl(dom.window.location.href)
}

