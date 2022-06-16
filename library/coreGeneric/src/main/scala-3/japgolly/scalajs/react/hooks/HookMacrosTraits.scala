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

trait ApiSecondaryWithRenderMacros[P, C <: Children, Ctx, CtxFn[_], _Step <: SubsequentStep[Ctx, CtxFn]] {
      self: PrimaryWithRender[P, C, Ctx, _Step] with Secondary[Ctx, CtxFn, _Step] =>

  inline final def renderRR(f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
    HookMacros.render2Workaround[P, C, Ctx, CtxFn, Step, s.CT](this, f, step, s)

  inline final def renderRRDebug(f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
    HookMacros.renderDebug2Workaround[P, C, Ctx, CtxFn, Step, s.CT](this, f, step, s)
}
