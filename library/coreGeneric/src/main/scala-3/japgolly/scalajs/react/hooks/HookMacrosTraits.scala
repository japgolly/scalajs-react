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
      HookMacros.render1[P, C, Ctx, Step, s.CT](self, f, s)

    inline def renderRRDebug(inline f: Ctx => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
      HookMacros.renderDebug1[P, C, Ctx, Step, s.CT](self, f, s)

    inline def renderRRReusable[A](inline f: Ctx => Reusable[A])(implicit s: CtorType.Summoner[Box[P], C], inline v: A => VdomNode): Component[P, s.CT] =
      HookMacros.renderReusable1[P, C, A, Ctx, Step, s.CT](self, f, s, v)

    inline def renderRRReusableDebug[A](inline f: Ctx => Reusable[A])(implicit s: CtorType.Summoner[Box[P], C], inline v: A => VdomNode): Component[P, s.CT] =
      HookMacros.renderReusableDebug1[P, C, A, Ctx, Step, s.CT](self, f, s, v)

    inline def renderRRWithReuse(inline f: Ctx => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], C], inline r: Reusability[Ctx]): Component[P, s.CT] =
      HookMacros.renderWithReuse1[P, C, Ctx, Step, s.CT](self, f, s, r)

    inline def renderRRWithReuseDebug(inline f: Ctx => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], C], inline r: Reusability[Ctx]): Component[P, s.CT] =
      HookMacros.renderWithReuseDebug1[P, C, Ctx, Step, s.CT](self, f, s, r)

    inline def renderRRWithReuseBy[A](inline reusableInputs: Ctx => A)(inline f: A => VdomNode)
    (implicit inline s: CtorType.Summoner[Box[P], C], inline r: Reusability[A]): Component[P, s.CT] =
      HookMacros.renderWithReuseBy1[P, C, A, Ctx, Step, s.CT](self, reusableInputs, f, s, r)

    inline def renderRRWithReuseByDebug[A](inline reusableInputs: Ctx => A)(inline f: A => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], C], inline r: Reusability[A]): Component[P, s.CT] =
      HookMacros.renderWithReuseByDebug1[P, C, A, Ctx, Step, s.CT](self, reusableInputs, f, s, r)
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
      HookMacros.render1C[P, s.CT](self, f, s)

    inline def renderRRDebug(inline f: (P, PropsChildren) => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], Children.Varargs]): Component[P, s.CT] =
      HookMacros.renderDebug1C[P, s.CT](self, f, s)

    inline def renderRRReusable[A](inline f: (P, PropsChildren) => Reusable[A])(implicit inline s: CtorType.Summoner[Box[P], Children.Varargs], inline v: A => VdomNode): Component[P, s.CT] =
      HookMacros.renderReusable1C[P, A, s.CT](self, f, s, v)

    inline def renderRRReusableDebug[A](inline f: (P, PropsChildren) => Reusable[A])(implicit inline s: CtorType.Summoner[Box[P], Children.Varargs], inline v: A => VdomNode): Component[P, s.CT] =
      HookMacros.renderReusableDebug1C[P, A, s.CT](self, f, s, v)

    inline def renderRRWithReuse(inline f: (P, PropsChildren) => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], Children.Varargs], inline r: Reusability[HookCtx.PC0[P]]): Component[P, s.CT] =
      HookMacros.renderWithReuse1C[P, s.CT](self, f, s, r)

    inline def renderRRWithReuseDebug(inline f: (P, PropsChildren) => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], Children.Varargs], inline r: Reusability[HookCtx.PC0[P]]): Component[P, s.CT] =
      HookMacros.renderWithReuseDebug1C[P, s.CT](self, f, s, r)

    inline def renderRRWithReuseBy[A](inline reusableInputs: (P, PropsChildren) => A)(inline f: A => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], Children.Varargs], inline r: Reusability[A]): Component[P, s.CT] =
      HookMacros.renderWithReuseBy1C[P, A, s.CT](self, reusableInputs, f, s, r)

    inline def renderRRWithReuseByDebug[A](inline reusableInputs: (P, PropsChildren) => A)(inline f: A => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], Children.Varargs], inline r: Reusability[A]): Component[P, s.CT] =
      HookMacros.renderWithReuseByDebug1C[P, A, s.CT](self, reusableInputs, f, s, r)
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
      HookMacros.render2[P, C, Ctx, CtxFn, Step, s.CT](self, f, step, s)

    inline def renderRRDebug(inline f: CtxFn[VdomNode])(implicit inline step: Step, inline s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
      HookMacros.renderDebug2[P, C, Ctx, CtxFn, Step, s.CT](self, f, step, s)

    inline def renderRRReusable[A](inline f: CtxFn[Reusable[A]])(implicit inline step: Step, inline s: CtorType.Summoner[Box[P], C], inline v: A => VdomNode): Component[P, s.CT] =
      HookMacros.renderReusable2[P, C, A, Ctx, CtxFn, Step, s.CT](self, f, step, s, v)

    inline def renderRRReusableDebug[A](inline f: CtxFn[Reusable[A]])(implicit inline step: Step, inline s: CtorType.Summoner[Box[P], C], inline v: A => VdomNode): Component[P, s.CT] =
      HookMacros.renderReusableDebug2[P, C, A, Ctx, CtxFn, Step, s.CT](self, f, step, s, v)

    inline def renderRRWithReuse(inline f: CtxFn[VdomNode])(implicit inline step: Step, inline s: CtorType.Summoner[Box[P], C], inline r: Reusability[Ctx]): Component[P, s.CT] =
      HookMacros.renderWithReuse2[P, C, Ctx, CtxFn, Step, s.CT](self, f, step, s, r)

    inline def renderRRWithReuseDebug(inline f: CtxFn[VdomNode])(implicit inline step: Step, inline s: CtorType.Summoner[Box[P], C], inline r: Reusability[Ctx]): Component[P, s.CT] =
      HookMacros.renderWithReuseDebug2[P, C, Ctx, CtxFn, Step, s.CT](self, f, step, s, r)

    inline def renderRRWithReuseBy[A](inline reusableInputs: CtxFn[A])(inline f: A => VdomNode)(implicit inline step: Step, inline s: CtorType.Summoner[Box[P], C], inline r: Reusability[A]): Component[P, s.CT] =
      HookMacros.renderWithReuseBy2[P, C, A, Ctx, CtxFn, Step, s.CT](self, reusableInputs, f, step, s, r)

    inline def renderRRWithReuseByDebug[A](inline reusableInputs: CtxFn[A])(inline f: A => VdomNode)(implicit inline step: Step, inline s: CtorType.Summoner[Box[P], C], inline r: Reusability[A]): Component[P, s.CT] =
      HookMacros.renderWithReuseByDebug2[P, C, A, Ctx, CtxFn, Step, s.CT](self, reusableInputs, f, step, s, r)

    // -----------------------------------------------------------------------------------------------
    // Gotta duplicate the primary extensions below due to the way Scala 3 handles overload resolution

    inline def renderRR(inline f: Ctx => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
      HookMacros.render1[P, C, Ctx, Step, s.CT](self, f, s)

    inline def renderRRDebug(inline f: Ctx => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
      HookMacros.renderDebug1[P, C, Ctx, Step, s.CT](self, f, s)

    inline def renderRRReusable[A](inline f: Ctx => Reusable[A])(implicit s: CtorType.Summoner[Box[P], C], inline v: A => VdomNode): Component[P, s.CT] =
      HookMacros.renderReusable1[P, C, A, Ctx, Step, s.CT](self, f, s, v)

    inline def renderRRReusableDebug[A](inline f: Ctx => Reusable[A])(implicit s: CtorType.Summoner[Box[P], C], inline v: A => VdomNode): Component[P, s.CT] =
      HookMacros.renderReusableDebug1[P, C, A, Ctx, Step, s.CT](self, f, s, v)

    inline def renderRRWithReuse(inline f: Ctx => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], C], inline r: Reusability[Ctx]): Component[P, s.CT] =
      HookMacros.renderWithReuse1[P, C, Ctx, Step, s.CT](self, f, s, r)

    inline def renderRRWithReuseDebug(inline f: Ctx => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], C], inline r: Reusability[Ctx]): Component[P, s.CT] =
      HookMacros.renderWithReuseDebug1[P, C, Ctx, Step, s.CT](self, f, s, r)

    inline def renderRRWithReuseBy[A](inline reusableInputs: Ctx => A)(inline f: A => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], C], inline r: Reusability[A]): Component[P, s.CT] =
      HookMacros.renderWithReuseBy1[P, C, A, Ctx, Step, s.CT](self, reusableInputs, f, s, r)

    inline def renderRRWithReuseByDebug[A](inline reusableInputs: Ctx => A)(inline f: A => VdomNode)(implicit inline s: CtorType.Summoner[Box[P], C], inline r: Reusability[A]): Component[P, s.CT] =
      HookMacros.renderWithReuseByDebug1[P, C, A, Ctx, Step, s.CT](self, reusableInputs, f, s, r)
  }
}
