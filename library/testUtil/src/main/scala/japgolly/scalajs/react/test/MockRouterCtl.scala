package japgolly.scalajs.react.test

import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.util.DefaultEffects.Sync

/** Mock [[RouterCtl]] that does nothing but record events.
  *
  * Call [[events()]] to inspect recorded events, and [[clear()]] to clear them.
  */
class MockRouterCtl[P](baseUrl: BaseUrl, pageToPath: P => Path) extends MockRouterCtlF[Sync, P](baseUrl, pageToPath)

object MockRouterCtl {
  type Event       [+P] = MockRouterCtlF.Event       [P]
  type SetUrlToPage[+P] = MockRouterCtlF.SetUrlToPage[P]
  type SetUrlToPath     = MockRouterCtlF.SetUrlToPath

  val Refresh      = MockRouterCtlF.Refresh
  val SetUrlToPage = MockRouterCtlF.SetUrlToPage
  val SetUrlToPath = MockRouterCtlF.SetUrlToPath

  @inline def defaultBaseUrl       = MockRouterCtlF.defaultBaseUrl
  @inline def defaultPageToPath[P] = MockRouterCtlF.defaultPageToPath[P]

  def apply[P](baseUrl   : BaseUrl   = defaultBaseUrl,
               pageToPath: P => Path = defaultPageToPath[P]): MockRouterCtl[P] =
    new MockRouterCtl(baseUrl, pageToPath)
}
