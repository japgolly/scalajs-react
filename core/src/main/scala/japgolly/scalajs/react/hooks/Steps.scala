package japgolly.scalajs.react.hooks

import HookComponentBuilder._

trait StepBase {
  type Next[A]
}

trait SubsequentStep[_Ctx, _CtxFn[_]] extends StepBase {
  final type Ctx = _Ctx
  final type CtxFn[A] = _CtxFn[A]
  def squash[A]: CtxFn[A] => (Ctx => A)
}

object Step {

  // ===================================================================================================================
  // Component with Props

  object ComponentP {
    import HookComponentBuilder.{ComponentP => H}

    final class First[P] extends StepBase {
      override type Next[H1] =
        H.Subsequent[P, HookCtx.P1[P, H1], HookCtxFn.P1[P, H1]#Fn]

      def apply[H1](hook1: P => H1): Next[H1] = {
        type Ctx = HookCtx.P1[P, H1]
        val render: H.RenderFn[P, Ctx] =
          f => Component((p, _) => {
            val h1 = hook1(p)
            f(HookCtx(p, h1))
          })
        new H.Subsequent[P, HookCtx.P1[P, H1], HookCtxFn.P1[P, H1]#Fn](render)
      }
    }

    implicit def first[P]: First[P] =
      new First

    trait Subsequent[P, _Ctx, _CtxFn[_]] extends SubsequentStep[_Ctx, _CtxFn] {
      def next[A]: (H.RenderFn[P, Ctx], Ctx => A) => Next[A]
      def squash[A]: CtxFn[A] => (Ctx => A)
    }

    object Subsequent extends ComponentP_SubsequentSteps {
      type To[P, Ctx, CtxFn[_], N[_]] = Subsequent[P, Ctx, CtxFn] { type Next[A] = N[A] }
    }
  }

  // ===================================================================================================================
  // Component with Props and PropsChildren

  object ComponentPC {
    import HookComponentBuilder.{ComponentPC => H}

    final class First[P] extends StepBase {
      override type Next[H1] =
        H.Subsequent[P, HookCtx.PC1[P, H1], HookCtxFn.PC1[P, H1]#Fn]

      def apply[H1](hook1: HookCtx.PC0[P] => H1): Next[H1] = {
        type Ctx = HookCtx.PC1[P, H1]
        val render: H.RenderFn[P, Ctx] =
          f => Component((p, pc) => {
            val h1 = hook1(HookCtx.withChildren(p, pc))
            f(HookCtx.withChildren(p, pc, h1))
          })
        new H.Subsequent[P, HookCtx.PC1[P, H1], HookCtxFn.PC1[P, H1]#Fn](render)
      }
    }

    implicit def first[P]: First[P] =
      new First

    trait Subsequent[P, _Ctx, _CtxFn[_]] extends SubsequentStep[_Ctx, _CtxFn] {
      def next[A]: (H.RenderFn[P, Ctx], Ctx => A) => Next[A]
      def squash[A]: CtxFn[A] => (Ctx => A)
    }

    object Subsequent extends ComponentPC_SubsequentSteps {
      type To[P, Ctx, CtxFn[_], N[_]] = Subsequent[P, Ctx, CtxFn] { type Next[A] = N[A] }
    }
  }

}
