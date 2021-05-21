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
      f => new Component(p => {
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

trait DslMultiSteps { self: DslMulti.type =>
  sealed trait AtStep1[P, H1] { type Next[H2] = DslMulti[P, HookCtx.P2[P, H1, H2], HookCtxFn.P2[P, H1, H2]#Fn] }
  sealed trait AtStep2[P, H1, H2] { type Next[H3] = DslMulti[P, HookCtx.P3[P, H1, H2, H3], HookCtxFn.P3[P, H1, H2, H3]#Fn] }
}

trait StepMulti[P, _Ctx, _CtxFn[_]] extends StepBase {
  final type Ctx = _Ctx
  final type CtxFn[A] = _CtxFn[A]
  def next[A]: (RenderFn[P, Ctx], Ctx => A) => Next[A]
  def squash[A]: CtxFn[A] => (Ctx => A)
}

object StepMulti {
  type To[P, Ctx, CtxFn[_], N[_]] = StepMulti[P, Ctx, CtxFn] { type Next[A] = N[A] }

  type AtStep1[P, H1] = To[
    P,
    HookCtx.P1[P, H1],
    HookCtxFn.P1[P, H1]#Fn,
    DslMulti.AtStep1[P, H1]#Next]

  implicit def atStep1[P, H1]: AtStep1[P, H1] =
    new StepMulti[P, HookCtx.P1[P, H1], HookCtxFn.P1[P, H1]#Fn] {
      override type Next[A] = DslMulti.AtStep1[P, H1]#Next[A]
      override def next[H2] =
        (renderPrev, initNextHook) => {
          val renderNext: RenderFn[P, HookCtx.P2[P, H1, H2]] =
            render => renderPrev { ctx1 =>
              val h2 = initNextHook(ctx1)
              val ctx2 = HookCtx(ctx1.props, ctx1.hook1, h2)
              render(ctx2)
            }
          new DslMulti(renderNext)
        }
      override def squash[A] = f => _.apply1(f)
    }

  type AtStep2[P, H1, H2] = To[
    P,
    HookCtx.P2[P, H1, H2],
    HookCtxFn.P2[P, H1, H2]#Fn,
    DslMulti.AtStep2[P, H1, H2]#Next]

  implicit def atStep2[P, H1, H2]: AtStep2[P, H1, H2] =
    new StepMulti[P, HookCtx.P2[P, H1, H2], HookCtxFn.P2[P, H1, H2]#Fn] {
      override type Next[A] = DslMulti.AtStep2[P, H1, H2]#Next[A]
      override def next[H3] =
        (renderPrev, initNextHook) => {
          val renderNext: RenderFn[P, HookCtx.P3[P, H1, H2, H3]] =
            render => renderPrev { ctx2 =>
              val h3 = initNextHook(ctx2)
              val ctx3 = HookCtx(ctx2.props, ctx2.hook1, ctx2.hook2, h3)
              render(ctx3)
            }
          new DslMulti(renderNext)
        }
      override def squash[A] = f => _.apply2(f)
    }

}