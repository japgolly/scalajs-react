package japgolly.scalajs.react.hooks

import japgolly.microlibs.compiletime.MacroEnv.*
import japgolly.scalajs.react.{Children, CtorType, PropsChildren, Ref => ScalaRef, Reusable, Reusability}
import japgolly.scalajs.react.component.{Js => JsComponent, JsFn, Scala => ScalaComponent, ScalaFn}
import japgolly.scalajs.react.component.ScalaFn.Component
import japgolly.scalajs.react.facade
import japgolly.scalajs.react.facade.React
import japgolly.scalajs.react.hooks.Api.*
import japgolly.scalajs.react.hooks.CustomHook
import japgolly.scalajs.react.hooks.CustomHook.ReusableDepState
import japgolly.scalajs.react.hooks.HookCtx
import japgolly.scalajs.react.internal.{Box, MacroLogger, ShouldComponentUpdateComponent}
import japgolly.scalajs.react.util.DefaultEffects
import japgolly.scalajs.react.vdom.{TopNode, VdomNode}
import scala.annotation._
import scala.language.`3.1`
import scala.quoted.*
import scala.reflect.ClassTag
import scala.scalajs.js

object HookMacros {
  import AbstractHookMacros.HookStep

  type PrimaryProxy[P, C <: Children, Ctx, Step <: AbstractStep] =
    ApiPrimaryWithRenderMacros[P, C, Ctx, Step]

  type PrimaryApi[P, C <: Children, Ctx, Step <: AbstractStep] =
    PrimaryWithRender[P, C, Ctx, Step]

