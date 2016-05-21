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

//  type Props = js.Object | Null
  type State = js.Object | Null

  @js.native
  trait PropsWithChildren extends js.Object {
    val children: ReactNodeList
  }

  @js.native
  trait ReactDOMElement extends js.Object {
    def `type`: String
    def props: PropsWithChildren
    def key: Key
    def ref: Ref
  }

  @js.native
  trait ReactComponentElement[Props <: js.Object] extends js.Object {
    // [1/7] $$typeof : number   = 60103
    // [2/7] _owner   : object   = null
    // [3/7] _store   : object   = [object Object]
    // [4/7] key      : object   = null
    // [5/7] props    : object   = [object Object]
    // [6/7] ref      : object   = null
    // [7/7] type     : function = function (props, context, updater) {
    def `type`: ReactClass[Props] | ReactFunctionalComponent[Props]
    def props: Props with PropsWithChildren
    def key: Key
    def ref: Ref
  }

  // Type aliases can't be recursive
  // type ReactFragment = js.Array[ReactNode | ReactEmpty]
  @js.native
  trait ReactFragment extends js.Any
  @inline implicit def ReactFragment[A](a: A)(implicit w: A => js.Array[ReactNode | ReactEmpty]): ReactFragment =
    w(a).asInstanceOf[ReactFragment]

  /** Type of `props.children` */
  type ReactNodeList = ReactNode | ReactEmpty

  def emptyReactNodeList: ReactNodeList =
    js.undefined

  type ReactText = String | JsNumber

  type ReactEmpty = Null | js.UndefOr[Nothing] | Boolean

  type ReactClass[Props <: js.Object] = js.Function1[Props, ReactComponent[Props]]

  /** Once-mounted component. */
  @js.native
  trait ReactComponent[+Props <: js.Object] extends js.Object {
    // [ 1/30] __reactAutoBindPairs      : object   =
    // [ 2/30] _reactInternalInstance    : object   = [object Object]
    // [ 3/30] childContextTypes         : object   = null
    // [ 4/30] componentDidMount         : object   = null
    // [ 5/30] componentDidUpdate        : object   = null
    // [ 6/30] componentWillMount        : object   = null
    // [ 7/30] componentWillReceiveProps : object   = null
    // [ 8/30] componentWillUnmount      : object   = null
    // [ 9/30] componentWillUpdate       : object   = null
    // [10/30] constructor               : function = function (props, context, updater) {
    // [11/30] context                   : object   = [object Object]
    // [12/30] contextTypes              : object   = null
    // [13/30] forceUpdate               : function = function (callback) {
    // [14/30] getChildContext           : object   = null
    def getDefaultProps: Props
    def getInitialState: State
    def isMounted(): Boolean
    // [18/30] isReactComponent          : object   = [object Object]
    // [19/30] mixins                    : object   = null
    // [20/30] propTypes                 : object   = null
    def props: Props with PropsWithChildren
    // [22/30] refs                      : object   = [object Object]
    def render(): ReactElement
    // [24/30] replaceState              : function = function (newState, callback) {
    // [25/30] setState                  : function = function (partialState, callback) {
    def setState(partialState: js.Object, callback: js.Function = js.native): Unit = js.native
    // [26/30] shouldComponentUpdate     : object   = null
    def state: State
    // [28/30] statics                   : object   = null
    // [29/30] updateComponent           : object   = null
    // [30/30] updater                   : object   = [object Object]
  }

  type ReactFunctionalComponent[-Props <: js.Object] = js.Function1[Props, ReactElement]
}

