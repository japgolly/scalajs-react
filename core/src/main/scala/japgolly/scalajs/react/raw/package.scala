package japgolly.scalajs.react

import scala.scalajs.js.annotation.JSName
import scalajs.js
import scalajs.js.|

package object raw {

  type Void = Unit

  type JsNumber = Byte | Short | Int | Float | Double

  type Key = String | Boolean | JsNumber | Null

  // Deprecated by React
  type Ref = String | Null

  type RefFn = js.Function1[js.Any, Unit]

  type ReactText = String | JsNumber

  type ReactNode = ReactElement | ReactFragment | ReactText

  type ReactEmpty = Boolean | Void | Null

  type ReactNodeList = ReactNode | ReactEmpty

  type PropsChildren = ReactNodeList

  @js.native
  trait PropsWithChildren extends js.Object {
    val children: PropsChildren
  }

  /** ReactComponentElement | ReactDOMElement */
  @js.native
  trait ReactElement extends js.Object

  @js.native
  trait ReactDOMElement extends ReactElement {
    def `type`: String
    def props: PropsWithChildren
    def key: Key
    def ref: Ref
  }

  // function(props, context, updater)
  type ReactCtorAfterUse = js.Function3[js.Object, js.Any, js.Any, js.Any] with HasDisplayName

  @js.native
  trait ReactComponentElement extends ReactElement {
    def `type`: ReactCtorAfterUse
    def props: PropsWithChildren
    def key: Key
    def ref: Ref
  }

  // Type aliases can't be recursive
  // type ReactFragment = js.Array[ReactNode | ReactEmpty]
  @js.native
  trait ReactFragment extends js.Any
  implicit def ReactFragment[A](a: A)(implicit w: A => js.Array[ReactNode | ReactEmpty]): ReactFragment =
    w(a).asInstanceOf[ReactFragment]

//  def emptyReactNodeList: ReactNodeList =
//    js.undefined

  type ReactClass[P <: js.Object, S <: js.Object] = js.Function1[P, ReactComponent[P, S]] with HasDisplayName
  type ReactClassP[P <: js.Object] = ReactClass[P, _ <: js.Object]
  type ReactClassUntyped = ReactClass[_ <: js.Object, _ <: js.Object]

  @js.native
  trait HasDisplayName extends js.Object {
    val displayName: js.UndefOr[String] = js.native
  }

  /** Once-mounted component. */
  @js.native
  trait ReactComponent[P <: js.Object, S <: js.Object] extends js.Object {
    final type Props = P with PropsWithChildren
    final type State = S

    // [ 3/30] childContextTypes         : object   = null
    // [ 4/30] componentDidMount         : object   = null
    // [ 5/30] componentDidUpdate        : object   = null
    // [ 6/30] componentWillMount        : object   = null
    // [ 7/30] componentWillReceiveProps : object   = null
    // [ 8/30] componentWillUnmount      : object   = null
    // [ 9/30] componentWillUpdate       : object   = null
    final val constructor: ReactCtorAfterUse = js.native
    // [11/30] context                   : object   = [object Object]
    // [12/30] contextTypes              : object   = null
    def forceUpdate(callback: js.Function0[Unit] = js.native): Unit = js.native
    // [14/30] getChildContext           : object   = null
    // def getDefaultProps: Props = js.native
    // def getInitialState: State = js.native

    /** js.UndefOr because ES6 components return undefined */
    final def isMounted(): js.UndefOr[Boolean] = js.native

    // [18/30] isReactComponent          : object   = [object Object]
    // val mixins: js.Array[js.Object] | Null = js.native
    // [20/30] propTypes                 : object   = null
    final def props: Props = js.native
    // [22/30] refs                      : object   = [object Object]
    def render(): ReactElement
    final def replaceState(newState: State, callback: js.Function0[Unit] = js.native): Unit = js.native
    final def setState(partialState: js.Object, callback: js.Function0[Unit] = js.native): Unit = js.native
    @JSName("setState") final def modState(fn: js.Function1[State, js.Object], callback: js.Function0[Unit] = js.native): Unit = js.native
    // [26/30] shouldComponentUpdate     : object   = null
    final var state: State = js.native
    // [28/30] statics                   : object   = null
    // [29/30] updateComponent           : object   = null
    // [30/30] updater                   : object   = [object Object]
  }

  type ReactComponentUntyped = ReactComponent[_ <: js.Object, _ <: js.Object]

  @js.native
  trait ReactComponentSpec[P <: js.Object, S <: js.Object] extends js.Object {
    var displayName              : js.UndefOr[String]                                               = js.native
    var render                   : js.ThisFunction0[raw.ReactComponent[P, S], raw.ReactElement]     = js.native
    var getInitialState          : js.Function                                                      = js.native
 // var getInitialState          : js.ThisFunction0[raw.ReactComponentElement, S] | js.Function0[S] = js.native
    var componentWillMount       : js.ThisFunction0[raw.ReactComponent[P, S], Unit]                 = js.native
    var componentWillUnmount     : js.ThisFunction0[raw.ReactComponent[P, S], Unit]                 = js.native
    var componentDidMount        : js.ThisFunction0[raw.ReactComponent[P, S], Unit]                 = js.native
    var componentWillUpdate      : js.ThisFunction2[raw.ReactComponent[P, S], P, S, Unit]           = js.native
    var componentDidUpdate       : js.ThisFunction2[raw.ReactComponent[P, S], P, S, Unit]           = js.native
    var componentWillReceiveProps: js.ThisFunction1[raw.ReactComponent[P, S], P, Unit]              = js.native
    var shouldComponentUpdate    : js.ThisFunction2[raw.ReactComponent[P, S], P, S, Boolean]        = js.native
 // var getDefaultProps          : xxxxx                                                            = js.native
 // var mixins                   : xxxxx                                                            = js.native
  }

}

