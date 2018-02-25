package japgolly.scalajs.react.raw

import org.scalajs.dom
import scala.scalajs.js.|
import scala.scalajs.js
import scala.scalajs.js.annotation._

@JSImport("react-dom", JSImport.Namespace, "ReactDOM")
@js.native
object ReactDOM extends ReactDOM

@js.native
trait ReactDOM extends js.Object {

  final type Container = dom.Element | dom.raw.Document

  def render(element  : React.Element,
             container: Container,
             callback : js.Function0[Unit] = js.native): React.ComponentUntyped = js.native

  def unmountComponentAtNode(container: dom.Node): Boolean = js.native

  // ==========================================================================
  //
  // NOTE: Ensure that Generic.MountedDomNode is kept up-to-date with this type
  //
  // ==========================================================================
  final type FindDomNodeResult = dom.Element | dom.Text | Null

  def findDOMNode(componentOrElement: dom.Element | React.ComponentUntyped): FindDomNodeResult = js.native
}
