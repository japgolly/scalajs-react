package japgolly.scalajs.react.test

import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.util.Effect.Sync
import scala.scalajs.js.URIUtils

/** Mock [[RouterCtl]] that does nothing but record events.
  *
  * Call [[events()]] to inspect recorded events, and [[clear()]] to clear them.
  */
class MockRouterCtlF[F[_], P](override val baseUrl: BaseUrl, pageToPath: P => Path)(implicit ff: Sync[F]) extends RouterCtlF[F, P] { self =>
  import MockRouterCtlF._

  protected implicit def F: Sync[F] =
    ff

  override val byPath: RouterCtlF[F, Path] =
    new RouterCtlF[F, Path] {
    override protected implicit def F           = self.F
      override def byPath                       = this
      override def baseUrl                      = self.baseUrl
      override def refresh                      = self.refresh
      override def pathFor(p: Path)             = p
      override def set(p: Path, v: SetRouteVia) = logEvent(SetUrlToPath(p, v))
    }

  override def refresh                   = logEvent(Refresh)
  override def pathFor(p: P)             = pageToPath(p)
  override def set(p: P, v: SetRouteVia) = logEvent(SetUrlToPage(p, pathFor(p), v))

  private def logEvent(e: => Event[P]): F[Unit] =
    F.delay(_events :+= e)

  protected var _events = Vector.empty[Event[P]]

  def events(): Vector[Event[P]] =
    _events

  def clear(): Unit =
    _events = Vector.empty
}

object MockRouterCtlF {
  sealed trait Event[+P]
  case object Refresh                                                          extends Event[Nothing]
  final case class SetUrlToPage[+P](page: P, mockPath: Path, via: SetRouteVia) extends Event[P]
  final case class SetUrlToPath    (path: Path, via: SetRouteVia)              extends Event[Nothing]

  def defaultBaseUrl: BaseUrl =
    BaseUrl("http://mock.localhost/")

  def defaultPageToPath[P]: P => Path =
    p => Path(URIUtils encodeURIComponent p.toString)

  def apply[F[_]: Sync, P](baseUrl   : BaseUrl   = defaultBaseUrl,
                           pageToPath: P => Path = defaultPageToPath[P]): MockRouterCtlF[F, P] =
    new MockRouterCtlF(baseUrl, pageToPath)
}
