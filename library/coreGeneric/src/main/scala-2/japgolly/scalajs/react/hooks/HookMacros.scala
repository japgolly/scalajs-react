package japgolly.scalajs.react.hooks

import japgolly.microlibs.compiletime.MacroUtils
import japgolly.scalajs.react.component.ScalaFn.Component
import japgolly.scalajs.react.hooks.Api._
import japgolly.scalajs.react.internal.{Box, MacroLogger}
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, CtorType}
import scala.annotation.{nowarn, tailrec}
import scala.reflect.macros.blackbox.Context

object HookMacros {

  trait ApiSecondaryWithRenderMacros[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]] {
      self: PrimaryWithRender[P, C, Ctx, Step] with Secondary[Ctx, CtxFn, Step] =>

    final def render(f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
      // Without macros: render(step.squash(f)(_))
      macro HookMacros.render[P, C, Ctx, CtxFn, Step]
  }
}

// =====================================================================================================================

class HookMacros(val c: Context) extends MacroUtils {
  import c.universe._

  private implicit def autoTagToType[A](t: c.WeakTypeTag[A]): Type = t.tpe

  private def Box         : Tree = q"_root_.japgolly.scalajs.react.internal.Box"
  private def Box(t: Type): Type = appliedType(c.typeOf[Box[_]], t)
  private def Hooks       : Tree = q"_root_.japgolly.scalajs.react.hooks.Hooks"
  private def JsFn        : Tree = q"_root_.japgolly.scalajs.react.component.JsFn"
  private def React       : Tree = q"_root_.japgolly.scalajs.react.facade.React"
  private def ScalaFn     : Tree = q"_root_.japgolly.scalajs.react.component.ScalaFn"
  private def withHooks          = "withHooks"

  case class HookDefn(propsType: Tree, steps: List[HookStep])
  case class HookStep(name: String, targs: List[Tree], args: List[List[Tree]])

  def render[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]]
            (f: c.Tree)(step: c.Tree, s: c.Tree)
            (implicit P: c.WeakTypeTag[P], C: c.WeakTypeTag[C]): c.Tree = {

    implicit val log = MacroLogger()
    // log.enabled = showCode(c.macroApplication).contains("counter.value")
    log.header()
    log("macroApplication", showRaw(c.macroApplication))

    val self = c.prefix

    val parsed = parseHookDefn(c.macroApplication, Nil, Nil, Nil)

    val inlined = parsed
      .flatMap(inlineHookDefn)
      .map(inlineHookRawComponent[P])
      .map(inlineHookComponent[P, C](_, s))

    val result: Tree =
      inlined match {
        case Right(r) =>
          r
        case Left(e) =>
          log(e())
          q"""
            val f = $step.squash($f)
            $self.render(f)($s)
          """
    }

    log.footer(showCode(result))
    result
  }

  @tailrec
  private def parseHookDefn(tree: Tree, targs: List[Tree], args: List[List[Tree]], steps: List[HookStep])
                           (implicit log: MacroLogger): Either[() => String, HookDefn] =
      tree match {

        case Apply(t, a) =>
           parseHookDefn(t, targs, a :: args, steps)

        case TypeApply(t, a) =>
          if (targs.isEmpty)
            parseHookDefn(t, a, args, steps)
          else
            Left(() => "Multiple type arg clauses found at " + showRaw(tree))

        case Select(t, n) =>
          val name = n.toString
          if (name == withHooks) {
            if (args.nonEmpty)
              Left(() => s"$withHooks called with args when none exepcted: ${args.map(_.map(showCode(_)))}")
            else
              targs match {
                case props :: Nil => Right(HookDefn(props, steps))
                case Nil          => Left(() => s"$withHooks called without targs, unable to discern Props type.")
                case _            => Left(() => s"$withHooks called multiple targs: ${targs.map(showCode(_))}")
              }
          } else {
            val step = HookStep(name, targs, args)
            log(s"Found step '$name'", step)
            parseHookDefn(t, Nil, Nil, step :: steps)
          }

        case _ =>
          Left(() => "Don't know how to parse " + showRaw(tree))
      }

  private type RenderInliner = (Tree, Init) => Tree

  private def inlineHookDefn(h: HookDefn)(implicit log: MacroLogger): Either[() => String, RenderInliner] = {
    val init = new Init("hook" + _, lazyVals = false)
    val it = h.steps.iterator
    var stepId = 0
    var renderStep: HookStep = null
    var hooks = List.empty[TermName]
    while (it.hasNext) {
      val step = it.next()
      if (it.hasNext) {
        stepId += 1
        inlineHookStep(stepId, step, init) match {
          case Right(termName) => hooks ::= termName
          case Left(e) => return Left(e)
        }
      } else
        renderStep = step
    }
    hooks = hooks.reverse

    hookRenderInliner(renderStep, hooks.map(Ident(_))).map { f =>
      (props, init2) => {
        init2 ++= init.stmts
        f(props, init2)
      }
    }
  }

  private def inlineHookStep(stepId: Int, step: HookStep, init: Init)(implicit log: MacroLogger): Either[() => String, TermName] = {
    log("inlineHookStep." + step.name, step)
    step.name match {
      case "useState" =>
        val stateType = step.targs.head
        val arg       = step.args.head.head
        val rawName   = TermName("hook" + stepId + "_raw")
        val name      = TermName("hook" + stepId)
        init += q"val $rawName = $React.useStateFn(() => $Box[$stateType]($arg))"
        init += q"val $name = $Hooks.UseState.fromJsBoxed[$stateType]($rawName)"
        Right(name)

      case _ =>
        Left(() => s"Inlining of hook method '${step.name}' not yet supported.")
    }
  }

  private def hookRenderInliner(step: HookStep, hooks: List[Tree])(implicit log: MacroLogger): Either[() => String, RenderInliner] = {
    log("inlineHookRender." + step.name, step)
    step.name match {
      case "render" =>
        @nowarn("msg=exhaustive") val List(List(renderFn), _) = step.args
        Right { (props, _) =>
          val args = props :: hooks
          Apply(Select(renderFn, TermName("apply")), args)
        }

      case _ =>
        Left(() => s"Inlining of hook render method '${step.name}' not yet supported.")
    }
  }

  private def inlineHookRawComponent[P](renderInliner: RenderInliner)(implicit P: c.WeakTypeTag[P]): Tree = {
    val props_unbox = q"props.unbox"
    val init        = new Init("_i" + _)
    val render1     = renderInliner(props_unbox, init)
    val render2     = init.wrap(q"$render1.rawNode")
    q"(props => $render2): $JsFn.RawComponent[${Box(P)}]"
  }

  private def inlineHookComponent[P, C <: Children](rawComp: Tree, summoner: c.Tree)(implicit P: c.WeakTypeTag[P], C: c.WeakTypeTag[C]): Tree = {
    c.untypecheck(q"""
      val rawComponent = $rawComp
      $ScalaFn.fromBoxed($JsFn.fromJsFn[${Box(P)}, $C](rawComponent)($summoner))
    """)
  }
}
