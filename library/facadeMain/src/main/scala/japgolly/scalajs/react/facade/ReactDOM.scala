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

  // ==========================================================================
  // NOTE: Ensure that ComponentDom is kept up-to-date with this type
  //
  final type DomNode = dom.Node
  // ==========================================================================

  final def createPortal(child: React.Node, container: Container): React.Node = js.native

  final def flushSync[R](f: js.Function0[R]): R = js.native
  final def flushSync[A, R](f: js.Function1[A, R], a: A): R = js.native
}