  type SecondaryProxy[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]] =
    ApiSecondaryWithRenderMacros[P, C, Ctx, CtxFn, Step]

  type SecondaryApi[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]] =
    PrimaryWithRender[P, C, Ctx, Step] with Secondary[Ctx, CtxFn, Step]

  // Note: *Workaround methods exist due to https://github.com/lampepfl/dotty/issues/15357

  // ===================================================================================================================
  // render

  inline def render1[P, C <: Children, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: PrimaryProxy[P, C, Ctx, Step],
      inline f    : Ctx => VdomNode,
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT]): Component[P, CT] =
    ${ render1Workaround[P, C, Ctx, Step, CT]('proxy, 'f, 's) }

  inline def renderDebug1[P, C <: Children, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: PrimaryProxy[P, C, Ctx, Step],
      inline f    : Ctx => VdomNode,
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT]): Component[P, CT] =
    ${ renderDebug1Workaround[P, C, Ctx, Step, CT]('proxy, 'f, 's) }

  def render1Workaround[P, C <: Children, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[PrimaryProxy[P, C, Ctx, Step]],
      f    : Expr[Ctx => VdomNode],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]])
      (using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], Step: Type[Step]): Expr[Component[P, CT]] =
    _render1(proxy, f, s, false)

  def renderDebug1Workaround[P, C <: Children, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[PrimaryProxy[P, C, Ctx, Step]],
      f    : Expr[Ctx => VdomNode],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]])
     (using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], Step: Type[Step]): Expr[Component[P, CT]] =
    _render1(proxy, f, s, true)

  def _render1[P, C <: Children, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      proxy   : Expr[PrimaryProxy[P, C, Ctx, Step]],
      renderFn: Expr[Ctx => VdomNode],
      summoner: Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      debug   : Boolean)
      (using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], Step: Type[Step]): Expr[Component[P, CT]] = {
    import quotes.reflect.*
    val self = proxy.asInstanceOf[Expr[PrimaryApi[P, C, Ctx, Step]]]
    apply[P, C, CT](
      macroApplication = self.asTerm,
      ctxType          = TypeTree.of[Ctx],
      summoner         = summoner,
      debug            = debug,
      renderStep       = HookStep("renderRR", Nil, List(List(renderFn.asTerm), List(summoner.asTerm))),
      giveUp           = () => '{ $self.render($renderFn)($summoner) },
    )
  }

  // -------------------------------------------------------------------------------------------------------------------

  inline def render1C[P, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: ComponentPCMacros[P],
      inline f    : (P, PropsChildren) => VdomNode,
      inline s    : CtorType.Summoner.Aux[Box[P], Children.Varargs, CT]): Component[P, CT] =
    ${ render1CWorkaround[P, CT]('proxy, 'f, 's) }

  inline def renderDebug1C[P, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: ComponentPCMacros[P],
      inline f    : (P, PropsChildren) => VdomNode,
      inline s    : CtorType.Summoner.Aux[Box[P], Children.Varargs, CT]): Component[P, CT] =
    ${ renderDebug1CWorkaround[P, CT]('proxy, 'f, 's) }

  def render1CWorkaround[P, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[ComponentPCMacros[P]],
      f    : Expr[(P, PropsChildren) => VdomNode],
      s    : Expr[CtorType.Summoner.Aux[Box[P], Children.Varargs, CT]])
      (using q: Quotes, P: Type[P], CT: Type[CT]): Expr[Component[P, CT]] =
    _render1C(proxy, f, s, false)

  def renderDebug1CWorkaround[P, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[ComponentPCMacros[P]],
      f    : Expr[(P, PropsChildren) => VdomNode],
      s    : Expr[CtorType.Summoner.Aux[Box[P], Children.Varargs, CT]])
      (using q: Quotes, P: Type[P], CT: Type[CT]): Expr[Component[P, CT]] =
    _render1C(proxy, f, s, true)

  def _render1C[P, CT[-p, +u] <: CtorType[p, u]](
      proxy   : Expr[ComponentPCMacros[P]],
      renderFn: Expr[(P, PropsChildren) => VdomNode],
      summoner: Expr[CtorType.Summoner.Aux[Box[P], Children.Varargs, CT]],
      debug   : Boolean)
      (using q: Quotes, P: Type[P], CT: Type[CT]): Expr[Component[P, CT]] = {
    import quotes.reflect.*
    val self = proxy.asInstanceOf[Expr[HookComponentBuilder.ComponentPC.First[P]]]
    apply[P, Children.Varargs, CT](
      macroApplication = self.asTerm,
      ctxType          = TypeTree.of[HookCtx.PC0[P]],
      summoner         = summoner,
      debug            = debug,
      renderStep       = HookStep("renderRR", Nil, List(List(renderFn.asTerm), List(summoner.asTerm))),
      giveUp           = () => '{ $self.render($renderFn)($summoner) },
    )
  }

  // -------------------------------------------------------------------------------------------------------------------

  inline def render2[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      inline proxy: SecondaryProxy[P, C, Ctx, CtxFn, Step],
      inline f    : CtxFn[VdomNode],
      inline step : Step,
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT]): Component[P, CT] =
    ${ render2Workaround[P, C, Ctx, CtxFn, Step, CT]('proxy, 'f, 'step, 's) }

  inline def renderDebug2[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      inline proxy: SecondaryProxy[P, C, Ctx, CtxFn, Step],
      inline f    : CtxFn[VdomNode],
      inline step : Step,
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT]): Component[P, CT] =
    ${ renderDebug2Workaround[P, C, Ctx, CtxFn, Step, CT]('proxy, 'f, 'step, 's) }

  def render2Workaround[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[SecondaryProxy[P, C, Ctx, CtxFn, Step]],
      f    : Expr[CtxFn[VdomNode]],
      step : Expr[Step],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]])
      (using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], CtxFn: Type[CtxFn], Step: Type[Step]): Expr[Component[P, CT]] =
    _render2(proxy, f, step, s, false)

  def renderDebug2Workaround[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[SecondaryProxy[P, C, Ctx, CtxFn, Step]],
      f    : Expr[CtxFn[VdomNode]],
      step : Expr[Step],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]])
      (using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], CtxFn: Type[CtxFn], Step: Type[Step]): Expr[Component[P, CT]] =
    _render2(proxy, f, step, s, true)

  def _render2[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      proxy   : Expr[SecondaryProxy[P, C, Ctx, CtxFn, Step]],
      renderFn: Expr[CtxFn[VdomNode]],
      step    : Expr[Step],
      summoner: Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      debug   : Boolean)
      (using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], CtxFn: Type[CtxFn], Step: Type[Step]): Expr[Component[P, CT]] = {
    import quotes.reflect.*
    val self = proxy.asInstanceOf[Expr[SecondaryApi[P, C, Ctx, CtxFn, Step]]]
    apply[P, C, CT](
      macroApplication = self.asTerm,
      ctxType          = TypeTree.of[Ctx],
      summoner         = summoner,
      debug            = debug,
      renderStep       = HookStep("renderRR", Nil, List(List(renderFn.asTerm), List(step.asTerm, summoner.asTerm))),
      giveUp           = () => '{ $self.render($step.squash($renderFn)(_))($summoner) },
    )
  }

  // ===================================================================================================================
  // renderReusable

  inline def renderReusable1[P, C <: Children, A, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: PrimaryProxy[P, C, Ctx, Step],
      inline f    : Ctx => Reusable[A],
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT],
      inline v    : A => VdomNode): Component[P, CT] =
    ${ renderReusable1Workaround[P, C, A, Ctx, Step, CT]('proxy, 'f, 's, 'v) }

  inline def renderReusableDebug1[P, C <: Children, A, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: PrimaryProxy[P, C, Ctx, Step],
      inline f    : Ctx => Reusable[A],
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT],
      inline v    : A => VdomNode): Component[P, CT] =
    ${ renderReusableDebug1Workaround[P, C, A, Ctx, Step, CT]('proxy, 'f, 's, 'v) }

  def renderReusable1Workaround[P, C <: Children, A, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[PrimaryProxy[P, C, Ctx, Step]],
      f    : Expr[Ctx => Reusable[A]],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      v    : Expr[A => VdomNode])
      (using q: Quotes, P: Type[P], C: Type[C], A: Type[A], CT: Type[CT], Ctx: Type[Ctx], Step: Type[Step]): Expr[Component[P, CT]] =
    _renderReusable1(proxy, f, s, v, false)

  def renderReusableDebug1Workaround[P, C <: Children, A, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[PrimaryProxy[P, C, Ctx, Step]],
      f    : Expr[Ctx => Reusable[A]],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      v    : Expr[A => VdomNode])
     (using q: Quotes, P: Type[P], C: Type[C], A: Type[A], CT: Type[CT], Ctx: Type[Ctx], Step: Type[Step]): Expr[Component[P, CT]] =
    _renderReusable1(proxy, f, s, v, true)

  def _renderReusable1[P, C <: Children, A, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      proxy   : Expr[PrimaryProxy[P, C, Ctx, Step]],
      f       : Expr[Ctx => Reusable[A]],
      summoner: Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      v       : Expr[A => VdomNode],
      debug   : Boolean)
      (using q: Quotes, P: Type[P], C: Type[C], A: Type[A], CT: Type[CT], Ctx: Type[Ctx], Step: Type[Step]): Expr[Component[P, CT]] = {
    import quotes.reflect.*
    val self = proxy.asInstanceOf[Expr[PrimaryApi[P, C, Ctx, Step]]]
    apply[P, C, CT](
      macroApplication = self.asTerm,
      ctxType          = TypeTree.of[Ctx],
      summoner         = summoner,
      debug            = debug,
      renderStep       = HookStep("renderRRReusable", List(TypeTree.of[A]), List(List(f.asTerm), List(summoner.asTerm, v.asTerm))),
      giveUp           = () => '{ $self.renderReusable($f)($summoner, $v) },
    )
  }

  // -------------------------------------------------------------------------------------------------------------------

  inline def renderReusable1C[P, A, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: ComponentPCMacros[P],
      inline f    : (P, PropsChildren) => Reusable[A],
      inline s    : CtorType.Summoner.Aux[Box[P], Children.Varargs, CT],
      inline v    : A => VdomNode): Component[P, CT] =
    ${ renderReusable1CWorkaround[P, A, CT]('proxy, 'f, 's, 'v) }

  inline def renderReusableDebug1C[P, A, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: ComponentPCMacros[P],
      inline f    : (P, PropsChildren) => Reusable[A],
      inline s    : CtorType.Summoner.Aux[Box[P], Children.Varargs, CT],
      inline v    : A => VdomNode): Component[P, CT] =
    ${ renderReusableDebug1CWorkaround[P, A, CT]('proxy, 'f, 's, 'v) }

  def renderReusable1CWorkaround[P, A, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[ComponentPCMacros[P]],
      f    : Expr[(P, PropsChildren) => Reusable[A]],
      s    : Expr[CtorType.Summoner.Aux[Box[P], Children.Varargs, CT]],
      v    : Expr[A => VdomNode])
      (using q: Quotes, P: Type[P], A: Type[A], CT: Type[CT]): Expr[Component[P, CT]] =
    _renderReusable1C(proxy, f, s, v, false)

  def renderReusableDebug1CWorkaround[P, A, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[ComponentPCMacros[P]],
      f    : Expr[(P, PropsChildren) => Reusable[A]],
      s    : Expr[CtorType.Summoner.Aux[Box[P], Children.Varargs, CT]],
      v    : Expr[A => VdomNode])
      (using q: Quotes, P: Type[P], A: Type[A], CT: Type[CT]): Expr[Component[P, CT]] =
    _renderReusable1C(proxy, f, s, v, true)

  def _renderReusable1C[P, A, CT[-p, +u] <: CtorType[p, u]](
      proxy   : Expr[ComponentPCMacros[P]],
      f       : Expr[(P, PropsChildren) => Reusable[A]],
      summoner: Expr[CtorType.Summoner.Aux[Box[P], Children.Varargs, CT]],
      v       : Expr[A => VdomNode],
      debug   : Boolean)
      (using q: Quotes, P: Type[P], A: Type[A], CT: Type[CT]): Expr[Component[P, CT]] = {
    import quotes.reflect.*
    val self = proxy.asInstanceOf[Expr[HookComponentBuilder.ComponentPC.First[P]]]
    apply[P, Children.Varargs, CT](
      macroApplication = self.asTerm,
      ctxType          = TypeTree.of[HookCtx.PC0[P]],
      summoner         = summoner,
      debug            = debug,
      renderStep       = HookStep("renderRRReusable", List(TypeTree.of[A]), List(List(f.asTerm), List(summoner.asTerm, v.asTerm))),
      giveUp           = () => '{ $self.renderReusable($f)($summoner, $v) },
    )
  }

  // -------------------------------------------------------------------------------------------------------------------

  inline def renderReusable2[P, C <: Children, A, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      inline proxy: SecondaryProxy[P, C, Ctx, CtxFn, Step],
      inline f    : CtxFn[Reusable[A]],
      inline step : Step,
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT],
      inline v    : A => VdomNode): Component[P, CT] =
    ${ renderReusable2Workaround[P, C, A, Ctx, CtxFn, Step, CT]('proxy, 'f, 'step, 's, 'v) }

  inline def renderReusableDebug2[P, C <: Children, A, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      inline proxy: SecondaryProxy[P, C, Ctx, CtxFn, Step],
      inline f    : CtxFn[Reusable[A]],
      inline step : Step,
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT],
      inline v    : A => VdomNode): Component[P, CT] =
    ${ renderReusableDebug2Workaround[P, C, A, Ctx, CtxFn, Step, CT]('proxy, 'f, 'step, 's, 'v) }

  def renderReusable2Workaround[P, C <: Children, A, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[SecondaryProxy[P, C, Ctx, CtxFn, Step]],
      f    : Expr[CtxFn[Reusable[A]]],
      step : Expr[Step],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      v    : Expr[A => VdomNode])
      (using q: Quotes, P: Type[P], C: Type[C], A: Type[A], CT: Type[CT], Ctx: Type[Ctx], CtxFn: Type[CtxFn], Step: Type[Step]): Expr[Component[P, CT]] =
    _renderReusable2(proxy, f, step, s, v, false)

  def renderReusableDebug2Workaround[P, C <: Children, A, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[SecondaryProxy[P, C, Ctx, CtxFn, Step]],
      f    : Expr[CtxFn[Reusable[A]]],
      step : Expr[Step],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      v    : Expr[A => VdomNode])
      (using q: Quotes, P: Type[P], C: Type[C], A: Type[A], CT: Type[CT], Ctx: Type[Ctx], CtxFn: Type[CtxFn], Step: Type[Step]): Expr[Component[P, CT]] =
    _renderReusable2(proxy, f, step, s, v, true)

  def _renderReusable2[P, C <: Children, A, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      proxy   : Expr[SecondaryProxy[P, C, Ctx, CtxFn, Step]],
      f       : Expr[CtxFn[Reusable[A]]],
      step    : Expr[Step],
      summoner: Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      v       : Expr[A => VdomNode],
      debug   : Boolean)
      (using q: Quotes, P: Type[P], C: Type[C], A: Type[A], CT: Type[CT], Ctx: Type[Ctx], CtxFn: Type[CtxFn], Step: Type[Step]): Expr[Component[P, CT]] = {
    import quotes.reflect.*
    val self = proxy.asInstanceOf[Expr[SecondaryApi[P, C, Ctx, CtxFn, Step]]]
    apply[P, C, CT](
      macroApplication = self.asTerm,
      ctxType          = TypeTree.of[Ctx],
      summoner         = summoner,
      debug            = debug,
      renderStep       = HookStep("renderRRReusable", List(TypeTree.of[A]), List(List(f.asTerm), List(step.asTerm, summoner.asTerm, v.asTerm))),
      giveUp           = () => '{ $self.renderReusable($step.squash($f)(_))($summoner, $v) },
    )
  }

  // ===================================================================================================================
  // renderWithReuse

  inline def renderWithReuse1[P, C <: Children, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: PrimaryProxy[P, C, Ctx, Step],
      inline f    : Ctx => VdomNode,
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT],
      inline r    : Reusability[Ctx]): Component[P, CT] =
    ${ renderWithReuse1Workaround[P, C, Ctx, Step, CT]('proxy, 'f, 's, 'r) }

  inline def renderWithReuseDebug1[P, C <: Children, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: PrimaryProxy[P, C, Ctx, Step],
      inline f    : Ctx => VdomNode,
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT],
      inline r    : Reusability[Ctx]): Component[P, CT] =
    ${ renderWithReuseDebug1Workaround[P, C, Ctx, Step, CT]('proxy, 'f, 's, 'r) }

  def renderWithReuse1Workaround[P, C <: Children, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[PrimaryProxy[P, C, Ctx, Step]],
      f    : Expr[Ctx => VdomNode],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      r    : Expr[Reusability[Ctx]])
      (using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], Step: Type[Step]): Expr[Component[P, CT]] =
    _renderWithReuse1(proxy, f, s, r, false)

  def renderWithReuseDebug1Workaround[P, C <: Children, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[PrimaryProxy[P, C, Ctx, Step]],
      f    : Expr[Ctx => VdomNode],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      r    : Expr[Reusability[Ctx]])
     (using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], Step: Type[Step]): Expr[Component[P, CT]] =
    _renderWithReuse1(proxy, f, s, r, true)

  def _renderWithReuse1[P, C <: Children, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      proxy   : Expr[PrimaryProxy[P, C, Ctx, Step]],
      renderFn: Expr[Ctx => VdomNode],
      summoner: Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      r       : Expr[Reusability[Ctx]],
      debug   : Boolean)
      (using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], Step: Type[Step]): Expr[Component[P, CT]] = {
    import quotes.reflect.*
    val self = proxy.asInstanceOf[Expr[PrimaryApi[P, C, Ctx, Step]]]
    apply[P, C, CT](
      macroApplication = self.asTerm,
      ctxType          = TypeTree.of[Ctx],
      summoner         = summoner,
      debug            = debug,
      renderStep       = HookStep("renderRRWithReuse", Nil, List(List(renderFn.asTerm), List(summoner.asTerm, r.asTerm))),
      giveUp           = () => '{ $self.renderWithReuse($renderFn)($summoner, $r) },
    )
  }

  // -------------------------------------------------------------------------------------------------------------------

  inline def renderWithReuse1C[P, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: ComponentPCMacros[P],
      inline f    : (P, PropsChildren) => VdomNode,
      inline s    : CtorType.Summoner.Aux[Box[P], Children.Varargs, CT],
      inline r    : Reusability[HookCtx.PC0[P]]): Component[P, CT] =
    ${ renderWithReuse1CWorkaround[P, CT]('proxy, 'f, 's, 'r) }

  inline def renderWithReuseDebug1C[P, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: ComponentPCMacros[P],
      inline f    : (P, PropsChildren) => VdomNode,
      inline s    : CtorType.Summoner.Aux[Box[P], Children.Varargs, CT],
      inline r    : Reusability[HookCtx.PC0[P]]): Component[P, CT] =
    ${ renderWithReuseDebug1CWorkaround[P, CT]('proxy, 'f, 's, 'r) }

  def renderWithReuse1CWorkaround[P, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[ComponentPCMacros[P]],
      f    : Expr[(P, PropsChildren) => VdomNode],
      s    : Expr[CtorType.Summoner.Aux[Box[P], Children.Varargs, CT]],
      r    : Expr[Reusability[HookCtx.PC0[P]]])
      (using q: Quotes, P: Type[P], CT: Type[CT]): Expr[Component[P, CT]] =
    _renderWithReuse1C(proxy, f, s, r, false)

  def renderWithReuseDebug1CWorkaround[P, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[ComponentPCMacros[P]],
      f    : Expr[(P, PropsChildren) => VdomNode],
      s    : Expr[CtorType.Summoner.Aux[Box[P], Children.Varargs, CT]],
      r    : Expr[Reusability[HookCtx.PC0[P]]])
      (using q: Quotes, P: Type[P], CT: Type[CT]): Expr[Component[P, CT]] =
    _renderWithReuse1C(proxy, f, s, r, true)

  def _renderWithReuse1C[P, CT[-p, +u] <: CtorType[p, u]](
      proxy   : Expr[ComponentPCMacros[P]],
      renderFn: Expr[(P, PropsChildren) => VdomNode],
      summoner: Expr[CtorType.Summoner.Aux[Box[P], Children.Varargs, CT]],
      r       : Expr[Reusability[HookCtx.PC0[P]]],
      debug   : Boolean)
      (using q: Quotes, P: Type[P], CT: Type[CT]): Expr[Component[P, CT]] = {
    import quotes.reflect.*
    val self = proxy.asInstanceOf[Expr[HookComponentBuilder.ComponentPC.First[P]]]
    apply[P, Children.Varargs, CT](
      macroApplication = self.asTerm,
      ctxType          = TypeTree.of[HookCtx.PC0[P]],
      summoner         = summoner,
      debug            = debug,
      renderStep       = HookStep("renderRRWithReuse", Nil, List(List(renderFn.asTerm), List(summoner.asTerm, r.asTerm))),
      giveUp           = () => '{ $self.renderWithReuse($renderFn)($summoner, $r) },
    )
  }

  // -------------------------------------------------------------------------------------------------------------------

  inline def renderWithReuse2[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      inline proxy: SecondaryProxy[P, C, Ctx, CtxFn, Step],
      inline f    : CtxFn[VdomNode],
      inline step : Step,
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT],
      inline r    : Reusability[Ctx]): Component[P, CT] =
    ${ renderWithReuse2Workaround[P, C, Ctx, CtxFn, Step, CT]('proxy, 'f, 'step, 's, 'r) }

  inline def renderWithReuseDebug2[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      inline proxy: SecondaryProxy[P, C, Ctx, CtxFn, Step],
      inline f    : CtxFn[VdomNode],
      inline step : Step,
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT],
      inline r    : Reusability[Ctx]): Component[P, CT] =
    ${ renderWithReuseDebug2Workaround[P, C, Ctx, CtxFn, Step, CT]('proxy, 'f, 'step, 's, 'r) }

  def renderWithReuse2Workaround[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[SecondaryProxy[P, C, Ctx, CtxFn, Step]],
      f    : Expr[CtxFn[VdomNode]],
      step : Expr[Step],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      r    : Expr[Reusability[Ctx]])
      (using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], CtxFn: Type[CtxFn], Step: Type[Step]): Expr[Component[P, CT]] =
    _renderWithReuse2(proxy, f, step, s, r, false)

  def renderWithReuseDebug2Workaround[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[SecondaryProxy[P, C, Ctx, CtxFn, Step]],
      f    : Expr[CtxFn[VdomNode]],
      step : Expr[Step],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      r    : Expr[Reusability[Ctx]])
      (using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], CtxFn: Type[CtxFn], Step: Type[Step]): Expr[Component[P, CT]] =
    _renderWithReuse2(proxy, f, step, s, r, true)

  def _renderWithReuse2[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      proxy   : Expr[SecondaryProxy[P, C, Ctx, CtxFn, Step]],
      renderFn: Expr[CtxFn[VdomNode]],
      step    : Expr[Step],
      summoner: Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      r       : Expr[Reusability[Ctx]],
      debug   : Boolean)
      (using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], CtxFn: Type[CtxFn], Step: Type[Step]): Expr[Component[P, CT]] = {
    import quotes.reflect.*
    val self = proxy.asInstanceOf[Expr[SecondaryApi[P, C, Ctx, CtxFn, Step]]]
    apply[P, C, CT](
      macroApplication = self.asTerm,
      ctxType          = TypeTree.of[Ctx],
      summoner         = summoner,
      debug            = debug,
      renderStep       = HookStep("renderRRWithReuse", Nil, List(List(renderFn.asTerm), List(step.asTerm, summoner.asTerm, r.asTerm))),
      giveUp           = () => '{ $self.renderWithReuse($step.squash($renderFn)(_))($summoner, $r) },
    )
  }

  // ===================================================================================================================
  // renderWithReuseBy

  inline def renderWithReuseBy1[P, C <: Children, A, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: PrimaryProxy[P, C, Ctx, Step],
      inline i    : Ctx => A,
      inline f    : A => VdomNode,
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT],
      inline r    : Reusability[A]): Component[P, CT] =
    ${ renderWithReuseBy1Workaround[P, C, A, Ctx, Step, CT]('proxy, 'i, 'f, 's, 'r) }

  inline def renderWithReuseByDebug1[P, C <: Children, A, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: PrimaryProxy[P, C, Ctx, Step],
      inline i    : Ctx => A,
      inline f    : A => VdomNode,
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT],
      inline r    : Reusability[A]): Component[P, CT] =
    ${ renderWithReuseByDebug1Workaround[P, C, A, Ctx, Step, CT]('proxy, 'i, 'f, 's, 'r) }

  def renderWithReuseBy1Workaround[P, C <: Children, A, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[PrimaryProxy[P, C, Ctx, Step]],
      i    : Expr[Ctx => A],
      f    : Expr[A => VdomNode],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      r    : Expr[Reusability[A]])
      (using q: Quotes, P: Type[P], C: Type[C], A: Type[A], CT: Type[CT], Ctx: Type[Ctx], Step: Type[Step]): Expr[Component[P, CT]] =
    _renderWithReuseBy1(proxy, i, f, s, r, false)

  def renderWithReuseByDebug1Workaround[P, C <: Children, A, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[PrimaryProxy[P, C, Ctx, Step]],
      i    : Expr[Ctx => A],
      f    : Expr[A => VdomNode],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      r    : Expr[Reusability[A]])
     (using q: Quotes, P: Type[P], C: Type[C], A: Type[A], CT: Type[CT], Ctx: Type[Ctx], Step: Type[Step]): Expr[Component[P, CT]] =
    _renderWithReuseBy1(proxy, i, f, s, r, true)

  def _renderWithReuseBy1[P, C <: Children, A, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      proxy   : Expr[PrimaryProxy[P, C, Ctx, Step]],
      i       : Expr[Ctx => A],
      f       : Expr[A => VdomNode],
      summoner: Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      r       : Expr[Reusability[A]],
      debug   : Boolean)
      (using q: Quotes, P: Type[P], C: Type[C], A: Type[A], CT: Type[CT], Ctx: Type[Ctx], Step: Type[Step]): Expr[Component[P, CT]] = {
    import quotes.reflect.*
    val self = proxy.asInstanceOf[Expr[PrimaryApi[P, C, Ctx, Step]]]
    apply[P, C, CT](
      macroApplication = self.asTerm,
      ctxType          = TypeTree.of[Ctx],
      summoner         = summoner,
      debug            = debug,
      renderStep       = HookStep("renderRRWithReuseBy", List(TypeTree.of[A]), List(List(i.asTerm), List(f.asTerm), List(summoner.asTerm, r.asTerm))),
      giveUp           = () => '{ $self.renderWithReuseBy($i)($f)($summoner, $r) },
    )
  }

  // -------------------------------------------------------------------------------------------------------------------

  inline def renderWithReuseBy1C[P, A, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: ComponentPCMacros[P],
      inline i    : (P, PropsChildren) => A,
      inline f    : A => VdomNode,
      inline s    : CtorType.Summoner.Aux[Box[P], Children.Varargs, CT],
      inline r    : Reusability[A]): Component[P, CT] =
    ${ renderWithReuseBy1CWorkaround[P, A, CT]('proxy, 'i, 'f, 's, 'r) }

  inline def renderWithReuseByDebug1C[P, A, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: ComponentPCMacros[P],
      inline i    : (P, PropsChildren) => A,
      inline f    : A => VdomNode,
      inline s    : CtorType.Summoner.Aux[Box[P], Children.Varargs, CT],
      inline r    : Reusability[A]): Component[P, CT] =
    ${ renderWithReuseByDebug1CWorkaround[P, A, CT]('proxy, 'i, 'f, 's, 'r) }

  def renderWithReuseBy1CWorkaround[P, A, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[ComponentPCMacros[P]],
      i    : Expr[(P, PropsChildren) => A],
      f    : Expr[A => VdomNode],
      s    : Expr[CtorType.Summoner.Aux[Box[P], Children.Varargs, CT]],
      r    : Expr[Reusability[A]])
      (using q: Quotes, P: Type[P], A: Type[A], CT: Type[CT]): Expr[Component[P, CT]] =
    _renderWithReuseBy1C(proxy, i, f, s, r, false)

  def renderWithReuseByDebug1CWorkaround[P, A, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[ComponentPCMacros[P]],
      i    : Expr[(P, PropsChildren) => A],
      f    : Expr[A => VdomNode],
      s    : Expr[CtorType.Summoner.Aux[Box[P], Children.Varargs, CT]],
      r    : Expr[Reusability[A]])
      (using q: Quotes, P: Type[P], A: Type[A], CT: Type[CT]): Expr[Component[P, CT]] =
    _renderWithReuseBy1C(proxy, i, f, s, r, true)

  def _renderWithReuseBy1C[P, A, CT[-p, +u] <: CtorType[p, u]](
      proxy   : Expr[ComponentPCMacros[P]],
      i       : Expr[(P, PropsChildren) => A],
      f       : Expr[A => VdomNode],
      summoner: Expr[CtorType.Summoner.Aux[Box[P], Children.Varargs, CT]],
      r       : Expr[Reusability[A]],
      debug   : Boolean)
      (using q: Quotes, P: Type[P], A: Type[A], CT: Type[CT]): Expr[Component[P, CT]] = {
    import quotes.reflect.*
    val self = proxy.asInstanceOf[Expr[HookComponentBuilder.ComponentPC.First[P]]]
    apply[P, Children.Varargs, CT](
      macroApplication = self.asTerm,
      ctxType          = TypeTree.of[HookCtx.PC0[P]],
      summoner         = summoner,
      debug            = debug,
      renderStep       = HookStep("renderRRWithReuseBy", List(TypeTree.of[A]), List(List(i.asTerm), List(f.asTerm), List(summoner.asTerm, r.asTerm))),
      giveUp           = () => '{ $self.renderWithReuseBy($i)($f)($summoner, $r) },
    )
  }

  // -------------------------------------------------------------------------------------------------------------------

  inline def renderWithReuseBy2[P, C <: Children, A, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      inline proxy: SecondaryProxy[P, C, Ctx, CtxFn, Step],
      inline i    : CtxFn[A],
      inline f    : A => VdomNode,
      inline step : Step,
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT],
      inline r    : Reusability[A]): Component[P, CT] =
    ${ renderWithReuseBy2Workaround[P, C, A, Ctx, CtxFn, Step, CT]('proxy, 'i, 'f, 'step, 's, 'r) }

  inline def renderWithReuseByDebug2[P, C <: Children, A, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      inline proxy: SecondaryProxy[P, C, Ctx, CtxFn, Step],
      inline i    : CtxFn[A],
      inline f    : A => VdomNode,
      inline step : Step,
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT],
      inline r    : Reusability[A]): Component[P, CT] =
    ${ renderWithReuseByDebug2Workaround[P, C, A, Ctx, CtxFn, Step, CT]('proxy, 'i, 'f, 'step, 's, 'r) }

  def renderWithReuseBy2Workaround[P, C <: Children, A, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[SecondaryProxy[P, C, Ctx, CtxFn, Step]],
      i    : Expr[CtxFn[A]],
      f    : Expr[A => VdomNode],
      step : Expr[Step],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      r    : Expr[Reusability[A]])
      (using q: Quotes, P: Type[P], C: Type[C], A: Type[A], CT: Type[CT], Ctx: Type[Ctx], CtxFn: Type[CtxFn], Step: Type[Step]): Expr[Component[P, CT]] =
    _renderWithReuseBy2(proxy, i, f, step, s, r, false)

  def renderWithReuseByDebug2Workaround[P, C <: Children, A, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[SecondaryProxy[P, C, Ctx, CtxFn, Step]],
      i    : Expr[CtxFn[A]],
      f    : Expr[A => VdomNode],
      step : Expr[Step],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      r    : Expr[Reusability[A]])
      (using q: Quotes, P: Type[P], C: Type[C], A: Type[A], CT: Type[CT], Ctx: Type[Ctx], CtxFn: Type[CtxFn], Step: Type[Step]): Expr[Component[P, CT]] =
    _renderWithReuseBy2(proxy, i, f, step, s, r, true)

  def _renderWithReuseBy2[P, C <: Children, A, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      proxy   : Expr[SecondaryProxy[P, C, Ctx, CtxFn, Step]],
      i       : Expr[CtxFn[A]],
      f       : Expr[A => VdomNode],
      step    : Expr[Step],
      summoner: Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      r       : Expr[Reusability[A]],
      debug   : Boolean)
      (using q: Quotes, P: Type[P], C: Type[C], A: Type[A], CT: Type[CT], Ctx: Type[Ctx], CtxFn: Type[CtxFn], Step: Type[Step]): Expr[Component[P, CT]] = {
    import quotes.reflect.*
    val self = proxy.asInstanceOf[Expr[SecondaryApi[P, C, Ctx, CtxFn, Step]]]
    apply[P, C, CT](
      macroApplication = self.asTerm,
      ctxType          = TypeTree.of[Ctx],
      summoner         = summoner,
      debug            = debug,
      renderStep       = HookStep("renderRRWithReuseBy", List(TypeTree.of[A]), List(List(i.asTerm), List(f.asTerm), List(summoner.asTerm, r.asTerm))),
      giveUp           = () => '{ $self.renderWithReuseBy($step.squash($i)(_))($f)($summoner, $r) },
    )
  }

  // ===================================================================================================================

  private def HookMacrosImpl(qq: Quotes): HookMacrosImpl { val q: qq.type } =
    new HookMacrosImpl {
      override implicit val q: qq.type = qq
    }

  @nowarn("msg=Consider using canonical type reference .+Underlying instead")
  private trait HookMacrosImpl extends AbstractHookMacros {
    import AbstractHookMacros._

    implicit val q: Quotes
    import q.reflect.*
    import scala.quoted.{Type => Tpe}

    // TODO: Move into microlibs
    extension (self: q.reflect.TypeTree) {
      @targetName("typeTree_asTypeOf")
      def asTypeOf[A <: AnyKind]: Tpe[A] =
        self.asType.asInstanceOf[Tpe[A]]
    }
    extension (self: q.reflect.TypeRepr) {
      @targetName("typeRepr_asTypeOf")
      def asTypeOf[A <: AnyKind]: Tpe[A] =
        self.asType.asInstanceOf[Tpe[A]]
    }

    override type Expr[+A] = scala.quoted.Expr[A]
    override type Ref      = UntypedValDef.WithQuotes[q.type]
    override type Stmt     = q.reflect.Statement
    override type Term     = q.reflect.Term
    override type Type[A]  = Tpe[A]
    override type TypeTree = q.reflect.TypeTree

    override protected def asTerm    [A] = _.asTerm
    override protected def Expr      [A] = _.asExprOf[Any].asInstanceOf[Expr[A]]
    override protected def refToTerm     = _.ref
    override           def showCode      = _.show(using q.reflect.Printer.TreeShortCode)
    override protected def showRaw       = _.show(using q.reflect.Printer.TreeStructure)
    override protected def typeOfTerm    = _.tpe.asTypeTree
    override protected def uninline      = _uninline
    override protected def unitTerm      = '{ () }
    override protected def unitType      = Tpe.of[Unit]
    override protected def wrap          = (s, b) => Block(s.toList, b)

    // TODO: Move into microlibs (and make tailrec and non-recursive versions)
    @tailrec
    private def _uninline(t: Term): Term =
      t match {
        case Inlined(_, _, e) => _uninline(e)
        case _                => t
      }

    override protected def call = (function, args, betaReduce) => {
      val f = extractFunction(function)
      val t = Apply(Select.unique(f, "apply"), args)
      if (betaReduce)
        Term.betaReduce(t).getOrElse(t)
      else
        t
    }

    @tailrec
    private def extractFunction(term: Term): Term =
      term match {
        case Inlined(None, Nil, f) => extractFunction(f)
        case Block(Nil, expr)      => extractFunction(expr)
        case f                     => f
      }

    override protected def isUnit =
      _.tpe.asType match {
        case '[Unit] => true
        case _       => false
      }

    override protected def Type[A] =
      _.asTypeOf[A]

    override protected val rewriterBridge: RewriterBridge =
      HookRewriter.Bridge[Stmt, Term, Ref](
        apply     = Apply(_, _),
        hookCtx   = (usesChildren, args) => {
                      if (usesChildren) {
                        // .patch below removes the PropsChildren arg
                        val targs = args.patch(1, Nil, 1).map(_.tpe)
                        Select.overloaded('{HookCtx.withChildren}.asTerm, "apply", targs, args)
                      } else
                        Select.overloaded('{HookCtx}.asTerm, "apply", args.map(_.tpe), args)
                    },
        refToTerm = _.ref,
        scalaVer  = 3,
        valDef    = (n, t, l) => {
                      val flags = if (l) Flags.Lazy else Flags.EmptyFlags
                      val r = untypedValDef(n, t.tpe, flags)(t)
                      (r.valDef, r)
                    },
      )

    override val ApplyLike = new ApplyExtractor {
      override def unapply(a: Term) = a match {
        case Apply(x, y) => Some((x, y))
        case _           => None
      }
    }

    override val TypeApplyLike = new TypeApplyExtractor {
      override def unapply(a: Term) =  a match {
        case TypeApply(x, y) => Some((x, y))
        case _               => None
      }
    }

    override val SelectLike = new SelectExtractor {
      override def unapply(a: Term) =  a match {
        case Select(x, y) => Some((x, y))
        case _            => None
      }
    }

    override val FunctionLike = new FunctionExtractor {
      @tailrec
      override def unapply(function: Term) = function match {

        case Apply(TypeApply(f, args), _) if f.tpe.show.startsWith("scala.scalajs.js.Any.toFunction") =>
          Some(args.size - 1)

        case Block(List(DefDef(_, List(TermParamClause(params)), _, Some(_))), Closure(Ident(_), _)) =>
          Some(params.size)

        case Block(Nil, expr) =>
          unapply(expr)

        case Block(stmts1, Block(stmts2, expr)) =>
          val stmts = if (stmts2.isEmpty) stmts1 else stmts1 ::: stmts2
          unapply(Block(stmts, expr))

        case i@ Ident(_) =>
          i.symbol.tree match {
            case d: DefDef => byTypeRepr(d.returnTpt.tpe)
            case v: ValDef => byTypeRepr(v.tpt.tpe)
            case _         => None
          }

        case _ =>
          None
      }

      private def byTypeRepr(t: TypeRepr): Option[Success] = t match {
        case AppliedType(TypeRef(ThisType(_), f), args) if f.startsWith("Function") =>
          Some(args.size - 1)
        case _ =>
          None
      }
    }

    override protected def custom[I, O] = implicit (ti, to, hook, i) =>
      '{ $hook.unsafeInit($i) }

    override protected def customArg[Ctx, Arg] = implicit (tc, ta, hookArg, ctx) =>
      '{ $hookArg.convert($ctx) }

    override protected def hookDepsEmptyArray =
      '{ js.Array[Any]() }

    override protected def hookDepsIntArray1 = i =>
      '{ js.Array[Int]($i) }

    override protected def hooksVar[A] = implicit (tpe, body) =>
      '{ Hooks.Var[$tpe]($body) }

    override protected def jsComponentRawMountedType[P <: js.Object, S <: js.Object] = implicit (tpeP, tpeS) =>
      Tpe.of[JsComponent.RawMounted[P, S]]

    override protected def jsComponentRawMountedTypeWithFacade[P <: js.Object, S <: js.Object, F] = implicit (tpeP, tpeS, tpeF) =>
      Tpe.of[JsComponent.RawMounted[P, S] with F]

    override protected def none[A] = implicit tpe =>
      '{ None }

    override protected def optionType[A] = {
      case '[a] => Tpe.of[Option[a]].asInstanceOf[Type[Option[A]]]
    }

    override protected def refFromJs[A] = implicit (ref, tpe) =>
      '{ ScalaRef.fromJs($ref) }

    override protected def refMapJsMounted[P <: js.Object, S <: js.Object] = implicit (ref, tpeP, tpeS) =>
      '{ $ref.map(JsComponent.mounted(_)) }

    override protected def refMapJsMountedWithFacade[P <: js.Object, S <: js.Object, F <: js.Object] = implicit (ref, tpeP, tpeS, tpeF) =>
      '{ $ref.map(JsComponent.mounted[P, S](_).addFacade[F]) }

    override protected def refMapMountedImpure[P, S, B] = implicit (ref, tpeS, tpeP, tpeB) =>
      '{ $ref.map(_.mountedImpure) }

    override protected def refWithJsComponentArgHelper[F[_], A[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]] =
      implicit (r, a, ttF, ttA, tpeP1, tpeS1, ttCT1, tpeR, tpeP0, tpeS0) => {
        implicit val tpeF = ttF.asTypeOf[F]
        implicit val tpeA = ttA.asTypeOf[A]
        implicit val tpeC = ttCT1.asTypeOf[CT1]
        '{ AbstractHookMacros.helperRefToJsComponent($r, $a) }
      }

    override protected def refNarrowOption[A, B <: A] = implicit (ref, ct, tpeA, tpeB) =>
      '{ $ref.narrowOption[$tpeB]($ct) }

    override protected def refToComponentInject[P, S, B, CT[-p, +u] <: CtorType[p, u]] = implicit (c, r, tpeS, tpeP, tpeB, ttCT) => {
      implicit val tpeCT = ttCT.asTypeOf[CT]
      '{ AbstractHookMacros.helperRefToComponentInject($c, $r) }
    }

    override protected def reusableDepsLogic[D] = implicit (d, s, r, tpeD) =>
      '{ CustomHook.reusableDepsLogic[D]($d)($s)($r) }

    override protected def reusableDepStateRev = rds =>
      '{ $rds.rev }

    override protected def reusableDepStateType[D] = {
      case '[d] => Tpe.of[ReusableDepState[d]].asInstanceOf[Type[ReusableDepState[D]]]
    }

    override protected def reusableDepStateValue[D] = implicit (rds, tpeD) =>
      '{ $rds.value }

    override protected def reusableValueByInt[A] = implicit (i, a, tpeA) =>
      '{ Reusable.implicitly($i).withValue($a) }

    override protected def topNodeType =
      Tpe.of[TopNode]

    override protected def scalaComponentRawMountedType[P, S, B] = implicit (tpeS, tpeP, tpeB) =>
      Tpe.of[ScalaComponent.RawMounted[P, S, B]]

    override protected def scalaFn0[A] = implicit (tpe, body) =>
      '{ () => $body }

    override protected def useCallback[F <: js.Function] = implicit (f, deps, tpe) =>
      '{ React.useCallback($f, $deps) }

    override protected def useCallbackArgFromJs[A, J <: js.Function] = implicit (x, j, ta, tj) =>
      '{ $x.fromJs($j) }

    override protected def useCallbackArgToJs[A, J <: js.Function] = implicit (x, a, ta, tj) =>
      '{ $x.toJs($a) }

    override protected def useCallbackArgTypeJs[A, J <: js.Function] = x =>
      TypeSelect(x.asTerm, "J").asTypeOf[J]

    override protected def useContext[A] = implicit (ctx, tpe) =>
      '{ React.useContext($ctx.raw) }

    override protected def useDebugValue = desc =>
      '{ React.useDebugValue[Null](null, _ => $desc) }

    override protected def useEffect = implicit (a, d) =>
      '{ React.useEffect($a, $d) }

    override protected def useEffectArgToJs[A] = implicit (arg, a, tpeA) =>
      '{ $arg.toJs($a) }

    override protected def useForceUpdate1 =
      '{ React.useStateValue(0) }

    override protected def useForceUpdate2 = s =>
      '{ CustomHook.useForceUpdateRaw($s) }

    override protected def useLayoutEffect = implicit (a, d) =>
      '{ React.useLayoutEffect($a, $d) }

    override protected def useMemo[A] = implicit (a, d, tpeA) =>
      '{ React.useMemo(() => $a, $d) }

    override protected def useReducer[S, A] = implicit (r, s, tpeS, tpeA) =>
      '{ React.useReducer[Null, $tpeS, $tpeA](js.Any.fromFunction2($r), null, _ => $s) }

    override protected def useReducerFromJs[S, A] = implicit (raw, tpeS, tpeA) =>
      '{ Hooks.UseReducer.fromJs($raw) }

    override protected def useRef[A] = implicit (a, tpe) =>
      '{ React.useRef($a) }

    override protected def useRefOrNull[A] = implicit (tpe) =>
      '{ React.useRef[$tpe | Null](null) }

    override protected def useRefFromJs[A] = implicit (ref, tpe) =>
      '{ Hooks.UseRef.fromJs($ref) }

    override protected def useStateFn[S] = implicit (tpe, body) =>
      '{ React.useStateFn(() => Box[$tpe]($body)) }

    override protected def useStateValue[S] = implicit (tpe, body) =>
      '{ React.useStateValue(Box[$tpe]($body)) }

    override protected def useStateFromJsBoxed[S] = implicit (tpe, raw) =>
      '{ Hooks.UseState.fromJsBoxed[$tpe]($raw) }

    override protected def useStateWithReuseFromJsBoxed[S] = implicit (tpe, raw, reuse, ct) =>
      '{ Hooks.UseStateWithReuse.fromJsBoxed[$tpe]($raw)($reuse, $ct) }

    override protected def vdomRawNode = vdom =>
      '{ $vdom.rawNode }

    override protected def reusabilityReusable[A] = implicit tpe =>
      '{ Reusable.reusableReusability[A] }

    override protected def reusableMap[A, B] = implicit (r, f, tpeA, tpeB) =>
      '{ $r.map($f) }

    override protected def reusableType[A] = {
      case '[a] => Tpe.of[Reusable[a]].asInstanceOf[Type[Reusable[A]]]
    }

    override protected def reusableValue[A] = implicit (r, tpe) =>
      '{ $r.value }

    override protected def shouldComponentUpdateComponent = (rev, render) =>
      '{ ShouldComponentUpdateComponent($rev, () => $render) }

    override protected def vdomNodeType =
      Tpe.of[VdomNode]
  }

  // ===================================================================================================================

  private def apply[P, C <: Children, CT[-p, +u] <: CtorType[p, u]](
      using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT])(
      macroApplication: q.reflect.Term,
      ctxType         : q.reflect.TypeTree,
      summoner        : Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      debug           : Boolean,
      renderStep      : HookStep[q.reflect.Term, q.reflect.TypeTree],
      giveUp          : () => Expr[Component[P, CT]]): Expr[Component[P, CT]] = {

    import quotes.reflect.*

    val hookMacros = HookMacrosImpl(q)

    import hookMacros.log
    log.enabled = debug
    log.header()
    // log("self", macroApplication.show)

    def onFailure(msg: String): Expr[Component[P, CT]] = {
      import Console._
      log(RED_B + WHITE + "Giving up. " + msg + RESET)
      report.warning(msg)
      giveUp()
    }

    val result: Expr[Component[P, CT]] =
      try {

        val rewriteAttempt =
          for {
            hookDefn <- hookMacros.parse(macroApplication)
            rewriter <- hookMacros.rewriteComponent(hookDefn + renderStep)
          } yield rewriter

        rewriteAttempt match {

          case Right(rewriter) =>
            type JsProps = Box[P] with facade.PropsWithChildren

            def newBody(props: Expr[JsProps]): Expr[React.Node] = {
              val children = typedValDef[PropsChildren]("children", Flags.EmptyFlags)('{ PropsChildren.fromRawProps($props) })
              val ctx = hookMacros.rewriterCtx(
                props        = '{ $props.unbox }.asTerm,
                initChildren = children.valDef,
                children     = children.ref.asTerm,
                ctxType      = ctxType,
              )
              rewriter(ctx)
            }

            '{
              val rawComponent: JsFn.RawComponent[Box[P]] = props => ${newBody('props)}
              ScalaFn.fromBoxed(JsFn.fromJsFn[Box[P], C](rawComponent)($summoner))
            }

          case Left(err) =>
            onFailure(err())
        }

      } catch {
        case err: Throwable =>
          // err.printStackTrace()
          onFailure(err.getMessage())
      }

    log.footer(hookMacros.showCode(result.asTerm))
    result
  }

}
