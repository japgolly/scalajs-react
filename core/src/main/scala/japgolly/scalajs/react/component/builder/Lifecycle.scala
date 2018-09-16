package japgolly.scalajs.react.component.builder

import japgolly.scalajs.react.{Callback, CallbackTo, ComponentDom, PropsChildren, StateAccess}
import japgolly.scalajs.react.component.Scala._
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.raw.React
import Lifecycle._

final case class Lifecycle[P, S, B, SS](
      componentDidCatch        : Option[ComponentDidCatchFn        [P, S, B]],
      componentDidMount        : Option[ComponentDidMountFn        [P, S, B]],
      componentDidUpdate       : Option[ComponentDidUpdateFn       [P, S, B, SS]],
      componentWillMount       : Option[ComponentWillMountFn       [P, S, B]],
      componentWillReceiveProps: Option[ComponentWillReceivePropsFn[P, S, B]],
      componentWillUnmount     : Option[ComponentWillUnmountFn     [P, S, B]],
      componentWillUpdate      : Option[ComponentWillUpdateFn      [P, S, B]],
      getDerivedStateFromProps : Option[GetDerivedStateFromPropsFn [P, S]],
      getSnapshotBeforeUpdate  : Option[GetSnapshotBeforeUpdateFn  [P, S, B, SS]],
      shouldComponentUpdate    : Option[ShouldComponentUpdateFn    [P, S, B]]) {

  type This = Lifecycle[P, S, B, SS]

  def append[I, O](lens: Lens[Lifecycle[P, S, B, SS], Option[I => O]])(g: I => O)(implicit s: Semigroup[O]): This =
    lens.mod(o => Some(o.fold(g)(f => i => s.append(f(i), g(i)))))(this)

  def append2[I1, I2, O](lens: Lens[Lifecycle[P, S, B, SS], Option[(I1, I2) => O]])(g: (I1, I2) => O)(implicit s: Semigroup[O]): This =
    lens.mod(o => Some(o.fold(g)(f => (i1, i2) => s.append(f(i1, i2), g(i1, i2)))))(this)

  def resetSnapshot[SS2](componentDidUpdate     : Option[ComponentDidUpdateFn     [P, S, B, SS2]],
                         getSnapshotBeforeUpdate: Option[GetSnapshotBeforeUpdateFn[P, S, B, SS2]]): Lifecycle[P, S, B, SS2] =
    Lifecycle(
      componentDidCatch         = componentDidCatch        ,
      componentDidMount         = componentDidMount        ,
      componentDidUpdate        = componentDidUpdate       ,
      componentWillMount        = componentWillMount       ,
      componentWillReceiveProps = componentWillReceiveProps,
      componentWillUnmount      = componentWillUnmount     ,
      componentWillUpdate       = componentWillUpdate      ,
      getDerivedStateFromProps  = getDerivedStateFromProps ,
      getSnapshotBeforeUpdate   = getSnapshotBeforeUpdate  ,
      shouldComponentUpdate     = shouldComponentUpdate    )
}

object Lifecycle {
  type NoSnapshot = Unit

  def empty[P, S, B]: Lifecycle[P, S, B, NoSnapshot] =
    new Lifecycle(None, None, None, None, None, None, None, None, None, None)

  sealed trait Base[P, S, B] extends Any {
    def raw: RawMounted[P, S, B]

    final def backend      : B                      = raw.backend
    final def mountedImpure: MountedImpure[P, S, B] = raw.mountedImpure
    final def mountedPure  : MountedPure[P, S, B]   = raw.mountedPure
  }

  sealed trait StateW[P, S, B] extends Any with Base[P, S, B] with StateAccess.WriteWithProps[CallbackTo, P, S] {
    /** @param callback Executed after state is changed. */
    final override def setState(newState: S, callback: Callback): Callback =
      mountedPure.setState(newState, callback)

    /** @param callback Executed after state is changed. */
    final override def modState(mod: S => S, callback: Callback): Callback =
      mountedPure.modState(mod, callback)

    /** @param callback Executed after state is changed. */
    final override def modState(mod: (S, P) => S, callback: Callback): Callback =
      mountedPure.modState(mod, callback)

