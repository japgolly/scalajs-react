package japgolly.scalajs.react.hooks

import japgolly.microlibs.compiletime.MacroEnv.*
import japgolly.scalajs.react.component.{JsFn, ScalaFn}
import japgolly.scalajs.react.component.ScalaFn.Component
import japgolly.scalajs.react.facade
import japgolly.scalajs.react.facade.React
import japgolly.scalajs.react.hooks.Api.*
import japgolly.scalajs.react.hooks.HookCtx
import japgolly.scalajs.react.internal.{Box, MacroLogger}
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, CtorType, PropsChildren, Reusable, Reusability}
import scala.annotation._
import scala.quoted.*
import scala.reflect.ClassTag
import scala.scalajs.js

trait ApiPrimaryWithRenderMacros[P, C <: Children, Ctx, Step <: AbstractStep] {
    self: PrimaryWithRender[P, C, Ctx, Step] =>
}

object ApiPrimaryWithRenderMacros {
  extension [P, C <: Children, Ctx, Step <: AbstractStep](inline self: ApiPrimaryWithRenderMacros[P, C, Ctx, Step]) {

    inline def renderRR(inline f: Ctx => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
      HookMacros.render1Workaround[P, C, Ctx, Step, s.CT](self, f, s)

    inline def renderRRDebug(inline f: Ctx => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
      HookMacros.renderDebug1Workaround[P, C, Ctx, Step, s.CT](self, f, s)

    inline def renderRRReusable[A](f: Ctx => Reusable[A])(implicit s: CtorType.Summoner[Box[P], C], v: A => VdomNode): Component[P, s.CT] =
      renderReusable(f) // TODO: use macro

    inline def renderRRWithReuse(f: Ctx => VdomNode)(implicit s: CtorType.Summoner[Box[P], C], r: Reusability[Ctx]): Component[P, s.CT] =
      renderWithReuse(f) // TODO: use macro

    inline def renderRRWithReuseBy[A](reusableInputs: Ctx => A)(f: A => VdomNode)(implicit s: CtorType.Summoner[Box[P], C], r: Reusability[A]): Component[P, s.CT] =
      renderWithReuseBy(reusableInputs)(f) // TODO: use macro
  }
}

// =====================================================================================================================

trait ComponentPCMacros[P]
    extends ApiPrimaryWithRenderMacros[P, Children.Varargs, HookCtx.PC0[P], HookComponentBuilder.ComponentPC.FirstStep[P]] {
    self: HookComponentBuilder.ComponentPC.First[P] =>
}

object ComponentPCMacros {
  extension [P](inline self: ComponentPCMacros[P]) {

    inline def renderRR(inline f: (P, PropsChildren) => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
      HookMacros.renderC1Workaround[P, s.CT](self, f, s)

    inline def renderRRDebug(inline f: (P, PropsChildren) => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
      HookMacros.renderDebugC1Workaround[P, s.CT](self, f, s)

    inline def renderRRReusable[A](f: (P, PropsChildren) => Reusable[A])(implicit s: CtorType.Summoner[Box[P], Children.Varargs], v: A => VdomNode): Component[P, s.CT] =
      renderReusable(f) // TODO: use macro

    inline def renderRRWithReuse(f: (P, PropsChildren) => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs], r: Reusability[Ctx]): Component[P, s.CT] =
      renderWithReuse(f) // TODO: use macro

    inline def renderRRWithReuseBy[A](reusableInputs: (P, PropsChildren) => A)(f: A => VdomNode)(implicit s: CtorType.Summoner[Box[P], Children.Varargs], r: Reusability[A]): Component[P, s.CT] =
      renderWithReuseBy(reusableInputs)(f) // TODO: use macro
  }
}

// =====================================================================================================================

trait ApiSecondaryWithRenderMacros[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]]
    extends ApiPrimaryWithRenderMacros[P, C, Ctx, Step] {
    self: PrimaryWithRender[P, C, Ctx, Step] with Secondary[Ctx, CtxFn, Step] =>
}

object ApiSecondaryWithRenderMacros {
  extension [P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]](inline self: ApiSecondaryWithRenderMacros[P, C, Ctx, CtxFn, Step]) {

    inline def renderRR(inline f: CtxFn[VdomNode])(implicit inline step: Step, inline s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
      HookMacros.render2Workaround[P, C, Ctx, CtxFn, Step, s.CT](self, f, step, s)

    inline def renderRRDebug(inline f: CtxFn[VdomNode])(implicit inline step: Step, inline s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
      HookMacros.renderDebug2Workaround[P, C, Ctx, CtxFn, Step, s.CT](self, f, step, s)

    // Gotta duplicate the primary extensions below due to the way Scala 3 handles overload resolution

    inline def renderRR(inline f: Ctx => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
      HookMacros.render1Workaround[P, C, Ctx, Step, s.CT](self, f, s)

    inline def renderRRDebug(inline f: Ctx => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
      HookMacros.renderDebug1Workaround[P, C, Ctx, Step, s.CT](self, f, s)

    inline def renderRRReusable[A](f: CtxFn[Reusable[A]])(implicit step: Step, s: CtorType.Summoner[Box[P], C], v: A => VdomNode): Component[P, s.CT] =
      renderReusable(step.squash(f)(_)) // TODO: use macro

    inline def renderRRWithReuse(f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], C], r: Reusability[Ctx]): Component[P, s.CT] =
      renderWithReuse(step.squash(f)(_)) // TODO: use macro

    inline def renderRRWithReuseBy[A](reusableInputs: CtxFn[A])(f: A => VdomNode)(implicit step: Step, s: CtorType.Summoner[Box[P], C], r: Reusability[A]): Component[P, s.CT] =
      renderWithReuseBy(step.squash(reusableInputs)(_))(f) // TODO: use macro
  }
}
