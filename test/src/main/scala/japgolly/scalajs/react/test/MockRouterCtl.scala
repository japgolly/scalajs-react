package japgolly.scalajs.react.test

import scalajs.js.URIUtils
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._
import MockRouterCtl._

/**
 * Mock [[RouterCtl]] that does nothing but record events.
 *
 * Call [[events()]] to inspect recorded events, and [[clear()]] to clear them.
 */
class MockRouterCtl[P](override val baseUrl: BaseUrl, pageToPath: P => Path) extends RouterCtl[P] {
  override val byPath: RouterCtl[Path] =
    new RouterCtl[Path] {
      override def byPath           = this
      override def baseUrl          = MockRouterCtl.this.baseUrl
      override def refresh          = MockRouterCtl.this.refresh
      override def pathFor(p: Path) = p
      override def set(p: Path)     = logEvent(SetUrlToPath(p))
    }

  override def refresh       = logEvent(Refresh)
  override def set(p: P)     = logEvent(SetUrlToPage(p, pathFor(p)))
  override def pathFor(p: P) = pageToPath(p)

  private def logEvent(e: => Event[P]): Callback =
    Callback(_events :+= e)

  protected var _events = Vector.empty[Event[P]]

  def events(): Vector[Event[P]] =
    _events

  def clear(): Unit =
    _events = Vector.empty
}

object MockRouterCtl {

  sealed trait Event[+P]
  case object Refresh                                  extends Event[Nothing]
  case class SetUrlToPage[+P](page: P, mockPath: Path) extends Event[P]
  case class SetUrlToPath    (path: Path)              extends Event[Nothing]

  def apply[P](baseUrl   : BaseUrl   = defaultBaseUrl,
               pageToPath: P => Path = defaultPageToPath[P]
              ): MockRouterCtl[P] =
    new MockRouterCtl(baseUrl, pageToPath)

  def defaultBaseUrl: BaseUrl =
    BaseUrl("http://mock.localhost/")

  def defaultPageToPath[P]: P => Path =
    p => Path(URIUtils encodeURIComponent p.toString)
}
