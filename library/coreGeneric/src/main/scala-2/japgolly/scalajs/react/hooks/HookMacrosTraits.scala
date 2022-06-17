package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.component.ScalaFn.Component
import japgolly.scalajs.react.hooks.Api._
import japgolly.scalajs.react.internal.Box
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, CtorType, PropsChildren}

trait ApiPrimaryWithRenderMacros[P, C <: Children, Ctx, Step <: AbstractStep] {
    self: PrimaryWithRender[P, C, Ctx, Step] =>

  final def renderRR(f: Ctx => VdomNode)(implicit s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
    macro HookMacros.render1[P, C]

  final def renderRRDebug(f: Ctx => VdomNode)(implicit s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
    macro HookMacros.renderDebug1[P, C]
}

// =====================================================================================================================

trait ComponentPCMacros[P] {
    self: HookComponentBuilder.ComponentPC.First[P] =>

  final def renderRR(f: (P, PropsChildren) => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
    macro HookMacros.renderC1[P]

  final def renderRRDebug(f: (P, PropsChildren) => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
    macro HookMacros.renderDebugC1[P]
}

// =====================================================================================================================

trait ApiSecondaryWithRenderMacros[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]] {
    self: PrimaryWithRender[P, C, Ctx, Step] with Secondary[Ctx, CtxFn, Step] =>

  final def renderRR(f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
    macro HookMacros.render2[P, C, Ctx, CtxFn, Step]

  final def renderRRDebug(f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
    macro HookMacros.renderDebug2[P, C, Ctx, CtxFn, Step]
}
