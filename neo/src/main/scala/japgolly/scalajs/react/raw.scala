package japgolly.scalajs.react

import org.scalajs.dom
import scalajs.js
import scalajs.js.|

package object raw {

  type JsNumber = Byte | Short | Int | Float | Double

  type Key = String | Boolean | JsNumber | Null
  type Ref = String | Null


  type ReactNode = ReactElement | ReactFragment | ReactText

  type ReactElement = ReactComponentElement[_] | ReactDOMElement

  @js.native
  trait PropsChildren extends js.Object {
    val children: ReactNodeList
  }

  @js.native
  trait ReactDOMElement extends js.Object {
    def `type`: String
    def props: PropsChildren
    def key: Key
    def ref: Ref
  }

  @js.native
  trait ReactComponentElement[Props <: js.Object] extends js.Object {
    def `type`: ReactClass[Props] | ReactFunctionalComponent[Props]
    def props: Props with PropsChildren
    def key: Key
    def ref: Ref
  }

  type ReactFragment = js.Array[ReactNode | ReactEmpty]

  type ReactNodeList = ReactNode | ReactEmpty

  type ReactText = String | JsNumber

  type ReactEmpty = Null | js.UndefOr[Nothing] | Boolean

  type ReactClass[Props <: js.Object] = Props => ReactComponent[Props]

  @js.native
  trait ReactComponent[+Props <: js.Object] extends js.Object {
    def props: Props with PropsChildren
    def render(): ReactElement
  }

  type ReactFunctionalComponent[+Props <: js.Object]  = Props => ReactElement

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  @js.native
  object ReactDOM extends ReactDOM

  @js.native
  trait ReactDOM extends js.Object {

    final type Container = dom.html.Element | dom.svg.Element

    def render(element  : ReactElement,
               container: Container,
               callback : js.ThisFunction = js.native): ReactComponent[_] = js.native

    def unmountComponentAtNode(container: Container): Boolean = js.native

    def findDOMNode(component: ReactComponent[_]): Container = js.native

  }
}

