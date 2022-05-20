package japgolly.scalajs.react.facade

import org.scalajs.dom
import scala.annotation.nowarn
import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.|

@JSImport("react-dom/client", JSImport.Namespace, "ReactDOM")
@js.native
object ReactDOM extends ReactDOM

@js.native
@nowarn("cat=unused")
trait ReactDOM extends ReactDOM18 {

  val version: String = js.native

  final type Container = dom.Element | dom.Document

  @deprecated("Use createRoot instead", "2.2.0 / React v18")
  final def render(element: React.Node, container: Container): React.ComponentUntyped = js.native

  final def render(element  : React.Node,
                   container: Container,
                   callback : js.Function0[Any]): React.ComponentUntyped = js.native

   @deprecated("Use hydrateRoot instead", "2.2.0 / React v18")
  final def hydrate(element: React.Node, container: Container): React.ComponentUntyped = js.native

  final def hydrate(element  : React.Node,
                    container: Container,
                    callback : js.Function0[Any]): React.ComponentUntyped = js.native

  @deprecated("Use root.unmount() instead", "2.2.0 / React v18")
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