    /** @param callback Executed regardless of whether state is changed. */
    final override def setStateOption(newState: Option[S], callback: Callback): Callback =
      mountedPure.setStateOption(newState, callback)

    /** @param callback Executed regardless of whether state is changed. */
    final override def modStateOption(mod: S => Option[S], callback: Callback): Callback =
      mountedPure.modStateOption(mod, callback)

    /** @param callback Executed regardless of whether state is changed. */
    final override def modStateOption(mod: (S, P) => Option[S], callback: Callback): Callback =
      mountedPure.modStateOption(mod, callback)
  }

  sealed trait StateRW[P, S, B] extends Any with StateW[P, S, B] {
    final def state: S = mountedImpure.state
  }

  sealed trait ForceUpdate[P, S, B] extends Any with Base[P, S, B] {
    final def forceUpdate: Callback = forceUpdate(Callback.empty)
    final def forceUpdate(cb: Callback): Callback = mountedPure.forceUpdate(cb)
  }

  private def wrapTostring(toString: String) = toString
    .replaceAll("undefined → undefined", "undefined")
    .replace("props" +
      ": undefined, ", "")
    .replace("state: undefined)", ")")
    .replace(", )", ")")

  // ===================================================================================================================

  def componentDidCatch[P, S, B, SS] = Lens((_: Lifecycle[P, S, B, SS]).componentDidCatch)(n => _.copy(componentDidCatch = n))

  type ComponentDidCatchFn[P, S, B] = ComponentDidCatch[P, S, B] => Callback

  final class ComponentDidCatch[P, S, B](val raw: RawMounted[P, S, B], val error: React.Error, val info: React.ErrorInfo)
      extends StateRW[P, S, B] with ForceUpdate[P, S, B] {

    override def toString = wrapTostring(s"ComponentDidCatch($error)")

    def props        : P                    = mountedImpure.props
    def propsChildren: PropsChildren        = mountedImpure.propsChildren
    def getDOMNode   : ComponentDom.Mounted = mountedImpure.getDOMNode.asMounted()
  }

  // ===================================================================================================================

  def componentDidMount[P, S, B, SS] = Lens((_: Lifecycle[P, S, B, SS]).componentDidMount)(n => _.copy(componentDidMount = n))

  type ComponentDidMountFn[P, S, B] = ComponentDidMount[P, S, B] => Callback

  final class ComponentDidMount[P, S, B](val raw: RawMounted[P, S, B])
      extends AnyVal with StateRW[P, S, B] with ForceUpdate[P, S, B] {

    override def toString = wrapTostring(s"ComponentDidMount(props: $props, state: $state)")

    def props        : P                    = mountedImpure.props
    def propsChildren: PropsChildren        = mountedImpure.propsChildren
    def getDOMNode   : ComponentDom.Mounted = mountedImpure.getDOMNode.asMounted()
  }

  // ===================================================================================================================

  def componentDidUpdate[P, S, B, SS] = Lens((_: Lifecycle[P, S, B, SS]).componentDidUpdate)(n => _.copy(componentDidUpdate = n))

  type ComponentDidUpdateFn[P, S, B, SS] = ComponentDidUpdate[P, S, B, SS] => Callback

  final class ComponentDidUpdate[P, S, B, SS](val raw: RawMounted[P, S, B],
                                              val prevProps: P, val prevState: S, val snapshot: SS)
      extends StateW[P, S, B] with ForceUpdate[P, S, B] {

    override def toString = wrapTostring(s"ComponentDidUpdate(props: $prevProps → $currentProps, state: $prevState → $currentState)")

    def propsChildren: PropsChildren        = mountedImpure.propsChildren
    def currentProps : P                    = mountedImpure.props
    def currentState : S                    = mountedImpure.state
    def getDOMNode   : ComponentDom.Mounted = mountedImpure.getDOMNode.asMounted()
  }

  // ===================================================================================================================

  def componentWillMount[P, S, B, SS] = Lens((_: Lifecycle[P, S, B, SS]).componentWillMount)(n => _.copy(componentWillMount = n))

  type ComponentWillMountFn[P, S, B] = ComponentWillMount[P, S, B] => Callback

