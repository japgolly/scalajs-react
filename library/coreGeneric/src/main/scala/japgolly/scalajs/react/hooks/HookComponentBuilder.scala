package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.component.ScalaFn
import japgolly.scalajs.react.component.ScalaFn.Component
import japgolly.scalajs.react.internal.Box
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, CtorType, PropsChildren, Reusable, Reusability}

object HookComponentBuilder {

  def apply[P]: ComponentP.First[P] =
    new ComponentP.First(_ => ())

  // ===================================================================================================================
  // Component with Props

  object ComponentP {

    final class First[P](init: P => Unit) extends Api.PrimaryWithRender[P, Children.None, P, FirstStep[P]] {

      override protected def self(f: P => Any)(implicit step: Step): step.Self =
        step.self(init, f)

      override protected def next[H](f: P => H)(implicit step: Step): step.Next[H] =
        step.next(init, f)

      def withPropsChildren: ComponentPC.First[P] =
        new ComponentPC.First(ctx => init(ctx.props))

      override def render(f: P => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.None]): Component[P, s.CT] =
        ScalaFn(f)

      override def renderWithReuseBy[A](reusableInputs: P => A)(f: A => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.None], r: Reusability[A]): Component[P, s.CT] =
        customBy(p => CustomHook.shouldComponentUpdate(f).apply(() => reusableInputs(p)))
          .render((_: HookCtx.P1[P, VdomNode]).hook1)
    }

    type RenderFn[-P, +Ctx] = (Ctx => VdomNode) => P => VdomNode

    final class Subsequent[P, Ctx, CtxFn[_]](renderFn: RenderFn[P, Ctx]) extends Api.SecondaryWithRender[P, Children.None, Ctx, CtxFn, SubsequentStep[P, Ctx, CtxFn]] {

      override protected def self(f: Ctx => Any)(implicit step: Step): step.Self =
        step.self(renderFn, f)

      override protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H] =
        step.next[H](renderFn, f)

      override def render(f: Ctx => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.None]): Component[P, s.CT] =
        ScalaFn(renderFn(f))

      override def renderWithReuseBy[A](reusableInputs: Ctx => A)(f: A => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.None], r: Reusability[A]): Component[P, s.CT] = {
        val hook = CustomHook.shouldComponentUpdate(f)
        ScalaFn(renderFn { ctx =>
          hook.unsafeInit(() => reusableInputs(ctx))
        })
      }
    }

    object Subsequent extends ComponentP_SubsequentDsl

    final class FirstStep[P] extends Api.AbstractStep {
      override type Self     = First[P]
      override type Next[H1] = Subsequent[P, HookCtx.P1[P, H1], ({ type F[A] = (P, H1) => A})#F]

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
        new Subsequent[P, HookCtx.P1[P, H1], ({ type F[A] = (P, H1) => A})#F](render)
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

    final class First[P](init: HookCtx.PC0[P] => Unit) extends Api.PrimaryWithRender[P, Children.Varargs, HookCtx.PC0[P], FirstStep[P]] with ComponentPCMacros[P] {
      type Ctx = HookCtx.PC0[P]

      override protected def self(f: Ctx => Any)(implicit step: Step): step.Self =
        step.self(init, f)

      override protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H] =
        step.next(init, f)

      override def render(f: Ctx => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
        ScalaFn.withChildren((p: P, pc: PropsChildren) => f(HookCtx.withChildren(p, pc)))

      override def renderWithReuseBy[A](reusableInputs: Ctx => A)(f: A => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs], r: Reusability[A]): Component[P, s.CT] =
        customBy(ctx => CustomHook.shouldComponentUpdate(f).apply(() => reusableInputs(ctx)))
          .render((_: HookCtx.PC1[P, VdomNode]).hook1)

      def render(f: (P, PropsChildren) => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
        ScalaFn.withChildren(f)

      def renderReusable[A](f: (P, PropsChildren) => Reusable[A])(implicit s: CtorType.Summoner[Box[P], Children.Varargs], v: A => VdomNode): Component[P, s.CT] =
        renderReusable(($: Ctx) => f($.props, $.propsChildren))

      def renderWithReuse(f: (P, PropsChildren) => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs], r: Reusability[Ctx]): Component[P, s.CT] =
        renderWithReuse(($: Ctx) => f($.props, $.propsChildren))

      def renderWithReuseBy[A](reusableInputs: (P, PropsChildren) => A)(f: A => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs], r: Reusability[A]): Component[P, s.CT] =
        renderWithReuseBy(($: Ctx) => reusableInputs($.props, $.propsChildren))(f)
    }

    type RenderFn[-P, +Ctx] = (Ctx => VdomNode) => (P, PropsChildren) => VdomNode

    final class Subsequent[P, Ctx, CtxFn[_]](renderFn: RenderFn[P, Ctx]) extends Api.SecondaryWithRender[P, Children.Varargs, Ctx, CtxFn, SubsequentStep[P, Ctx, CtxFn]] {

      override protected def self(f: Ctx => Any)(implicit step: Step): step.Self =
        step.self(renderFn, f)

      override protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H] =
        step.next[H](renderFn, f)

      override def render(f: Ctx => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
        ScalaFn.withChildren(renderFn(f))

      override def renderWithReuseBy[A](reusableInputs: Ctx => A)(f: A => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs], r: Reusability[A]): Component[P, s.CT] = {
        val hook = CustomHook.shouldComponentUpdate(f)
        ScalaFn.withChildren(renderFn { ctx =>
          hook.unsafeInit(() => reusableInputs(ctx))
        })
      }
    }

    object Subsequent extends ComponentPC_SubsequentDsl

    final class FirstStep[P] extends Api.AbstractStep {
      override type Self     = First[P]
      override type Next[H1] = Subsequent[P, HookCtx.PC1[P, H1], ({ type F[A] = (P, PropsChildren, H1) => A})#F]

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
        new Subsequent[P, HookCtx.PC1[P, H1], ({ type F[A] = (P, PropsChildren, H1) => A})#F](render)
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
