package japgolly.scalajs.react.raw

import org.scalajs.dom
import scala.annotation.nowarn
import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.|

@JSImport("react-dom", JSImport.Namespace, "ReactDOM")
@js.native
object ReactDOM extends ReactDOM

@js.native
@nowarn("cat=unused")
trait ReactDOM extends js.Object {

  val version: String = js.native

  final type Container = dom.Element | dom.raw.Document

  final def render(element  : React.Node,
                   container: Container,
                   callback : js.Function0[Unit] = js.native): React.ComponentUntyped = js.native

  final def hydrate(element  : React.Node,
                    container: Container,
                    callback : js.Function0[Unit] = js.native): React.ComponentUntyped = js.native

  final def unmountComponentAtNode(container: dom.Node): Boolean = js.native

  // ==========================================================================
  // NOTE: Ensure that ComponentDom is kept up-to-date with this type
  //
  final type DomNode = dom.Element | dom.Text
  // ==========================================================================

  @throws[js.JavaScriptException]("if arg isn't a React component or its unmounted")
  final def findDOMNode(componentOrElement: dom.Element | React.ComponentUntyped): DomNode | Null = js.native

  final def createPortal(child: React.Node, container: Container): React.Node = js.native

  final def flushSync[R](f: js.Function0[R]): R = js.native
  final def flushSync[A, R](f: js.Function1[A, R], a: A): R = js.native
}
