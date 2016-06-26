package japgolly.scalajs.react

import japgolly.scalajs.react.internal._
import Lifecycle._
import ScalaComponent._
import org.scalajs.dom

final case class Lifecycle[P, S, B](
//    configureSpec            : js.UndefOr[ReactComponentSpec       [P, S, B] => Callback],
  componentDidMount        : Option[ComponentDidMountFn        [P, S, B]],
  componentDidUpdate       : Option[ComponentDidUpdateFn       [P, S, B]],
  componentWillMount       : Option[ComponentWillMountFn       [P, S, B]],
  componentWillReceiveProps: Option[ComponentWillReceivePropsFn[P, S, B]],
  componentWillUnmount     : Option[ComponentWillUnmountFn     [P, S, B]],
  componentWillUpdate      : Option[ComponentWillUpdateFn      [P, S, B]],
  shouldComponentUpdate    : Option[ShouldComponentUpdateFn    [P, S, B]]) {

  type This = Lifecycle[P, S, B]

  def append[I, O](lens: Lens[Lifecycle[P, S, B], Option[I => O]])(g: I => O)(implicit s: Semigroup[O]): This =
    lens.mod(o => Some(o.fold(g)(f => i => s.append(f(i), g(i)))))(this)
}

object Lifecycle {
  def empty[P, S, B]: Lifecycle[P, S, B] =
    new Lifecycle(None, None, None, None, None, None, None)

  // Reads are untyped
  //   - Safe because of implementation in builder (creating a new Callback on demand).
  //   - Preferred because use is easier. (TODO is it really?)

  // Writes are Callbacks
  //   - All state modification from within a component should return a Callback.
  //     Consistency, type-safe, protects API & future changes.

  // Missing from all below:
  //   - def isMounted: F[Boolean]
  //   - def withEffect[G[+_]](implicit t: Effect.Trans[F, G]): Props[G, P]
  //   - def mapProps[X](f: P => X): Mounted[F, X, S] =
  //   - def xmapState[X](f: S => X)(g: X => S): Mounted[F, P, X] =
  //   - def zoomState[X](get: S => X)(set: X => S => S): Mounted[F, P, X] =

  // ===================================================================================================================

  def componentDidMount[P, S, B] = Lens((_: Lifecycle[P, S, B]).componentDidMount)(n => _.copy(componentDidMount = n))

  type ComponentDidMountFn[P, S, B] = ComponentDidMount[P, S, B] => Callback

  final class ComponentDidMount[P, S, B](val raw: Vars[P, S, B]) extends AnyVal {
    def backend      : B             = raw.backend
    def props        : P             = raw.mounted.props
    def propsChildren: PropsChildren = raw.mounted.propsChildren
    def state        : S             = raw.mounted.state
    def getDOMNode   : dom.Element   = raw.mounted.getDOMNode

    def setState   (newState: S, cb: Callback = Callback.empty): Callback = raw.mountedCB.setState(newState, cb)
    def modState   (mod: S => S, cb: Callback = Callback.empty): Callback = raw.mountedCB.modState(mod, cb)
    def forceUpdate(cb: Callback = Callback.empty)             : Callback = raw.mountedCB.forceUpdate(cb)
  }

  // ===================================================================================================================

  def componentDidUpdate[P, S, B] = Lens((_: Lifecycle[P, S, B]).componentDidUpdate)(n => _.copy(componentDidUpdate = n))

  type ComponentDidUpdateFn[P, S, B] = ComponentDidUpdate[P, S, B] => Callback

  final class ComponentDidUpdate[P, S, B](val raw: Vars[P, S, B], val prevProps: P, val prevState: S) {
    def backend      : B             = raw.mounted.backend
    def propsChildren: PropsChildren = raw.mounted.propsChildren
    def currentProps : P             = raw.mounted.props
    def currentState : S             = raw.mounted.state
    def getDOMNode   : dom.Element   = raw.mounted.getDOMNode

    def setState   (newState: S, cb: Callback = Callback.empty): Callback = raw.mountedCB.setState(newState, cb)
    def modState   (mod: S => S, cb: Callback = Callback.empty): Callback = raw.mountedCB.modState(mod, cb)
    def forceUpdate(cb: Callback = Callback.empty)             : Callback = raw.mountedCB.forceUpdate(cb)
  }

  // ===================================================================================================================

  def componentWillMount[P, S, B] = Lens((_: Lifecycle[P, S, B]).componentWillMount)(n => _.copy(componentWillMount = n))

  type ComponentWillMountFn[P, S, B] = ComponentWillMount[P, S, B] => Callback

  final class ComponentWillMount[P, S, B](val raw: Vars[P, S, B]) extends AnyVal {
    def backend      : B             = raw.backend
    def props        : P             = raw.mounted.props
    def propsChildren: PropsChildren = raw.mounted.propsChildren
    def state        : S             = raw.mounted.state

    def setState   (newState: S, cb: Callback = Callback.empty): Callback = raw.mountedCB.setState(newState, cb)
    def modState   (mod: S => S, cb: Callback = Callback.empty): Callback = raw.mountedCB.modState(mod, cb)

