package japgolly.scalajs.react.hooks

import japgolly.microlibs.compiletime.MacroEnv._
import japgolly.scalajs.react.{Children, CtorType}
import japgolly.scalajs.react.component.ScalaFn.Component
import japgolly.scalajs.react.hooks.Api._
import japgolly.scalajs.react.internal.Box
import japgolly.scalajs.react.vdom.VdomNode
import scala.quoted.*
import scala.scalajs.js

object HookMacros {

  trait ApiSecondaryWithRenderMacros[P, C <: Children, Ctx, CtxFn[_], _Step <: SubsequentStep[Ctx, CtxFn]] {
      self: PrimaryWithRender[P, C, Ctx, _Step] with Secondary[Ctx, CtxFn, _Step] =>

    inline final def render(inline f: CtxFn[VdomNode])(implicit inline step: Step, inline s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
      // Without macros: render(step.squash(f)(_))
      renderWorkaround[P, C, Ctx, CtxFn, Step, s.CT](this, f, step, s)
  }

  // ===================================================================================================================

  // https://github.com/lampepfl/dotty/issues/15357
  inline def renderWorkaround[
        P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]
      ](inline self: PrimaryWithRender[P, C, Ctx, Step] with Secondary[Ctx, CtxFn, Step],
        inline f    : CtxFn[VdomNode],
        inline step : Step,
        inline s    : CtorType.Summoner.Aux[Box[P], C, CT],
       ): Component[P, CT] =
    ${ renderMacro[P, C, Ctx, CtxFn, Step, CT]('self, 'f, 'step, 's) }

  def renderMacro[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]]
      (self: Expr[PrimaryWithRender[P, C, Ctx, Step] with Secondary[Ctx, CtxFn, Step]],
      f    : Expr[CtxFn[VdomNode]],
      step : Expr[Step],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
     )(using Quotes, Type[P], Type[C], Type[CT], Type[Ctx], Type[CtxFn], Type[Step]): Expr[Component[P, CT]] = {

    import quotes.reflect.*

    var debug = false // showCode(c.macroApplication).contains("counter.value")

    def println(args: Any*): Unit =
      if (debug) System.out.println(args.mkString)

    println("="*120)
    println()

    val result: Expr[Component[P, CT]] =
      '{ $self.render($step.squash($f)(_))($s) }

    if (debug)
      println("RESULT:\n" + result.show)

    println()
    println("="*120)

    result
  }

}
