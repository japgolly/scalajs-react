package japgolly.scalajs.react.hooks

import japgolly.microlibs.compiletime.MacroUtils
import japgolly.scalajs.react.{Children, CtorType}
import japgolly.scalajs.react.component.ScalaFn.Component
import japgolly.scalajs.react.hooks.Api._
import japgolly.scalajs.react.internal.Box
import japgolly.scalajs.react.vdom.VdomNode
import scala.reflect.macros.blackbox.Context

object HookMacros {

  trait ApiSecondaryWithRenderMacros[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]] {
      self: PrimaryWithRender[P, C, Ctx, Step] with Secondary[Ctx, CtxFn, Step] =>

    final def render(f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
      // Without macros: render(step.squash(f)(_))
      macro HookMacros.render[P, C, Ctx, CtxFn, Step]
  }
}

@annotation.nowarn("cat=unused") // TODO: remove
class HookMacros(val c: Context) extends MacroUtils {
  import c.universe._

  def render[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]]
            (f: c.Tree)(step: c.Tree, s: c.Tree): c.Tree = {

    var debug = false // showCode(c.macroApplication).contains("counter.value")

    def println(args: Any*): Unit =
      if (debug) System.out.println(args.mkString)

    println("="*120)
    println()

    val self = c.prefix

    val result = q"""
    {
      val f = $step.squash($f)
      $self.render(f)($s)
    }
    """

    if (debug)
      println("RESULT:\n" + showCode(result))

    println()
    println("="*120)

    result
  }
}
