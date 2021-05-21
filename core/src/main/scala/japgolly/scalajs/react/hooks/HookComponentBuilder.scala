package japgolly.scalajs.react.hooks

import Hooks._
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, PropsChildren}

object HookComponentBuilder {

  case class Component[-P, C <: Children](value: (P, PropsChildren) => VdomNode) // TODO: Temp

  def apply[P]: DslStartP[P] =
    new DslStartP

  // ===================================================================================================================
  // API

  trait ApiPrimary[Ctx, _Step <: StepBase] {
    final type Step = _Step

    protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H]

    final def useState[S](initialState: Ctx => S)(implicit step: Step): step.Next[UseState[S]] =
      next(p => UseState.unsafeCreate(initialState(p)))
  }

  trait ApiSecondary[Ctx, CtxFn[_], _Step <: StepMulti[Ctx, CtxFn]] extends ApiPrimary[Ctx, _Step] {

    final def useState[S](initialState: CtxFn[S])(implicit step: Step): step.Next[UseState[S]] =
      useState(step.squash(initialState)(_))
  }

  // ===================================================================================================================
  // [P] Step 1

  final class DslStartP[P] extends ApiPrimary[P, StepFirstP[P]] {

    override protected def next[H](f: P => H)(implicit step: Step): step.Next[H] =
      step(f)

    def withPropsChildren: DslStartPC[P] =
      new DslStartPC

    def render(f: P => VdomNode): Component[P, Children.None] =
      Component((p, _) => f(p))
  }

  // ===================================================================================================================
  // [P] Step 2+

  type RenderFnP[-P, +Ctx] = (Ctx => VdomNode) => Component[P, Children.None]

  final class DslMultiP[P, Ctx, CtxFn[_]](renderFn: RenderFnP[P, Ctx]) extends ApiSecondary[Ctx, CtxFn, StepMultiP[P, Ctx, CtxFn]] {

    protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H] =
      step.next[H](renderFn, f)

    def render(f: Ctx => VdomNode): Component[P, Children.None] =
      renderFn(f)

    def render(f: CtxFn[VdomNode])(implicit step: Step): Component[P, Children.None] =
      render(step.squash(f)(_))
  }

  object DslMultiP extends DslMultiPSteps

  // ===================================================================================================================
  // [PC] Step 1

  final class DslStartPC[P] extends ApiPrimary[HookCtx.PC0[P], StepFirstPC[P]] {

    override protected def next[H](f: HookCtx.PC0[P] => H)(implicit step: Step): step.Next[H] =
      step(f)

    def render(f: HookCtx.PC0[P] => VdomNode): Component[P, Children.Varargs] =
      Component((p, pc) => f(HookCtx.withChildren(p, pc)))

    def render(f: (P, PropsChildren) => VdomNode): Component[P, Children.Varargs] =
      Component(f)
  }

  // ===================================================================================================================
  // [P] Step 2+

  type RenderFnPC[-P, +Ctx] = (Ctx => VdomNode) => Component[P, Children.Varargs]

  final class DslMultiPC[P, Ctx, CtxFn[_]](renderFn: RenderFnPC[P, Ctx]) extends ApiSecondary[Ctx, CtxFn, StepMultiPC[P, Ctx, CtxFn]] {

    protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H] =
      step.next[H](renderFn, f)

    def render(f: Ctx => VdomNode): Component[P, Children.Varargs] =
      renderFn(f)

    def render(f: CtxFn[VdomNode])(implicit step: Step): Component[P, Children.Varargs] =
      render(step.squash(f)(_))
  }

  object DslMultiPC extends DslMultiPCSteps
}