    @deprecated("forceUpdate prohibited within the componentWillMount callback.", "")
    def forceUpdate(prohibited: Nothing): Nothing = ???

    // Nope
    // def getDOMNode   : dom.Element   = raw.mounted.getDOMNode
  }

  // ===================================================================================================================

  def componentWillUnmount[P, S, B] = Lens((_: Lifecycle[P, S, B]).componentWillUnmount)(n => _.copy(componentWillUnmount = n))

  type ComponentWillUnmountFn[P, S, B] = ComponentWillUnmount[P, S, B] => Callback

  final class ComponentWillUnmount[P, S, B](val raw: Vars[P, S, B]) extends AnyVal {
    def backend      : B             = raw.backend
    def props        : P             = raw.mounted.props
    def propsChildren: PropsChildren = raw.mounted.propsChildren
    def state        : S             = raw.mounted.state
    def getDOMNode   : dom.Element   = raw.mounted.getDOMNode

    @deprecated("setState prohibited within the componentWillUnmount callback.", "")
    def setState(prohibited: Nothing): Nothing = ???

    @deprecated("modState prohibited within the componentWillUnmount callback.", "")
    def modState(prohibited: Nothing): Nothing = ???

    @deprecated("forceUpdate prohibited within the componentWillUnmount callback.", "")
    def forceUpdate(prohibited: Nothing): Nothing = ???
  }

  // ===================================================================================================================

  def componentWillReceiveProps[P, S, B] = Lens((_: Lifecycle[P, S, B]).componentWillReceiveProps)(n => _.copy(componentWillReceiveProps = n))

  type ComponentWillReceivePropsFn[P, S, B] = ComponentWillReceiveProps[P, S, B] => Callback

  final class ComponentWillReceiveProps[P, S, B](val raw: Vars[P, S, B], val nextProps: P) {
    def backend      : B             = raw.backend
    def propsChildren: PropsChildren = raw.mounted.propsChildren
    def currentProps : P             = raw.mounted.props
    def state        : S             = raw.mounted.state
    def getDOMNode   : dom.Element   = raw.mounted.getDOMNode

    def setState   (newState: S, cb: Callback = Callback.empty): Callback = raw.mountedCB.setState(newState, cb)
    def modState   (mod: S => S, cb: Callback = Callback.empty): Callback = raw.mountedCB.modState(mod, cb)
    def forceUpdate(cb: Callback = Callback.empty)             : Callback = raw.mountedCB.forceUpdate(cb)
  }

  // ===================================================================================================================

  def componentWillUpdate[P, S, B] = Lens((_: Lifecycle[P, S, B]).componentWillUpdate)(n => _.copy(componentWillUpdate = n))

  type ComponentWillUpdateFn[P, S, B] = ComponentWillUpdate[P, S, B] => Callback

  final class ComponentWillUpdate[P, S, B](val raw: Mounted[P, S, B], val nextProps: P, val nextState: S) {
    @inline def backend      : B             = raw.backend
    @inline def propsChildren: PropsChildren = raw.propsChildren
    @inline def currentProps : P             = raw.props
    @inline def currentState : S             = raw.state
    @inline def getDOMNode   : dom.Element   = raw.getDOMNode

    @deprecated("setState prohibited within the componentWillUpdate callback. Use componentWillReceiveProps instead.", "")
    def setState(prohibited: Nothing): Nothing = ???

    @deprecated("modState prohibited within the componentWillUpdate callback. Use componentWillReceiveProps instead.", "")
    def modState(prohibited: Nothing): Nothing = ???

    @deprecated("forceUpdate prohibited within the componentWillUpdate callback. Use componentWillReceiveProps instead.", "")
    def forceUpdate(prohibited: Nothing): Nothing = ???
  }

  // ===================================================================================================================

  def shouldComponentUpdate[P, S, B] = Lens((_: Lifecycle[P, S, B]).shouldComponentUpdate)(n => _.copy(shouldComponentUpdate = n))

  type ShouldComponentUpdateFn[P, S, B] = ShouldComponentUpdate[P, S, B] => Boolean

  final class ShouldComponentUpdate[P, S, B](val raw: Mounted[P, S, B], val nextProps: P, val nextState: S) {
    @inline def backend      : B             = raw.backend
    @inline def propsChildren: PropsChildren = raw.propsChildren
    @inline def currentProps : P             = raw.props
    @inline def currentState : S             = raw.state
    @inline def getDOMNode   : dom.Element   = raw.getDOMNode

    @inline def cmpProps(cmp: (P, P) => Boolean): Boolean = cmp(currentProps, nextProps)
    @inline def cmpState(cmp: (S, S) => Boolean): Boolean = cmp(currentState, nextState)

    @deprecated("setState prohibited within the shouldComponentUpdate callback.", "")
    def setState(prohibited: Nothing): Nothing = ???

    @deprecated("modState prohibited within the shouldComponentUpdate callback.", "")
    def modState(prohibited: Nothing): Nothing = ???

    @deprecated("forceUpdate prohibited within the shouldComponentUpdate callback.", "")
    def forceUpdate(prohibited: Nothing): Nothing = ???
  }
}