  final class ComponentWillMount[P, S, B](val raw: RawMounted[P, S, B])
      extends AnyVal with StateRW[P, S, B] {

    override def toString = wrapTostring(s"ComponentWillMount(props: $props, state: $state)")

    def props        : P             = mountedImpure.props
    def propsChildren: PropsChildren = mountedImpure.propsChildren

    @deprecated("forceUpdate prohibited within the componentWillMount callback.", "")
    def forceUpdate(nope: NotAllowed) = NotAllowed.body

    // Nope
    // def getDOMNode   : dom.Element   = raw.mounted.getDOMNode
  }

  // ===================================================================================================================

  def componentWillUnmount[P, S, B, SS] = Lens((_: Lifecycle[P, S, B, SS]).componentWillUnmount)(n => _.copy(componentWillUnmount = n))

  type ComponentWillUnmountFn[P, S, B] = ComponentWillUnmount[P, S, B] => Callback

  final class ComponentWillUnmount[P, S, B](val raw: RawMounted[P, S, B])
      extends AnyVal with Base[P, S, B] {

    override def toString = wrapTostring(s"ComponentWillUnmount(props: $props, state: $state)")

    def props        : P                    = mountedImpure.props
    def propsChildren: PropsChildren        = mountedImpure.propsChildren
    def state        : S                    = mountedImpure.state
    def getDOMNode   : ComponentDom.Mounted = mountedImpure.getDOMNode.asMounted()

    @deprecated("setState prohibited within the componentWillUnmount callback.", "")
    def setState(nope: NotAllowed, cb: Any = null) = NotAllowed.body

    @deprecated("modState prohibited within the componentWillUnmount callback.", "")
    def modState(nope: NotAllowed, cb: Any = null) = NotAllowed.body

    @deprecated("forceUpdate prohibited within the componentWillUnmount callback.", "")
    def forceUpdate(nope: NotAllowed) = NotAllowed.body
  }

  // ===================================================================================================================

  def componentWillReceiveProps[P, S, B, SS] = Lens((_: Lifecycle[P, S, B, SS]).componentWillReceiveProps)(n => _.copy(componentWillReceiveProps = n))

  type ComponentWillReceivePropsFn[P, S, B] = ComponentWillReceiveProps[P, S, B] => Callback

  final class ComponentWillReceiveProps[P, S, B](val raw: RawMounted[P, S, B], val nextProps: P)
      extends StateRW[P, S, B] with ForceUpdate[P, S, B] {

    override def toString = wrapTostring(s"ComponentWillReceiveProps(props: $currentProps → $nextProps, state: $state)")

    def propsChildren: PropsChildren        = mountedImpure.propsChildren
    def currentProps : P                    = mountedImpure.props
    def getDOMNode   : ComponentDom.Mounted = mountedImpure.getDOMNode.asMounted()
  }

  // ===================================================================================================================

  def componentWillUpdate[P, S, B, SS] = Lens((_: Lifecycle[P, S, B, SS]).componentWillUpdate)(n => _.copy(componentWillUpdate = n))

  type ComponentWillUpdateFn[P, S, B] = ComponentWillUpdate[P, S, B] => Callback

  final class ComponentWillUpdate[P, S, B](val raw: RawMounted[P, S, B], val nextProps: P, val nextState: S)
      extends Base[P, S, B] {

    override def toString = wrapTostring(s"ComponentWillUpdate(props: $currentProps → $nextProps, state: $currentState → $nextState)")

    def propsChildren: PropsChildren        = mountedImpure.propsChildren
    def currentProps : P                    = mountedImpure.props
    def currentState : S                    = mountedImpure.state
    def getDOMNode   : ComponentDom.Mounted = mountedImpure.getDOMNode.asMounted()

    @deprecated("setState prohibited within the componentWillUpdate callback. Use componentWillReceiveProps instead.", "")
    def setState(nope: NotAllowed, cb: Any = null) = NotAllowed.body

    @deprecated("modState prohibited within the componentWillUpdate callback. Use componentWillReceiveProps instead.", "")
    def modState(nope: NotAllowed, cb: Any = null) = NotAllowed.body

    @deprecated("forceUpdate prohibited within the componentWillUpdate callback. Use componentWillReceiveProps instead.", "")
    def forceUpdate(nope: NotAllowed) = NotAllowed.body
  }

  // ===================================================================================================================

