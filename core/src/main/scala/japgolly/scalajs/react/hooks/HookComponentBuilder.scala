package japgolly.scalajs.react.hooks

import Hooks._
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.Children

// TODO: Prop-less (maybe?)
// TODO: PropsChildren

object HookComponentBuilder {

  case class Component[-P, C <: Children](value: P => VdomNode) // TODO: Temp

  def apply[P]: DslStart[P] =
    new DslStart

  // ==========================================================================================================================
  // Step 0

  trait ApiPrimary[Ctx, _Step <: StepBase] {
    final type Step = _Step
    protected def nextPrimary[H](f: Ctx => H)(implicit step: Step): step.Next[H]

    final def useState[S](initialState: Ctx => S)(implicit step: Step): step.Next[UseState[S]] =
      nextPrimary(p => UseState.unsafeCreate(initialState(p)))
  }

  // ==========================================================================================================================
  // Step 1

  final class DslStart[P] extends ApiPrimary[P, StepFirst[P]] {
    override protected def nextPrimary[H](f: P => H)(implicit step: Step): step.Next[H] =
      step(f)

    def render(f: P => VdomNode): Component[P, Children.None] =
      Component(f)
  }

  // ==========================================================================================================================
  // Step 2+

  type RenderFn[-P, +Ctx] = (Ctx => VdomNode) => Component[P, Children.None]

  final class DslMulti[P, Ctx, CtxFn[_]](renderFn: RenderFn[P, Ctx]) extends ApiPrimary[Ctx, StepMulti[P, Ctx, CtxFn]] {
    protected def nextPrimary[H](f: Ctx => H)(implicit step: Step): step.Next[H] =
      step.next[H](renderFn, f)

    def useState[S](initialState: CtxFn[S])(implicit step: Step): step.Next[UseState[S]] =
      useState(step.squash(initialState)(_))

    def render(f: Ctx => VdomNode): Component[P, Children.None] =
      renderFn(f)

    def render(f: CtxFn[VdomNode])(implicit step: Step): Component[P, Children.None] =
      render(step.squash(f)(_))
  }

  object DslMulti extends DslMultiSteps
}