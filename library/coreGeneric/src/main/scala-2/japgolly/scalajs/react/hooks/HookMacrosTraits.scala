package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.component.ScalaFn.Component
import japgolly.scalajs.react.hooks.Api._
import japgolly.scalajs.react.internal.Box
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, CtorType, PropsChildren, Reusable, Reusability}

trait ApiPrimaryWithRenderMacros[P, C <: Children, Ctx, Step <: AbstractStep] {
    self: PrimaryWithRender[P, C, Ctx, Step] =>

  final def renderRR(f: Ctx => VdomNode)(implicit s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
    macro HookMacros.render1[P, C]

  final def renderRRDebug(f: Ctx => VdomNode)(implicit s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
    macro HookMacros.renderDebug1[P, C]

  final def renderRRReusable[A](f: Ctx => Reusable[A])(implicit s: CtorType.Summoner[Box[P], C], v: A => VdomNode): Component[P, s.CT] =
    renderReusable(f) // TODO: use macro

  final def renderRRWithReuse(f: Ctx => VdomNode)(implicit s: CtorType.Summoner[Box[P], C], r: Reusability[Ctx]): Component[P, s.CT] =
    renderWithReuse(f) // TODO: use macro

  final def renderRRWithReuseBy[A](reusableInputs: Ctx => A)(f: A => VdomNode)(implicit s: CtorType.Summoner[Box[P], C], r: Reusability[A]): Component[P, s.CT] =
    renderWithReuseBy(reusableInputs)(f) // TODO: use macro
}

// =====================================================================================================================

trait ComponentPCMacros[P] {
    self: HookComponentBuilder.ComponentPC.First[P] =>

  final def renderRR(f: (P, PropsChildren) => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
    macro HookMacros.renderC1[P]

  final def renderRRDebug(f: (P, PropsChildren) => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
    macro HookMacros.renderDebugC1[P]

  final def renderRRReusable[A](f: (P, PropsChildren) => Reusable[A])(implicit s: CtorType.Summoner[Box[P], Children.Varargs], v: A => VdomNode): Component[P, s.CT] =
    renderReusable(f) // TODO: use macro

  final def renderRRWithReuse(f: (P, PropsChildren) => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs], r: Reusability[Ctx]): Component[P, s.CT] =
    renderWithReuse(f) // TODO: use macro

  final def renderRRWithReuseBy[A](reusableInputs: (P, PropsChildren) => A)(f: A => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs], r: Reusability[A]): Component[P, s.CT] =
    renderWithReuseBy(reusableInputs)(f) // TODO: use macro
}

// =====================================================================================================================

trait ApiSecondaryWithRenderMacros[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]] {
    self: PrimaryWithRender[P, C, Ctx, Step] with Secondary[Ctx, CtxFn, Step] =>

  final def renderRR(f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
    macro HookMacros.render2[P, C, Ctx, CtxFn, Step]

  final def renderRRDebug(f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
    macro HookMacros.renderDebug2[P, C, Ctx, CtxFn, Step]

  final def renderRRReusable[A](f: CtxFn[Reusable[A]])(implicit step: Step, s: CtorType.Summoner[Box[P], C], v: A => VdomNode): Component[P, s.CT] =
    renderReusable(step.squash(f)(_)) // TODO: use macro

  final def renderRRWithReuse(f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], C], r: Reusability[Ctx]): Component[P, s.CT] =
    renderWithReuse(step.squash(f)(_)) // TODO: use macro

  final def renderRRWithReuseBy[A](reusableInputs: CtxFn[A])(f: A => VdomNode)(implicit step: Step, s: CtorType.Summoner[Box[P], C], r: Reusability[A]): Component[P, s.CT] =
    renderWithReuseBy(step.squash(reusableInputs)(_))(f) // TODO: use macro
}
