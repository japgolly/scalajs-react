package japgolly.scalajs.react.component.builder

import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.component.Scala.RawMounted
import japgolly.scalajs.react.component.builder.Lifecycle._
import japgolly.scalajs.react.facade.React
import japgolly.scalajs.react.internal.Lens
import japgolly.scalajs.react.util.Effect._
import japgolly.scalajs.react.util._
import japgolly.scalajs.react.{ComponentDom, PropsChildren, ReactCaughtError, StateAccess}
import scala.scalajs.LinkingInfo.developmentMode
import scala.scalajs.js

final case class Lifecycle[F[_], A[_], P, S, B, SS](
      componentDidCatch        : Option[ComponentDidCatchFn        [F, A, P, S, B]],
      componentDidMount        : Option[ComponentDidMountFn        [F, A, P, S, B]],
      componentDidUpdate       : Option[ComponentDidUpdateFn       [F, A, P, S, B, SS]],
      componentWillMount       : Option[ComponentWillMountFn       [F, A, P, S, B]],
      componentWillReceiveProps: Option[ComponentWillReceivePropsFn[F, A, P, S, B]],
      componentWillUnmount     : Option[ComponentWillUnmountFn     [F, A, P, S, B]],
      componentWillUpdate      : Option[ComponentWillUpdateFn      [F, A, P, S, B]],
      getDerivedStateFromProps : Option[GetDerivedStateFromPropsFn [   P, S]],
      getSnapshotBeforeUpdate  : Option[GetSnapshotBeforeUpdateFn  [F, A, P, S, B, SS]],
      shouldComponentUpdate    : Option[ShouldComponentUpdateFn    [F, A, P, S, B]]) {

  type This = Lifecycle[F, A, P, S, B, SS]

  def append[I, O](lens: Lens[Lifecycle[F, A, P, S, B, SS], Option[I => O]])(g: I => O)(implicit s: Semigroup[O]): This =
    lens.mod(o => Some(o.fold(g)(f => i => s.append(f(i), g(i)))))(this)

  def resetSnapshot[SS2](componentDidUpdate     : Option[ComponentDidUpdateFn     [F, A, P, S, B, SS2]],
                         getSnapshotBeforeUpdate: Option[GetSnapshotBeforeUpdateFn[F, A, P, S, B, SS2]]): Lifecycle[F, A, P, S, B, SS2] =
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

  def empty[F[_], A[_], P, S, B]: Lifecycle[F, A, P, S, B, NoSnapshot] =
    new Lifecycle(None, None, None, None, None, None, None, None, None, None)

  sealed abstract class Base[F[_], A[_], P, S, B](final val raw: RawMounted[P, S, B])(implicit f: Sync[F], a: Async[A]) {

    protected final implicit def F = f
    protected final implicit def A = a

    final def backend      : B                            = raw.backend
    final def mountedImpure: Scala.MountedImpure[P, S, B] = raw.mountedImpure
    final def mountedPure  : Scala.Mounted[F, A, P, S, B] = raw.mountedPure.withEffect(F).withAsyncEffect(A)
  }

  sealed trait StateW[F[_], A[_], P, S, B] extends StateAccess.WriteWithProps[F, A, P, S] { self: Base[F, A, P, S, B] =>

    /** @param callback Executed after state is changed. */
    final override def setState[G[_], B](newState: S, callback: => G[B])(implicit G: Sync[G]): F[Unit] =
      mountedPure.setState(newState, callback)

    /** @param callback Executed after state is changed. */
    final override def modState[G[_], B](mod: S => S, callback: => G[B])(implicit G: Sync[G]): F[Unit] =
      mountedPure.modState(mod, callback)

    /** @param callback Executed after state is changed. */
    final override def modState[G[_], B](mod: (S, P) => S, callback: => G[B])(implicit G: Sync[G]): F[Unit] =
      mountedPure.modState(mod, callback)

    /** @param callback Executed regardless of whether state is changed. */
    final override def setStateOption[G[_], B](newState: Option[S], callback: => G[B])(implicit G: Sync[G]): F[Unit] =
      mountedPure.setStateOption(newState, callback)

    /** @param callback Executed regardless of whether state is changed. */
    final override def modStateOption[G[_], B](mod: S => Option[S], callback: => G[B])(implicit G: Sync[G]): F[Unit] =
      mountedPure.modStateOption(mod, callback)

    /** @param callback Executed regardless of whether state is changed. */
    final override def modStateOption[G[_], B](mod: (S, P) => Option[S], callback: => G[B])(implicit G: Sync[G]): F[Unit] =
      mountedPure.modStateOption(mod, callback)
  }

  sealed trait StateRW[F[_], A[_], P, S, B] extends StateW[F, A, P, S, B] { self: Base[F, A, P, S, B] =>
    final def state: S = mountedImpure.state
  }

  sealed trait ForceUpdate[F[_], A[_], P, S, B] { self: Base[F, A, P, S, B] =>
    final def forceUpdate: F[Unit] =
      forceUpdate(Sync.empty)

    final def forceUpdate[G[_], B](callback: => G[B])(implicit G: Sync[G]): F[Unit] =
      mountedPure.forceUpdate(callback)
  }

  private def wrapTostring(toString: String) =
    if (developmentMode)
      toString
        .replaceAll("undefined → undefined", "undefined")
        .replace("props: undefined, ", "")
        .replace("state: undefined)", ")")
        .replace(", )", ")")
    else
      toString

  // ===================================================================================================================

  def componentDidCatch[F[_], A[_], P, S, B, SS] = Lens((_: Lifecycle[F, A, P, S, B, SS]).componentDidCatch)(n => _.copy(componentDidCatch = n))

  type ComponentDidCatchFn[F[_], A[_], P, S, B] = ComponentDidCatch[F, A, P, S, B] => F[Unit]

  final class ComponentDidCatch[F[_]: Sync, A[_]: Async, P, S, B](raw: RawMounted[P, S, B], rawError: js.Any, rawInfo: React.ErrorInfo)
      extends Base[F, A, P, S, B](raw) with StateRW[F, A, P, S, B] with ForceUpdate[F, A, P, S, B] {

    val error = ReactCaughtError(rawError, rawInfo)

    override def toString = wrapTostring(s"ComponentDidCatch(${error.rawErrorString})")

    def props        : P                    = mountedImpure.props
    def propsChildren: PropsChildren        = mountedImpure.propsChildren
    def getDOMNode   : ComponentDom.Mounted = mountedImpure.getDOMNode.asMounted()
  }

  // ===================================================================================================================

  def componentDidMount[F[_], A[_], P, S, B, SS] = Lens((_: Lifecycle[F, A, P, S, B, SS]).componentDidMount)(n => _.copy(componentDidMount = n))

  type ComponentDidMountFn[F[_], A[_], P, S, B] = ComponentDidMount[F, A, P, S, B] => F[Unit]

  final class ComponentDidMount[F[_]: Sync, A[_]: Async, P, S, B](raw: RawMounted[P, S, B])
      extends Base[F, A, P, S, B](raw) with StateRW[F, A, P, S, B] with ForceUpdate[F, A, P, S, B] {

    override def toString = wrapTostring(s"ComponentDidMount(props: $props, state: $state)")

    def props        : P                    = mountedImpure.props
    def propsChildren: PropsChildren        = mountedImpure.propsChildren
    def getDOMNode   : ComponentDom.Mounted = mountedImpure.getDOMNode.asMounted()
  }

  // ===================================================================================================================

  def componentDidUpdate[F[_], A[_], P, S, B, SS] = Lens((_: Lifecycle[F, A, P, S, B, SS]).componentDidUpdate)(n => _.copy(componentDidUpdate = n))

  type ComponentDidUpdateFn[F[_], A[_], P, S, B, SS] = ComponentDidUpdate[F, A, P, S, B, SS] => F[Unit]

  final class ComponentDidUpdate[F[_]: Sync, A[_]: Async, P, S, B, SS](raw: RawMounted[P, S, B], val prevProps: P, val prevState: S, val snapshot: SS)
      extends Base[F, A, P, S, B](raw) with StateW[F, A, P, S, B] with ForceUpdate[F, A, P, S, B] {

    override def toString = wrapTostring(s"ComponentDidUpdate(props: $prevProps → $currentProps, state: $prevState → $currentState)")

    def propsChildren: PropsChildren        = mountedImpure.propsChildren
    def currentProps : P                    = mountedImpure.props
    def currentState : S                    = mountedImpure.state
    def getDOMNode   : ComponentDom.Mounted = mountedImpure.getDOMNode.asMounted()
  }

  // ===================================================================================================================

  def componentWillMount[F[_], A[_], P, S, B, SS] = Lens((_: Lifecycle[F, A, P, S, B, SS]).componentWillMount)(n => _.copy(componentWillMount = n))

  type ComponentWillMountFn[F[_], A[_], P, S, B] = ComponentWillMount[F, A, P, S, B] => F[Unit]

  final class ComponentWillMount[F[_]: Sync, A[_]: Async, P, S, B](raw: RawMounted[P, S, B])
      extends Base[F, A, P, S, B](raw) with StateRW[F, A, P, S, B] {

    override def toString = wrapTostring(s"ComponentWillMount(props: $props, state: $state)")

    def props        : P             = mountedImpure.props
    def propsChildren: PropsChildren = mountedImpure.propsChildren

    @deprecated("forceUpdate prohibited within the componentWillMount callback.", "")
    def forceUpdate(no: NotAllowed) = no.result

    // Nope
    // def getDOMNode   : dom.Element   = raw.mounted.getDOMNode
  }

  // ===================================================================================================================

  def componentWillUnmount[F[_], A[_], P, S, B, SS] = Lens((_: Lifecycle[F, A, P, S, B, SS]).componentWillUnmount)(n => _.copy(componentWillUnmount = n))

  type ComponentWillUnmountFn[F[_], A[_], P, S, B] = ComponentWillUnmount[F, A, P, S, B] => F[Unit]

  final class ComponentWillUnmount[F[_]: Sync, A[_]: Async, P, S, B](raw: RawMounted[P, S, B])
      extends Base[F, A, P, S, B](raw) {

    override def toString = wrapTostring(s"ComponentWillUnmount(props: $props, state: $state)")

    def props        : P                    = mountedImpure.props
    def propsChildren: PropsChildren        = mountedImpure.propsChildren
    def state        : S                    = mountedImpure.state
    def getDOMNode   : ComponentDom.Mounted = mountedImpure.getDOMNode.asMounted()

    @deprecated("setState prohibited within the componentWillUnmount callback.", "")
    def setState(no: NotAllowed, cb: Any = null) = no.result

    @deprecated("modState prohibited within the componentWillUnmount callback.", "")
    def modState(no: NotAllowed, cb: Any = null) = no.result

    @deprecated("forceUpdate prohibited within the componentWillUnmount callback.", "")
    def forceUpdate(no: NotAllowed) = no.result
  }

  // ===================================================================================================================

  def componentWillReceiveProps[F[_], A[_], P, S, B, SS] = Lens((_: Lifecycle[F, A, P, S, B, SS]).componentWillReceiveProps)(n => _.copy(componentWillReceiveProps = n))

  type ComponentWillReceivePropsFn[F[_], A[_], P, S, B] = ComponentWillReceiveProps[F, A, P, S, B] => F[Unit]

  final class ComponentWillReceiveProps[F[_]: Sync, A[_]: Async, P, S, B](raw: RawMounted[P, S, B], val nextProps: P)
      extends Base[F, A, P, S, B](raw) with StateRW[F, A, P, S, B] with ForceUpdate[F, A, P, S, B] {

    override def toString = wrapTostring(s"ComponentWillReceiveProps(props: $currentProps → $nextProps, state: $state)")

    def propsChildren: PropsChildren        = mountedImpure.propsChildren
    def currentProps : P                    = mountedImpure.props
    def getDOMNode   : ComponentDom.Mounted = mountedImpure.getDOMNode.asMounted()
  }

  // ===================================================================================================================

  def componentWillUpdate[F[_], A[_], P, S, B, SS] = Lens((_: Lifecycle[F, A, P, S, B, SS]).componentWillUpdate)(n => _.copy(componentWillUpdate = n))

  type ComponentWillUpdateFn[F[_], A[_], P, S, B] = ComponentWillUpdate[F, A, P, S, B] => F[Unit]

  final class ComponentWillUpdate[F[_]: Sync, A[_]: Async, P, S, B](raw: RawMounted[P, S, B], val nextProps: P, val nextState: S)
      extends Base[F, A, P, S, B](raw) {

    override def toString = wrapTostring(s"ComponentWillUpdate(props: $currentProps → $nextProps, state: $currentState → $nextState)")

    def propsChildren: PropsChildren        = mountedImpure.propsChildren
    def currentProps : P                    = mountedImpure.props
    def currentState : S                    = mountedImpure.state
    def getDOMNode   : ComponentDom.Mounted = mountedImpure.getDOMNode.asMounted()

    @deprecated("setState prohibited within the componentWillUpdate callback. Use componentWillReceiveProps instead.", "")
    def setState(no: NotAllowed, cb: Any = null) = no.result

    @deprecated("modState prohibited within the componentWillUpdate callback. Use componentWillReceiveProps instead.", "")
    def modState(no: NotAllowed, cb: Any = null) = no.result

    @deprecated("forceUpdate prohibited within the componentWillUpdate callback. Use componentWillReceiveProps instead.", "")
    def forceUpdate(no: NotAllowed) = no.result
  }

  // ===================================================================================================================

  def getDerivedStateFromProps[F[_], A[_], P, S, B, SS] = Lens((_: Lifecycle[F, A, P, S, B, SS]).getDerivedStateFromProps)(n => _.copy(getDerivedStateFromProps = n))

  type GetDerivedStateFromPropsFn[P, S] = (P, S) => Option[S]

  // ===================================================================================================================

  def getSnapshotBeforeUpdate[F[_], A[_], P, S, B, SS] = Lens((_: Lifecycle[F, A, P, S, B, SS]).getSnapshotBeforeUpdate)(n => _.copy(getSnapshotBeforeUpdate = n))

  type GetSnapshotBeforeUpdateFn[F[_], A[_], P, S, B, SS] = GetSnapshotBeforeUpdate[F, A, P, S, B] => F[SS]

  final class GetSnapshotBeforeUpdate[F[_]: Sync, A[_]: Async, P, S, B](raw: RawMounted[P, S, B], val prevProps: P, val prevState: S)
      extends Base[F, A, P, S, B](raw) {

    override def toString = wrapTostring(s"GetSnapshotBeforeUpdate(props: $prevProps → $currentProps, state: $prevState → $currentState)")

    def propsChildren: PropsChildren        = mountedImpure.propsChildren
    def currentProps : P                    = mountedImpure.props
    def currentState : S                    = mountedImpure.state
    def getDOMNode   : ComponentDom.Mounted = mountedImpure.getDOMNode.asMounted()

    def cmpProps(cmp: (P, P) => Boolean): Boolean = cmp(currentProps, prevProps)
    def cmpState(cmp: (S, S) => Boolean): Boolean = cmp(currentState, prevState)

    @deprecated("setState prohibited within the getSnapshotBeforeUpdate callback.", "")
    def setState(no: NotAllowed, cb: Any = null) = no.result

    @deprecated("modState prohibited within the getSnapshotBeforeUpdate callback.", "")
    def modState(no: NotAllowed, cb: Any = null) = no.result

    @deprecated("forceUpdate prohibited within the getSnapshotBeforeUpdate callback.", "")
    def forceUpdate(no: NotAllowed) = no.result
  }

  // ===================================================================================================================

  def shouldComponentUpdate[F[_], A[_], P, S, B, SS] = Lens((_: Lifecycle[F, A, P, S, B, SS]).shouldComponentUpdate)(n => _.copy(shouldComponentUpdate = n))

  type ShouldComponentUpdateFn[F[_], A[_], P, S, B] = ShouldComponentUpdate[F, A, P, S, B] => F[Boolean]

  final class ShouldComponentUpdate[F[_]: Sync, A[_]: Async, P, S, B](raw: RawMounted[P, S, B], val nextProps: P, val nextState: S)
      extends Base[F, A, P, S, B](raw) {

    override def toString = wrapTostring(s"ShouldComponentUpdate(props: $currentProps → $nextProps, state: $currentState → $nextState)")

    def propsChildren: PropsChildren        = mountedImpure.propsChildren
    def currentProps : P                    = mountedImpure.props
    def currentState : S                    = mountedImpure.state
    def getDOMNode   : ComponentDom.Mounted = mountedImpure.getDOMNode.asMounted()

    def cmpProps(cmp: (P, P) => Boolean): Boolean = cmp(currentProps, nextProps)
    def cmpState(cmp: (S, S) => Boolean): Boolean = cmp(currentState, nextState)

    @deprecated("setState prohibited within the shouldComponentUpdate callback.", "")
    def setState(no: NotAllowed, cb: Any = null) = no.result

    @deprecated("modState prohibited within the shouldComponentUpdate callback.", "")
    def modState(no: NotAllowed, cb: Any = null) = no.result

    @deprecated("forceUpdate prohibited within the shouldComponentUpdate callback.", "")
    def forceUpdate(no: NotAllowed) = no.result
  }

  // ===================================================================================================================

  final class RenderScope[F[_]: Sync, A[_]: Async, P, S, B](raw: RawMounted[P, S, B])
      extends Base[F, A, P, S, B](raw) with StateRW[F, A, P, S, B] with ForceUpdate[F, A, P, S, B] {

    override def toString = wrapTostring(s"Render(props: $props, state: $state)")

    def props        : P                    = mountedImpure.props
    def propsChildren: PropsChildren        = mountedImpure.propsChildren
    def getDOMNode   : ComponentDom.Mounted = mountedImpure.getDOMNode.asMounted()
  }

}
