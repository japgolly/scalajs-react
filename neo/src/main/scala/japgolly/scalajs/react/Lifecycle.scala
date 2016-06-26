package japgolly.scalajs.react

import japgolly.scalajs.react.internal._
import Lifecycle._
import ScalaComponent._
import org.scalajs.dom

final case class Lifecycle[P, S, B](
//    configureSpec            : js.UndefOr[ReactComponentSpec       [P, S, B] => Callback],
//    getDefaultProps          : js.UndefOr[                                      CallbackTo[P]],
//    componentWillMount       : js.UndefOr[DuringCallbackU          [P, S, B] => Callback],
  componentDidMount        : Option[ComponentDidMountFn[P, S, B]],
//    componentWillUnmount     : js.UndefOr[DuringCallbackM          [P, S, B] => Callback],
//    componentWillUpdate      : js.UndefOr[ComponentWillUpdate      [P, S, B] => Callback],
//    componentDidUpdate       : js.UndefOr[ComponentDidUpdate       [P, S, B] => Callback],
//    componentWillReceiveProps: js.UndefOr[ComponentWillReceiveProps[P, S, B] => Callback],
  shouldComponentUpdate    : Option[ShouldComponentUpdateFn[P, S, B]]) {

  type This = Lifecycle[P, S, B]

  def append[I, O](lens: Lens[Lifecycle[P, S, B], Option[I => O]])(g: I => O)(implicit s: Semigroup[O]): This =
    lens.mod(o => Some(o.fold(g)(f => i => s.append(f(i), g(i)))))(this)
}

object Lifecycle {
  def empty[P, S, B]: Lifecycle[P, S, B] =
    new Lifecycle(None, None)

  // Reads are untyped
  //   - Safe because of implementation in builder (creating a new Callback on demand).
  //   - Preferred because use is easier. (TODO is it really?)

  // Writes are Callbacks
  //   - All state modification from within a component should return a Callback.
  //     Consistency, type-safe, protects API & future changes.

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

//    def isMounted: F[Boolean]
//    def withEffect[G[+_]](implicit t: Effect.Trans[F, G]): Props[G, P]
//    def mapProps[X](f: P => X): Mounted[F, X, S] =
//    def xmapState[X](f: S => X)(g: X => S): Mounted[F, P, X] =
//    def zoomState[X](get: S => X)(set: X => S => S): Mounted[F, P, X] =
  }

  // ===================================================================================================================

  def shouldComponentUpdate[P, S, B] = Lens((_: Lifecycle[P, S, B]).shouldComponentUpdate)(n => _.copy(shouldComponentUpdate = n))

  type ShouldComponentUpdateFn[P, S, B] = ShouldComponentUpdate[P, S, B] => Boolean

  final class ShouldComponentUpdate[P, S, B](val $: Mounted[P, S, B], val nextProps: P, val nextState: S) {
    @inline final def component        = $
    @inline final def backend          = $.backend
    @inline final def propsChildren    = $.propsChildren
    @inline final def currentProps : P = $.props
    @inline final def currentState : S = $.state

    @inline def cmpProps(cmp: (P, P) => Boolean): Boolean = cmp(currentProps, nextProps)
    @inline def cmpState(cmp: (S, S) => Boolean): Boolean = cmp(currentState, nextState)
  }

//  sealed abstract class LifecycleInput[P, S, +$ <: HasProps[P] with HasState[S]] {
//    val $: $
//    @inline final def component: $ = $
//    @inline final def currentProps: P = $._props.v
//    @inline final def currentState: S = $._state.v
//  }
//  final case class ComponentWillUpdate      [P, S, +B]($: WillUpdate[P, S, B],      nextProps: P, nextState: S) extends LifecycleInput[P, S, WillUpdate[P, S, B]]
//  final case class ComponentDidUpdate       [P, S, +B]($: DuringCallbackM[P, S, B], prevProps: P, prevState: S) extends LifecycleInput[P, S, DuringCallbackM[P, S, B]]
//  final case class ShouldComponentUpdate    [P, S, +B]($: DuringCallbackM[P, S, B], nextProps: P, nextState: S) extends LifecycleInput[P, S, DuringCallbackM[P, S, B]]
//  final case class ComponentWillReceiveProps[P, S, +B]($: DuringCallbackM[P, S, B], nextProps: P) extends LifecycleInput[P, S, DuringCallbackM[P, S, B]]

}

