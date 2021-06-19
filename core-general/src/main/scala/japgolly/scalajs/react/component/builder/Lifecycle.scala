package japgolly.scalajs.react.component.builder

import japgolly.scalajs.react.util.DefaultEffects.{Async => DA, Sync => DS}

object Lifecycle {
  type Lifecycle  [P, S, B, SS] = LifecycleF            [DS, DA, P, S, B, SS]
  type Base       [P, S, B]     = LifecycleF.Base       [DS, DA, P, S, B]
  type ForceUpdate[P, S, B]     = LifecycleF.ForceUpdate[DS, DA, P, S, B]
  type StateRW    [P, S, B]     = LifecycleF.StateRW    [DS, DA, P, S, B]
  type StateW     [P, S, B]     = LifecycleF.StateW     [DS, DA, P, S, B]

  @inline private[builder] def empty[P, S, B] = LifecycleF.empty[DS, DA, P, S, B]

  type ComponentDidCatch        [P, S, B]     = LifecycleF.ComponentDidCatch        [DS, DA, P, S, B]
  type ComponentDidMount        [P, S, B]     = LifecycleF.ComponentDidMount        [DS, DA, P, S, B]
  type ComponentDidUpdate       [P, S, B, SS] = LifecycleF.ComponentDidUpdate       [DS, DA, P, S, B, SS]
  type ComponentWillMount       [P, S, B]     = LifecycleF.ComponentWillMount       [DS, DA, P, S, B]
  type ComponentWillReceiveProps[P, S, B]     = LifecycleF.ComponentWillReceiveProps[DS, DA, P, S, B]
  type ComponentWillUnmount     [P, S, B]     = LifecycleF.ComponentWillUnmount     [DS, DA, P, S, B]
  type ComponentWillUpdate      [P, S, B]     = LifecycleF.ComponentWillUpdate      [DS, DA, P, S, B]
  type GetSnapshotBeforeUpdate  [P, S, B]     = LifecycleF.GetSnapshotBeforeUpdate  [DS, DA, P, S, B]
  type NoSnapshot                             = LifecycleF.NoSnapshot
  type RenderScope              [P, S, B]     = LifecycleF.RenderScope              [DS, DA, P, S, B]
  type ShouldComponentUpdate    [P, S, B]     = LifecycleF.ShouldComponentUpdate    [DS, DA, P, S, B]
}
