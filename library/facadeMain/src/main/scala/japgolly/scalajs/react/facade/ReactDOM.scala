package japgolly.scalajs.react.facade

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.|

@JSImport("react-dom", JSImport.Namespace, "ReactDOM")
@js.native
object ReactDOM extends ReactDOM

@js.native
trait ReactDOM extends js.Object {

  final type Container = dom.Element | dom.Document | dom.DocumentFragment

  val version: String = js.native

  final type DomNode = dom.Node

  final def createPortal(child: React.Node, container: Container): React.Node = js.native

  final def flushSync[R](f: js.Function0[R]): R = js.native
  final def flushSync[A, R](f: js.Function1[A, R], a: A): R = js.native

  @js.native
  trait FormStatus extends js.Object {
    val pending: Boolean                                                             = js.native
    val data   : dom.FormData | Null                                                 = js.native
    val method : String | Null                                                       = js.native
    val action : String | js.Function1[dom.FormData, Unit | js.Thenable[Any]] | Null = js.native
  }

  final def useFormStatus(): FormStatus = js.native

  /** @since 4.0.0 / React v19 */
  final def requestFormReset(form: dom.HTMLFormElement): Unit = js.native

  /** @since 4.0.0 / React v19 */
  final def prefetchDNS(href: String): Unit = js.native

  /** @since 4.0.0 / React v19 */
  @js.native
  trait PreconnectOptions extends js.Object {
    var crossOrigin: js.UndefOr[String]
  }

  /** @since 4.0.0 / React v19 */
  final def preconnect(href: String, options: js.UndefOr[PreconnectOptions] = js.native): Unit = js.native

  /** @since 4.0.0 / React v19 */
  @js.native
  trait PreloadOptions extends js.Object {
    var as            : String
    var crossOrigin   : js.UndefOr[String]
    var fetchPriority : js.UndefOr[String]
    var imageSizes    : js.UndefOr[String]
    var imageSrcSet   : js.UndefOr[String]
    var integrity     : js.UndefOr[String]
    var `type`        : js.UndefOr[String]
    var nonce         : js.UndefOr[String]
    var referrerPolicy: js.UndefOr[String]
    var media         : js.UndefOr[String]
  }

  /** @since 4.0.0 / React v19 */
  final def preload(href: String, options: PreloadOptions = js.native): Unit = js.native

  /** @since 4.0.0 / React v19 */
  @js.native
  trait PreinitOptions extends js.Object {
    var as           : String
    var crossOrigin  : js.UndefOr[String]
    var fetchPriority: js.UndefOr[String]
    var precedence   : js.UndefOr[String]
    var integrity    : js.UndefOr[String]
    var nonce        : js.UndefOr[String]
  }

  /** @since 4.0.0 / React v19 */
  final def preinit(href: String, options: PreinitOptions = js.native): Unit = js.native
}