  def getDerivedStateFromProps[P, S, B, SS] = Lens((_: Lifecycle[P, S, B, SS]).getDerivedStateFromProps)(n => _.copy(getDerivedStateFromProps = n))

  type GetDerivedStateFromPropsFn[P, S] = (P, S) => Option[S]

  // ===================================================================================================================

  def getSnapshotBeforeUpdate[P, S, B, SS] = Lens((_: Lifecycle[P, S, B, SS]).getSnapshotBeforeUpdate)(n => _.copy(getSnapshotBeforeUpdate = n))

  type GetSnapshotBeforeUpdateFn[P, S, B, SS] = GetSnapshotBeforeUpdate[P, S, B] => CallbackTo[SS]

  final class GetSnapshotBeforeUpdate[P, S, B](val raw: RawMounted[P, S, B], val prevProps: P, val prevState: S)
      extends Base[P, S, B] {

    override def toString = wrapTostring(s"GetSnapshotBeforeUpdate(props: $prevProps → $currentProps, state: $prevState → $currentState)")

    def propsChildren: PropsChildren        = mountedImpure.propsChildren
    def currentProps : P                    = mountedImpure.props
    def currentState : S                    = mountedImpure.state
    def getDOMNode   : ComponentDom.Mounted = mountedImpure.getDOMNode.asMounted()

    def cmpProps(cmp: (P, P) => Boolean): Boolean = cmp(currentProps, prevProps)
    def cmpState(cmp: (S, S) => Boolean): Boolean = cmp(currentState, prevState)

    @deprecated("setState prohibited within the getSnapshotBeforeUpdate callback.", "")
    def setState(nope: NotAllowed, cb: Any = null) = NotAllowed.body

    @deprecated("modState prohibited within the getSnapshotBeforeUpdate callback.", "")
    def modState(nope: NotAllowed, cb: Any = null) = NotAllowed.body

    @deprecated("forceUpdate prohibited within the getSnapshotBeforeUpdate callback.", "")
    def forceUpdate(nope: NotAllowed) = NotAllowed.body
  }

  // ===================================================================================================================

  def shouldComponentUpdate[P, S, B, SS] = Lens((_: Lifecycle[P, S, B, SS]).shouldComponentUpdate)(n => _.copy(shouldComponentUpdate = n))

  type ShouldComponentUpdateFn[P, S, B] = ShouldComponentUpdate[P, S, B] => CallbackTo[Boolean]

  final class ShouldComponentUpdate[P, S, B](val raw: RawMounted[P, S, B], val nextProps: P, val nextState: S)
      extends Base[P, S, B] {

    override def toString = wrapTostring(s"ShouldComponentUpdate(props: $currentProps → $nextProps, state: $currentState → $nextState)")

    def propsChildren: PropsChildren        = mountedImpure.propsChildren
    def currentProps : P                    = mountedImpure.props
    def currentState : S                    = mountedImpure.state
    def getDOMNode   : ComponentDom.Mounted = mountedImpure.getDOMNode.asMounted()

    def cmpProps(cmp: (P, P) => Boolean): Boolean = cmp(currentProps, nextProps)
    def cmpState(cmp: (S, S) => Boolean): Boolean = cmp(currentState, nextState)

    @deprecated("setState prohibited within the shouldComponentUpdate callback.", "")
    def setState(nope: NotAllowed, cb: Any = null) = NotAllowed.body

    @deprecated("modState prohibited within the shouldComponentUpdate callback.", "")
    def modState(nope: NotAllowed, cb: Any = null) = NotAllowed.body

    @deprecated("forceUpdate prohibited within the shouldComponentUpdate callback.", "")
    def forceUpdate(nope: NotAllowed) = NotAllowed.body
  }

  // ===================================================================================================================

  final class RenderScope[P, S, B](val raw: RawMounted[P, S, B])
      extends AnyVal with StateRW[P, S, B] with ForceUpdate[P, S, B] {

    override def toString = wrapTostring(s"Render(props: $props, state: $state)")

    def props        : P                    = mountedImpure.props
    def propsChildren: PropsChildren        = mountedImpure.propsChildren
    def getDOMNode   : ComponentDom.Mounted = mountedImpure.getDOMNode.asMounted()
  }

}
