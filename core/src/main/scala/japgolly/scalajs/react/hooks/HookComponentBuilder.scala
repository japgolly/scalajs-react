package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.component.ScalaFn
import japgolly.scalajs.react.component.ScalaFn.Component
import japgolly.scalajs.react.internal.Box
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, CtorType, PropsChildren}

object HookComponentBuilder {

  def apply[P]: ComponentP.First[P] =
    new ComponentP.First(_ => ())

  // ===================================================================================================================
  // Component with Props

  object ComponentP {

    final class First[P](init: P => Unit) extends Api.Primary[P, FirstStep[P]] {

      override protected def self(f: P => Any)(implicit step: Step): step.Self =
        step.self(init, f)

      override protected def next[H](f: P => H)(implicit step: Step): step.Next[H] =
        step.next(init, f)

      def withPropsChildren: ComponentPC.First[P] =
        new ComponentPC.First(ctx => init(ctx.props))

      def render(f: P => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.None]): Component[P, s.CT] =
        ScalaFn(f)
    }

    type RenderFn[-P, +Ctx] = (Ctx => VdomNode) => P => VdomNode

    final class Subsequent[P, Ctx, CtxFn[_]](renderFn: RenderFn[P, Ctx]) extends Api.Secondary[Ctx, CtxFn, SubsequentStep[P, Ctx, CtxFn]] {

      override protected def self(f: Ctx => Any)(implicit step: Step): step.Self =
        step.self(renderFn, f)

      override protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H] =
        step.next[H](renderFn, f)

      def render(f: Ctx => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.None]): Component[P, s.CT] =
        ScalaFn(renderFn(f))

      def render(f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], Children.None]): Component[P, s.CT] =
        render(step.squash(f)(_))
    }

    object Subsequent extends ComponentP_SubsequentDsl

    final class FirstStep[P] extends Api.Step {
      override type Self     = First[P]
      override type Next[H1] = Subsequent[P, HookCtx.P1[P, H1], HookCtxFn.P1[P, H1]#Fn]

      def self(initFirst: P => Unit, f: P => Any): Self =
        new First[P](p => {
          initFirst(p)
          f(p)
        })

      def next[H1](initFirst: P => Unit, hook1: P => H1): Next[H1] = {
        type Ctx = HookCtx.P1[P, H1]
        val render: RenderFn[P, Ctx] =
          f => p => {
            initFirst(p)
            val h1 = hook1(p)
            f(HookCtx(p, h1))
          }
        new Subsequent[P, HookCtx.P1[P, H1], HookCtxFn.P1[P, H1]#Fn](render)
      }
    }

    implicit def firstStep[P]: FirstStep[P] =
      new FirstStep

    trait SubsequentStep[P, _Ctx, _CtxFn[_]] extends Api.SubsequentStep[_Ctx, _CtxFn] {
      final type Self = Subsequent[P, Ctx, CtxFn]
      final def self: (RenderFn[P, Ctx], Ctx => Any) => Self =
        (renderPrev, initNextHook) => {
          val renderNext: ComponentP.RenderFn[P, Ctx] =
            render => renderPrev { ctx =>
              initNextHook(ctx)
              render(ctx)
            }
          new ComponentP.Subsequent[P, Ctx, CtxFn](renderNext)
        }
      def next[A]: (RenderFn[P, Ctx], Ctx => A) => Next[A]
    }

    object SubsequentStep extends ComponentP_SubsequentSteps {
      type To[P, Ctx, CtxFn[_], N[_]] = SubsequentStep[P, Ctx, CtxFn] { type Next[A] = N[A] }
    }
  }

  // ===================================================================================================================
  // Component with Props and PropsChildren

  object ComponentPC {

    final class First[P](init: HookCtx.PC0[P] => Unit) extends Api.Primary[HookCtx.PC0[P], FirstStep[P]] {

      override protected def self(f: HookCtx.PC0[P] => Any)(implicit step: Step): step.Self =
        step.self(init, f)

      override protected def next[H](f: HookCtx.PC0[P] => H)(implicit step: Step): step.Next[H] =
        step.next(init, f)

      def render(f: HookCtx.PC0[P] => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
        ScalaFn.withChildren((p, pc) => f(HookCtx.withChildren(p, pc)))

      def render(f: (P, PropsChildren) => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
        ScalaFn.withChildren(f)
    }

    type RenderFn[-P, +Ctx] = (Ctx => VdomNode) => (P, PropsChildren) => VdomNode

    final class Subsequent[P, Ctx, CtxFn[_]](renderFn: RenderFn[P, Ctx]) extends Api.Secondary[Ctx, CtxFn, SubsequentStep[P, Ctx, CtxFn]] {

      override protected def self(f: Ctx => Any)(implicit step: Step): step.Self =
        step.self(renderFn, f)

      override protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H] =
        step.next[H](renderFn, f)

      def render(f: Ctx => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
        ScalaFn.withChildren(renderFn(f))

      def render(f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
        render(step.squash(f)(_))
    }

    object Subsequent extends ComponentPC_SubsequentDsl

    final class FirstStep[P] extends Api.Step {
      override type Self     = First[P]
      override type Next[H1] = Subsequent[P, HookCtx.PC1[P, H1], HookCtxFn.PC1[P, H1]#Fn]

      def self(initFirst: HookCtx.PC0[P] => Unit, f: HookCtx.PC0[P] => Any): Self =
        new First[P](ctx0 => {
          initFirst(ctx0)
          f(ctx0)
        })

      def next[H1](initFirst: HookCtx.PC0[P] => Unit, hook1: HookCtx.PC0[P] => H1): Next[H1] = {
        type Ctx = HookCtx.PC1[P, H1]
        val render: RenderFn[P, Ctx] =
          f => (p, pc) => {
            val ctx0 = HookCtx.withChildren(p, pc)
            initFirst(ctx0)
            val h1 = hook1(ctx0)
            f(HookCtx.withChildren(p, pc, h1))
          }
        new Subsequent[P, HookCtx.PC1[P, H1], HookCtxFn.PC1[P, H1]#Fn](render)
      }
    }

    implicit def firstStep[P]: FirstStep[P] =
      new FirstStep

    trait SubsequentStep[P, _Ctx, _CtxFn[_]] extends Api.SubsequentStep[_Ctx, _CtxFn] {
      final type Self = Subsequent[P, Ctx, CtxFn]
      final def self: (RenderFn[P, Ctx], Ctx => Any) => Self =
        (renderPrev, initNextHook) => {
          val renderNext: ComponentPC.RenderFn[P, Ctx] =
            render => renderPrev { ctx =>
              initNextHook(ctx)
              render(ctx)
            }
          new ComponentPC.Subsequent[P, Ctx, CtxFn](renderNext)
        }
      def next[A]: (RenderFn[P, Ctx], Ctx => A) => Next[A]
    }

    object SubsequentStep extends ComponentPC_SubsequentSteps {
      type To[P, Ctx, CtxFn[_], N[_]] = SubsequentStep[P, Ctx, CtxFn] { type Next[A] = N[A] }
    }
  }

}
