package japgolly.scalajs.react.hooks

import japgolly.microlibs.compiletime.MacroEnv.*
import japgolly.scalajs.react.{Children, CtorType, PropsChildren, Reusability}
import japgolly.scalajs.react.component.{JsFn, ScalaFn}
import japgolly.scalajs.react.component.ScalaFn.Component
import japgolly.scalajs.react.facade
import japgolly.scalajs.react.facade.React
import japgolly.scalajs.react.hooks.Api.*
import japgolly.scalajs.react.hooks.HookCtx
import japgolly.scalajs.react.internal.{Box, MacroLogger}
import japgolly.scalajs.react.vdom.VdomNode
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
  }
}
