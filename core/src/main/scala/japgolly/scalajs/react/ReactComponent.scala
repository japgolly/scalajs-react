package japgolly.scalajs.react

import japgolly.scalajs.react

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.{JSName, ScalaJSDefined}

@js.native
@JSName("React.Component")
private[react] class ReactJSComponent[P, S] extends js.Object {
  val displayName: String = js.native

  var refs: RefsObject = js.native

  var context: js.Dynamic = js.native
  @JSName("state")
  private[react] var _state: WrapObj[S] = js.native

  def componentDidMount(): Unit = js.native

  def componentWillUnmount(): Unit = js.native

  @JSName("forceUpdate")
  private[react] def _forceUpdate(callback: js.UndefOr[js.Function]): Unit = js.native

  @JSName("componentWillReceiveProps")
  private[react] def _componentWillReceiveProps(nextProps: WrapObj[P]): Unit = js.native

  @JSName("shouldComponentUpdate")
  private[react] def _shouldComponentUpdate(nextProps: WrapObj[P], nextState: WrapObj[S]): Boolean = js.native

  @JSName("componentWillUpdate")
  private[react] def _componentWillUpdate(nextProps: WrapObj[P], nextState: WrapObj[S]): Unit = js.native

  @JSName("componentDidUpdate")
  private[react] def _componentDidUpdate(prevProps: WrapObj[P], prevState: WrapObj[S]): Unit = js.native

  @JSName("props")
  private[react] def _props: WrapObj[P] with PropsMixedIn = js.native

  @JSName("setState")
  private[react] def _setState(s: WrapObj[S], callback: UndefOr[js.Function]): Unit = js.native

  @JSName("setState")
  private[react] def _modState(s: js.Function1[WrapObj[S], WrapObj[S]], callback: UndefOr[js.Function]): Unit = js.native
}

@ScalaJSDefined
abstract class BasicReactComponent[P, S, N <: TopNode] extends ReactJSComponent[P, S] {
  if (js.isUndefined(_state) || _state == null)
    _state = js.Dictionary[Any]("v" -> null).asInstanceOf[WrapObj[S]]

  def getRef[R <: Ref](ref: R) = ref(refs)

  def render(): ReactElement

  @JSName("_forceUpdate")
  def forceUpdate(callback: Callback = Callback.empty): Unit =
    _forceUpdate(callback.toJsCallback)

  @JSName("componentWillUpdate")
  override def _componentWillUpdate(nextProps: WrapObj[P], nextState: WrapObj[S]): Unit =
    componentWillUpdate(nextProps.v, nextState.v)

  @JSName("_componentWillUpdate")
  def componentWillUpdate(nextProps: => P, nextState: => S): Unit = ()

  @JSName("componentWillReceiveProps")
  override def _componentWillReceiveProps(nextProps: WrapObj[P]): Unit =
    componentWillReceiveProps(nextProps.v)

  @JSName("_componentWillReceiveProps")
  def componentWillReceiveProps(nextProps: => P): Unit = ()

  def children: PropsChildren = _props.children

  def propsDynamic = _props.asInstanceOf[js.Dynamic]
}

@ScalaJSDefined
abstract class ReactComponentNoProps[S, N <: TopNode] extends BasicReactComponent[Unit, S, N] {
  def initialState(): S

  @JSName("_state")
  def state: S = _state.v

  @JSName("_modState")
  def modState(func: (S) => S, callback: Callback = Callback.empty): Callback =
    CallbackTo(_modState((s: WrapObj[S]) => WrapObj(func(s.v)), callback.toJsCallback))

  @JSName("componentWillMount")
  def _componentWillMount(): Unit =
    (setState(initialState()) >> CallbackTo(componentWillMount())).runNow()

  @JSName("_setState")
  def setState(newState: S, callback: Callback = Callback.empty): Callback =
    CallbackTo(_setState(WrapObj(newState), callback.toJsCallback))

  @JSName("_componentWillMount")
  def componentWillMount(): Unit = ()
}

@ScalaJSDefined
abstract class ReactComponentNoState[P, N <: TopNode] extends BasicReactComponent[P, Unit, N] {
  @JSName("_props")
  def props: P = _props.v

  def componentWillMount(): Unit = ()
}

@ScalaJSDefined
abstract class ReactComponentNoPropsAndState[N <: react.TopNode] extends BasicReactComponent[Unit, Unit, N]

@ScalaJSDefined
abstract class ReactComponent[P, S, N <: react.TopNode] extends BasicReactComponent[P, S, N] {
  def initialState(props: P): S

  @JSName("_state")
  def state: S = _state.v

  @JSName("_modState")
  def modState(func: (S) => S, callback: Callback = Callback.empty): Callback =
    CallbackTo(_modState((s: WrapObj[S]) => WrapObj(func(s.v)), callback.toJsCallback))

  @JSName("componentWillMount")
  def _componentWillMount(): Unit =
    setState(initialState(props), CallbackTo(componentWillMount())).runNow()

  @JSName("_props")
  def props: P = _props.v

  @JSName("_setState")
  def setState(newState: S, callback: Callback = Callback.empty): Callback =
    CallbackTo(_setState(WrapObj(newState), callback.toJsCallback))

  @JSName("_componentWillMount")
  def componentWillMount(): Unit = ()
}
