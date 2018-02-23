package japgolly.scalajs.react.raw

import org.scalajs.dom
import scalajs.js
import scalajs.js.annotation._

@JSImport("react-dom", JSImport.Namespace, "ReactDOM")
@js.native
object ReactDOM extends ReactDOM

@js.native
trait ReactDOM extends js.Object {

  final type Container = dom.Element

  def render(element  : React.Element,
             container: Container,
             callback : js.Function0[Unit] = js.native): React.ComponentUntyped = js.native

  def unmountComponentAtNode(container: dom.Node): Boolean = js.native

  def findDOMNode(component: React.ComponentUntyped): dom.Element = js.native
}
