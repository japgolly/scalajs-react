package japgolly.scalajs.react

import org.scalajs.dom.raw.HTMLElement

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSName, ScalaJSDefined}
import scala.scalajs.js.{UndefOr, undefined}

@js.native
trait JSProps[P] extends js.Object {
  def key: UndefOr[String] = js.native

  def ref: UndefOr[js.Function] = js.native

  def sprops: P = js.native

  def children: PropsChildren = js.native
}

object JSProps {
  def apply[P](key: UndefOr[String] = undefined,
               ref: UndefOr[js.Function] = undefined,
               sprops: P) = {
    val dict = js.Dictionary[Any]("sprops" -> sprops)
    key.foreach(v => dict.update("key", v))
    ref.foreach(v => dict.update("ref", v))
    dict.asInstanceOf[JSProps[P]]
  }
}

@js.native
trait JSState[S] extends js.Object {
  var sstate: S = js.native
}

object JSState {
  def apply[S](sstate: S) = js.Dictionary("sstate" -> sstate).asInstanceOf[JSState[S]]
}

@js.native // unmounted react element
trait ReactElementU[P, S] extends ReactComponent[P, S] with ReactElement

@js.native // mounted react element
trait ReactElementM[P, S] extends ReactComponent[P, S] with ReactElement


@js.native
@JSName("React.Component")
class ReactJSComponent[P, S] extends js.Object {

  @JSName("props") private[react] var jsProps: JSProps[P] = js.native

  @JSName("state") private[react] var jsState: JSState[S] = js.native

  var refs: js.Dynamic = js.native

  var context: js.Dynamic = js.native

  @JSName("setState") def jsSetState(newState: JSState[S], callback: UndefOr[() => _] = js.undefined): Unit = js.native

  @JSName("setState") def jsSetState(func: js.Function2[JSState[S], JSProps[P], JSState[S]]): Unit = js.native

  def forceUpdate(callback: js.Function = ???): Unit = js.native

  def componentWillMount(): Unit = js.native

  def componentDidMount(): Unit = js.native

  def componentWillUnmount(): Unit = js.native

  @JSName("componentWillReceiveProps") def jsComponentWillReceiveProps(nextProps: JSProps[P]): Unit = js.native

  @JSName("shouldComponentUpdate") def jsShouldComponentUpdate(nextProps: JSProps[P], nextState: JSState[S]): Boolean = js.native

  @JSName("componentWillUpdate") def jsComponentWillUpdate(nextProps: JSProps[P], nextState: JSState[S]): Unit = js.native

  @JSName("componentDidUpdate") def jsComponentDidUpdate(prevProps: JSProps[P], prevState: JSState[S]): Unit = js.native

}


@ScalaJSDefined
abstract class ReactComponent[P, S] extends ReactJSComponent[P, S] {

  if (js.isUndefined(jsState) || jsState == null) {
    jsState = js.Dictionary[Any]("sstate" -> null).asInstanceOf[JSState[S]]
  }

  @JSName("sProps")
  @inline
  def props: P = jsProps.sprops

  @JSName("sState")
  @inline
  def state: S = jsState.sstate

  @inline
  def propsDynamic = jsProps.asInstanceOf[js.Dynamic]

  @inline
  def children: PropsChildren = jsProps.children


  @inline
  def initialState(s: S): Unit = {
    jsState = JSState(s)
  }

  @JSName("sSetState")
  @inline
  def setState(newState: S, callback: UndefOr[() => _] = js.undefined): Unit = {
    jsSetState(JSState(newState), callback)
  }

  @JSName("sSetStateFunc")
  @inline
  def setState(func: js.Function2[S, P, S]): Unit = {
    jsSetState((s: JSState[S], p: JSProps[P]) => JSState[S](func(s.sstate, p.sprops)))
  }

  @inline
  def getRef[T](name: String, cls: Class[T]) = {
    refs.selectDynamic(name).asInstanceOf[T]
  }

  def render(): ReactElement

  @JSName("sComponentWillUpdate")
  def componentWillUpdate(nextProps: => P, nextState: => S): Unit = ()

  @JSName("componentWillUpdate")
  override def jsComponentWillUpdate(nextProps: JSProps[P], nextState: JSState[S]): Unit = {
    componentWillUpdate(nextProps.sprops, nextState.sstate)
  }

  @JSName("sComponentWillReceiveProps")
  def componentWillReceiveProps(nextProps: => P): Unit = ()

  @JSName("componentWillReceiveProps")
  override def jsComponentWillReceiveProps(nextProps: JSProps[P]): Unit = {
    componentWillReceiveProps(nextProps.sprops)
  }

}

@js.native
trait ReactComponentFactory[P, S] extends ReactComponent[P, S] {
  def apply(props: js.Any, children: ReactNode*): ReactElementU[P, S] = js.native
}

@js.native
trait ReactComponentConstructor[P, S, C <: ReactComponent[P,S]] extends ReactClass[P, S, Unit, HTMLElement]

