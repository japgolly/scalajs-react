package japgolly.scalajs.react.hooks

import HookComponentBuilder._

trait StepBase {
  type Next[A]
}

// ==========================================================================================================================
// Step 1

final class StepFirst[P] extends StepBase {
  override type Next[H1] =
    DslMulti[P, HookCtx.P1[P, H1], HookCtxFn.P1[P, H1]#Fn]

  def apply[H1](hook1: P => H1): Next[H1] = {
    type Ctx = HookCtx.P1[P, H1]
    val render: RenderFn[P, Ctx] =
      f => Component(p => {
        val h1 = hook1(p)
        f(HookCtx(p, h1))
      })
    new DslMulti(render)
  }
}

object StepFirst {
  implicit def instance[P]: StepFirst[P] =
    new StepFirst
}

// ==========================================================================================================================
// Step 2+

trait StepMulti[P, _Ctx, _CtxFn[_]] extends StepBase {
  final type Ctx = _Ctx
  final type CtxFn[A] = _CtxFn[A]
  def next[A]: (RenderFn[P, Ctx], Ctx => A) => Next[A]
  def squash[A]: CtxFn[A] => (Ctx => A)
}

object StepMulti extends StepMultiInstances {
  type To[P, Ctx, CtxFn[_], N[_]] = StepMulti[P, Ctx, CtxFn] { type Next[A] = N[A] }
}