package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, PropsChildren}

object HookComponentBuilder {

  case class Component[-P, C <: Children](value: (P, PropsChildren) => VdomNode) // TODO: Temp

  def apply[P]: ComponentP.First[P] =
    new ComponentP.First

  // ===================================================================================================================
  // Component with Props

  object ComponentP {

    final class First[P] extends Api.Primary[P, FirstStep[P]] {

      override protected def next[H](f: P => H)(implicit step: Step): step.Next[H] =
        step(f)

      def withPropsChildren: ComponentPC.First[P] =
        new ComponentPC.First

      def render(f: P => VdomNode): Component[P, Children.None] =
        Component((p, _) => f(p))
    }

    type RenderFn[-P, +Ctx] = (Ctx => VdomNode) => Component[P, Children.None]

    final class Subsequent[P, Ctx, CtxFn[_]](renderFn: RenderFn[P, Ctx]) extends Api.Secondary[Ctx, CtxFn, SubsequentStep[P, Ctx, CtxFn]] {

      protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H] =
        step.next[H](renderFn, f)

      def render(f: Ctx => VdomNode): Component[P, Children.None] =
        renderFn(f)

      def render(f: CtxFn[VdomNode])(implicit step: Step): Component[P, Children.None] =
        render(step.squash(f)(_))
    }

    object Subsequent extends ComponentP_SubsequentDsl

    final class FirstStep[P] extends Api.Step {
      override type Next[H1] =
        Subsequent[P, HookCtx.P1[P, H1], HookCtxFn.P1[P, H1]#Fn]

      def apply[H1](hook1: P => H1): Next[H1] = {
        type Ctx = HookCtx.P1[P, H1]
        val render: RenderFn[P, Ctx] =
          f => Component((p, _) => {
            val h1 = hook1(p)
            f(HookCtx(p, h1))
          })
        new Subsequent[P, HookCtx.P1[P, H1], HookCtxFn.P1[P, H1]#Fn](render)
      }
    }

    implicit def firstStep[P]: FirstStep[P] =
      new FirstStep

    trait SubsequentStep[P, _Ctx, _CtxFn[_]] extends Api.SubsequentStep[_Ctx, _CtxFn] {
      def next[A]: (RenderFn[P, Ctx], Ctx => A) => Next[A]
      def squash[A]: CtxFn[A] => (Ctx => A)
    }

    object SubsequentStep extends ComponentP_SubsequentSteps {
      type To[P, Ctx, CtxFn[_], N[_]] = SubsequentStep[P, Ctx, CtxFn] { type Next[A] = N[A] }
    }
  }

  // ===================================================================================================================
  // Component with Props and PropsChildren

  object ComponentPC {

    final class First[P] extends Api.Primary[HookCtx.PC0[P], FirstStep[P]] {

      override protected def next[H](f: HookCtx.PC0[P] => H)(implicit step: Step): step.Next[H] =
        step(f)

      def render(f: HookCtx.PC0[P] => VdomNode): Component[P, Children.Varargs] =
        Component((p, pc) => f(HookCtx.withChildren(p, pc)))

      def render(f: (P, PropsChildren) => VdomNode): Component[P, Children.Varargs] =
        Component(f)
    }

    type RenderFn[-P, +Ctx] = (Ctx => VdomNode) => Component[P, Children.Varargs]

    final class Subsequent[P, Ctx, CtxFn[_]](renderFn: RenderFn[P, Ctx]) extends Api.Secondary[Ctx, CtxFn, SubsequentStep[P, Ctx, CtxFn]] {

      protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H] =
        step.next[H](renderFn, f)

      def render(f: Ctx => VdomNode): Component[P, Children.Varargs] =
        renderFn(f)

      def render(f: CtxFn[VdomNode])(implicit step: Step): Component[P, Children.Varargs] =
        render(step.squash(f)(_))
    }

    object Subsequent extends ComponentPC_SubsequentDsl

    final class FirstStep[P] extends Api.Step {
      override type Next[H1] =
        Subsequent[P, HookCtx.PC1[P, H1], HookCtxFn.PC1[P, H1]#Fn]

      def apply[H1](hook1: HookCtx.PC0[P] => H1): Next[H1] = {
        type Ctx = HookCtx.PC1[P, H1]
        val render: RenderFn[P, Ctx] =
          f => Component((p, pc) => {
            val h1 = hook1(HookCtx.withChildren(p, pc))
            f(HookCtx.withChildren(p, pc, h1))
          })
        new Subsequent[P, HookCtx.PC1[P, H1], HookCtxFn.PC1[P, H1]#Fn](render)
      }
    }

    implicit def firstStep[P]: FirstStep[P] =
      new FirstStep

    trait SubsequentStep[P, _Ctx, _CtxFn[_]] extends Api.SubsequentStep[_Ctx, _CtxFn] {
      def next[A]: (RenderFn[P, Ctx], Ctx => A) => Next[A]
      def squash[A]: CtxFn[A] => (Ctx => A)
    }

    object SubsequentStep extends ComponentPC_SubsequentSteps {
      type To[P, Ctx, CtxFn[_], N[_]] = SubsequentStep[P, Ctx, CtxFn] { type Next[A] = N[A] }
    }
  }

}
