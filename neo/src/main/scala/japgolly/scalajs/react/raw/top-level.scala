package japgolly.scalajs.react.raw

import org.scalajs.dom
import scalajs.js
import scalajs.js.|

@js.native
object React extends React

@js.native
trait React extends js.Object {

//  def createClass[P,S,B,N <: TopNode](spec: ReactComponentSpec[P,S,B,N]): ReactClass[P,S,B,N] = js.native

//  def createFactory[P,S,B,N <: TopNode](t: ReactClass[P,S,B,N]): ReactComponentCU[P,S,B,N] = js.native
//  def createFactory[P <: js.Any, S <: js.Any, N <: TopNode](t: JsComponentType[P, S, N]): JsComponentC[P, S, N] = js.native

//  def createElement[P,S,B,N <: TopNode](t: ReactClass[P,S,B,N]): ReactComponentCU[P,S,B,N] = js.native
//  def createElement(tag: String, props: Object, children: ReactNode*): ReactDOMElement = js.native
//  def createElement(fc: FunctionalComponent[Nothing], props: Object, children: ReactNode*): ReactDOMElement = js.native
//  def createElement(fc: FunctionalComponent.WithChildren[Nothing], props: Object, children: ReactNode*): ReactDOMElement = js.native

  def createElement(`type`: String | ReactClass[_]): ReactDOMElement = js.native
  def createElement(`type`: String | ReactClass[_], props: js.Object = js.native): ReactDOMElement = js.native
  def createElement(`type`: String | ReactClass[_], props: js.Object, children: ReactNodeList*): ReactDOMElement = js.native

//  /** Verifies the object is a ReactElement. */
//  def isValidElement(o: JAny): Boolean = js.native

}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@js.native
object ReactDOM extends ReactDOM

@js.native
trait ReactDOM extends js.Object {

  final type Container = dom.html.Element | dom.svg.Element

  def render(element  : ReactElement,
             container: Container,
             callback : js.ThisFunction = js.native): ReactComponent[_ <: js.Object] = js.native

  def unmountComponentAtNode(container: dom.Node): Boolean = js.native

  def findDOMNode(component: ReactComponent[_]): dom.Element = js.native
}

