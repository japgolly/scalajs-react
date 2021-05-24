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
    import Step.{ComponentP => Step}

    final class First[P] extends Api.Primary[P, Step.First[P]] {

      override protected def next[H](f: P => H)(implicit step: Step): step.Next[H] =
        step(f)

      def withPropsChildren: ComponentPC.First[P] =
        new ComponentPC.First

      def render(f: P => VdomNode): Component[P, Children.None] =
        Component((p, _) => f(p))
    }

    type RenderFn[-P, +Ctx] = (Ctx => VdomNode) => Component[P, Children.None]

    final class Subsequent[P, Ctx, CtxFn[_]](renderFn: RenderFn[P, Ctx]) extends Api.Secondary[Ctx, CtxFn, Step.Subsequent[P, Ctx, CtxFn]] {

      protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H] =
        step.next[H](renderFn, f)

      def render(f: Ctx => VdomNode): Component[P, Children.None] =
        renderFn(f)

      def render(f: CtxFn[VdomNode])(implicit step: Step): Component[P, Children.None] =
        render(step.squash(f)(_))
    }

    object Subsequent extends ComponentP_SubsequentDsl
  }

  // ===================================================================================================================
  // Component with Props and PropsChildren

  object ComponentPC {
    import Step.{ComponentPC => Step}

    final class First[P] extends Api.Primary[HookCtx.PC0[P], Step.First[P]] {

      override protected def next[H](f: HookCtx.PC0[P] => H)(implicit step: Step): step.Next[H] =
        step(f)

      def render(f: HookCtx.PC0[P] => VdomNode): Component[P, Children.Varargs] =
        Component((p, pc) => f(HookCtx.withChildren(p, pc)))

      def render(f: (P, PropsChildren) => VdomNode): Component[P, Children.Varargs] =
        Component(f)
    }

    type RenderFn[-P, +Ctx] = (Ctx => VdomNode) => Component[P, Children.Varargs]

    final class Subsequent[P, Ctx, CtxFn[_]](renderFn: RenderFn[P, Ctx]) extends Api.Secondary[Ctx, CtxFn, Step.Subsequent[P, Ctx, CtxFn]] {

      protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H] =
        step.next[H](renderFn, f)

      def render(f: Ctx => VdomNode): Component[P, Children.Varargs] =
        renderFn(f)

      def render(f: CtxFn[VdomNode])(implicit step: Step): Component[P, Children.Varargs] =
        render(step.squash(f)(_))
    }

    object Subsequent extends ComponentPC_SubsequentDsl
  }

}
