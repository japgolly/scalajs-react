package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.component.ScalaFn.Component
import japgolly.scalajs.react.hooks.Api._
import japgolly.scalajs.react.internal.Box
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, CtorType, PropsChildren, Reusable, Reusability}

trait ApiPrimaryWithRenderMacros[P, C <: Children, Ctx, Step <: AbstractStep] {
    self: PrimaryWithRender[P, C, Ctx, Step] =>

  final def renderRR(f: Ctx => VdomNode)(implicit s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
    macro HookMacros.render1[P, C, Ctx]

  final def renderRRDebug(f: Ctx => VdomNode)(implicit s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
    macro HookMacros.renderDebug1[P, C, Ctx]

  final def renderRRReusable[A](f: Ctx => Reusable[A])(implicit s: CtorType.Summoner[Box[P], C], v: A => VdomNode): Component[P, s.CT] =
    macro HookMacros.renderReusable1[P, C, A, Ctx]

  final def renderRRReusableDebug[A](f: Ctx => Reusable[A])(implicit s: CtorType.Summoner[Box[P], C], v: A => VdomNode): Component[P, s.CT] =
    macro HookMacros.renderReusableDebug1[P, C, A, Ctx]

  final def renderRRWithReuse(f: Ctx => VdomNode)(implicit s: CtorType.Summoner[Box[P], C], r: Reusability[Ctx]): Component[P, s.CT] =
    macro HookMacros.renderWithReuse1[P, C, Ctx]

  final def renderRRWithReuseDebug(f: Ctx => VdomNode)(implicit s: CtorType.Summoner[Box[P], C], r: Reusability[Ctx]): Component[P, s.CT] =
    macro HookMacros.renderWithReuseDebug1[P, C, Ctx]

  final def renderRRWithReuseBy[A](reusableInputs: Ctx => A)(f: A => VdomNode)(implicit s: CtorType.Summoner[Box[P], C], r: Reusability[A]): Component[P, s.CT] =
    macro HookMacros.renderWithReuseBy1[P, C, A, Ctx]

  final def renderRRWithReuseByDebug[A](reusableInputs: Ctx => A)(f: A => VdomNode)(implicit s: CtorType.Summoner[Box[P], C], r: Reusability[A]): Component[P, s.CT] =
    macro HookMacros.renderWithReuseByDebug1[P, C, A, Ctx]
}

// =====================================================================================================================

trait ComponentPCMacros[P] {
    self: HookComponentBuilder.ComponentPC.First[P] =>

  final def renderRR(f: (P, PropsChildren) => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
    macro HookMacros.renderC1[P]

  final def renderRRDebug(f: (P, PropsChildren) => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
    macro HookMacros.renderDebugC1[P]

  final def renderRRReusable[A](f: (P, PropsChildren) => Reusable[A])(implicit s: CtorType.Summoner[Box[P], Children.Varargs], v: A => VdomNode): Component[P, s.CT] =
    macro HookMacros.renderReusable1C[P, A]

  final def renderRRReusableDebug[A](f: (P, PropsChildren) => Reusable[A])(implicit s: CtorType.Summoner[Box[P], Children.Varargs], v: A => VdomNode): Component[P, s.CT] =
    macro HookMacros.renderReusableDebug1C[P, A]

  final def renderRRWithReuse(f: (P, PropsChildren) => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs], r: Reusability[Ctx]): Component[P, s.CT] =
    macro HookMacros.renderWithReuse1C[P]

  final def renderRRWithReuseDebug(f: (P, PropsChildren) => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs], r: Reusability[Ctx]): Component[P, s.CT] =
    macro HookMacros.renderWithReuseDebug1C[P]

  final def renderRRWithReuseBy[A](reusableInputs: (P, PropsChildren) => A)(f: A => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs], r: Reusability[A]): Component[P, s.CT] =
    macro HookMacros.renderWithReuseBy1C[P, A]

  final def renderRRWithReuseByDebug[A](reusableInputs: (P, PropsChildren) => A)(f: A => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs], r: Reusability[A]): Component[P, s.CT] =
    macro HookMacros.renderWithReuseByDebug1C[P, A]
}

// =====================================================================================================================

trait ApiSecondaryWithRenderMacros[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]] {
    self: PrimaryWithRender[P, C, Ctx, Step] with Secondary[Ctx, CtxFn, Step] =>

  final def renderRR(f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
    macro HookMacros.render2[P, C, Ctx, CtxFn, Step]

  final def renderRRDebug(f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
    macro HookMacros.renderDebug2[P, C, Ctx, CtxFn, Step]

  final def renderRRReusable[A](f: CtxFn[Reusable[A]])(implicit step: Step, s: CtorType.Summoner[Box[P], C], v: A => VdomNode): Component[P, s.CT] =
    macro HookMacros.renderReusable2[P, C, A, Ctx]

  final def renderRRReusableDebug[A](f: CtxFn[Reusable[A]])(implicit step: Step, s: CtorType.Summoner[Box[P], C], v: A => VdomNode): Component[P, s.CT] =
    macro HookMacros.renderReusableDebug2[P, C, A, Ctx]

  final def renderRRWithReuse(f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], C], r: Reusability[Ctx]): Component[P, s.CT] =
    macro HookMacros.renderWithReuse2[P, C, Ctx]

  final def renderRRWithReuseDebug(f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], C], r: Reusability[Ctx]): Component[P, s.CT] =
    macro HookMacros.renderWithReuseDebug2[P, C, Ctx]

  final def renderRRWithReuseBy[A](reusableInputs: CtxFn[A])(f: A => VdomNode)(implicit step: Step, s: CtorType.Summoner[Box[P], C], r: Reusability[A]): Component[P, s.CT] =
    macro HookMacros.renderWithReuseBy2[P, C, A, Ctx]

  final def renderRRWithReuseByDebug[A](reusableInputs: CtxFn[A])(f: A => VdomNode)(implicit step: Step, s: CtorType.Summoner[Box[P], C], r: Reusability[A]): Component[P, s.CT] =
    macro HookMacros.renderWithReuseByDebug2[P, C, A, Ctx]
}
