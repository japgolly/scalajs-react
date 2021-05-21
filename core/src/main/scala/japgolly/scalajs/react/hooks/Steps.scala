package japgolly.scalajs.react.hooks

import HookComponentBuilder._

trait StepBase {
  type Next[A]
}

trait StepMulti[_Ctx, _CtxFn[_]] extends StepBase {
  final type Ctx = _Ctx
  final type CtxFn[A] = _CtxFn[A]
  def squash[A]: CtxFn[A] => (Ctx => A)
}

// ==========================================================================================================================
// [P] Step 1

final class StepFirstP[P] extends StepBase {
  override type Next[H1] =
    DslMultiP[P, HookCtx.P1[P, H1], HookCtxFn.P1[P, H1]#Fn]

  def apply[H1](hook1: P => H1): Next[H1] = {
    type Ctx = HookCtx.P1[P, H1]
    val render: RenderFnP[P, Ctx] =
      f => Component((p, _) => {
        val h1 = hook1(p)
        f(HookCtx(p, h1))
      })
    new DslMultiP[P, HookCtx.P1[P, H1], HookCtxFn.P1[P, H1]#Fn](render)
  }
}

object StepFirstP {
  implicit def instance[P]: StepFirstP[P] =
    new StepFirstP
}

// ==========================================================================================================================
// [P] Step 2+

trait StepMultiP[P, _Ctx, _CtxFn[_]] extends StepMulti[_Ctx, _CtxFn] {
  def next[A]: (RenderFnP[P, Ctx], Ctx => A) => Next[A]
  def squash[A]: CtxFn[A] => (Ctx => A)
}

object StepMultiP extends StepMultiPInstances {
  type To[P, Ctx, CtxFn[_], N[_]] = StepMultiP[P, Ctx, CtxFn] { type Next[A] = N[A] }
}

// ==========================================================================================================================
// [PC] Step 1

final class StepFirstPC[P] extends StepBase {
  override type Next[H1] =
    DslMultiPC[P, HookCtx.PC1[P, H1], HookCtxFn.PC1[P, H1]#Fn]

  def apply[H1](hook1: HookCtx.PC0[P] => H1): Next[H1] = {
    type Ctx = HookCtx.PC1[P, H1]
    val render: RenderFnPC[P, Ctx] =
      f => Component((p, pc) => {
        val h1 = hook1(HookCtx.withChildren(p, pc))
        f(HookCtx.withChildren(p, pc, h1))
      })
    new DslMultiPC[P, HookCtx.PC1[P, H1], HookCtxFn.PC1[P, H1]#Fn](render)
  }
}

object StepFirstPC {
  implicit def instance[P]: StepFirstPC[P] =
    new StepFirstPC
}

// ==========================================================================================================================
// [PC] Step 2+

trait StepMultiPC[P, _Ctx, _CtxFn[_]] extends StepMulti[_Ctx, _CtxFn] {
  def next[A]: (RenderFnPC[P, Ctx], Ctx => A) => Next[A]
  def squash[A]: CtxFn[A] => (Ctx => A)
}

object StepMultiPC extends StepMultiPCInstances {
  type To[P, Ctx, CtxFn[_], N[_]] = StepMultiPC[P, Ctx, CtxFn] { type Next[A] = N[A] }
}
