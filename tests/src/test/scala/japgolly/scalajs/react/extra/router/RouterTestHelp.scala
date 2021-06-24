package japgolly.scalajs.react.extra.router

import org.scalajs.dom.window

object RouterTestHelp {

  val localBaseUrl: BaseUrl =
    if (window.navigator.userAgent.toLowerCase contains "phantom")
      BaseUrl("file:///routerDemo")
    else
      BaseUrl("http://localhost")

  val localBaseUrl_/ : BaseUrl =
    localBaseUrl.endWith_/

}
