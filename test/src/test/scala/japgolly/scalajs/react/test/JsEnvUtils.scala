package japgolly.scalajs.react.test

import scala.util.Try
import scala.util.control.NonFatal
import scalajs.js.Dynamic.global
import Console._

object JsEnvUtils {

  /** Sample (real) values are:
    * - Mozilla/5.0 (Unknown; Linux x86_64) AppleWebKit/538.1 (KHTML, like Gecko) PhantomJS/2.1.1 Safari/538.1
    * - Mozilla/5.0 (X11; Linux x86_64; rv:45.0) Gecko/20100101 Firefox/45.0
    * - Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.75 Safari/537.36
    */
  val userAgent: String =
  Try(global.navigator.userAgent.asInstanceOf[String]) getOrElse "Unknown"

  // Check each browser
  val isFirefox = userAgent contains "Firefox"
  val isChrome  = userAgent contains "Chrome"

  val isRealBrowser = isFirefox || isChrome

  private val skipMTest =
    YELLOW + "Skipped; need real browser." + RESET

  def requiresRealBrowser(test: => Any): Any =
    if (isRealBrowser)
      try
        test
      catch {
        case NonFatal(t) => throw t
        case t: Throwable => throw new RuntimeException(t)
      }
    else
      skipMTest
}
