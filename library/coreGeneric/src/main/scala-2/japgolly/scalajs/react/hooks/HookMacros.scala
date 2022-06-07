package japgolly.scalajs.react.hooks

import japgolly.microlibs.compiletime.MacroUtils
import japgolly.scalajs.react.component.ScalaFn.Component
import japgolly.scalajs.react.hooks.Api._
import japgolly.scalajs.react.internal.Box
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, CtorType}
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

  private def Box          : Tree = q"_root_.japgolly.scalajs.react.internal.Box"
  private def Box(t: Type) : Type = appliedType(c.typeOf[Box[_]], t)
  private def HookCtx      : Tree = q"_root_.japgolly.scalajs.react.hooks.HookCtx"
  private def Hooks        : Tree = q"_root_.japgolly.scalajs.react.hooks.Hooks"
  private def JsFn         : Tree = q"_root_.japgolly.scalajs.react.component.JsFn"
  private def PropsChildren: Tree = q"_root_.japgolly.scalajs.react.PropsChildren"
  private def React        : Tree = q"_root_.japgolly.scalajs.react.facade.React"
  private def ScalaFn      : Tree = q"_root_.japgolly.scalajs.react.component.ScalaFn"

  private final class HookMacrosImpl extends AbstractHookMacros {
    import AbstractHookMacros._

    override type Tree = c.universe.Tree

    override type Apply = c.universe.Apply
    override val Apply = new ApplyExtractor {
      override def apply(fun: Tree, args: List[Tree]) = c.universe.Apply(fun, args)
      override def unapply(a: Apply) = c.universe.Apply.unapply(a)
    }

    override type TypeApply = c.universe.TypeApply
    override val TypeApply = new TypeApplyExtractor {
      override def apply(fun: Tree, args: List[Tree]) = c.universe.TypeApply(fun, args)
      override def unapply(a: TypeApply) = c.universe.TypeApply.unapply(a)
    }

    override type Select = c.universe.Select
    override val Select = new SelectExtractor {
      override def apply(qualifier: Tree, name: String) = c.universe.Select(qualifier, TermName(name))
      override def unapply(a: Select) = c.universe.Select.unapply(a).map(x => (x._1, x._2.toString))
    }

    override type Function = c.universe.Function
    override val Function = new FunctionExtractor {
      override def unapply(f: Function) = c.universe.Function.unapply(f)
    }

    override def showRaw(t: Tree): String = c.universe.showRaw(t)
    override def showCode(t: Tree): String = c.universe.showCode(t)

    override type HookRef = TermName

    override def rewriter() =
      new HookRewriter[Tree, TermName] {
        override protected def props                          = q"props.unbox"
        override protected def initChildren                   = q"val children = $PropsChildren.fromRawProps(props)"
        override protected def propsChildren                  = q"children"
        override protected def Apply(t: Tree, as: List[Tree]) = c.universe.Apply(t, as)
        override protected def hookCtx(withChildren: Boolean) = if (withChildren) q"$HookCtx.withChildren" else HookCtx
        override protected def Ident(name: TermName)          = c.universe.Ident(name)
        override protected def TermName(name: String)         = c.universe.TermName(name)
        override protected def ValDef(n: TermName, t: Tree)   = q"val $n = $t"
        override def wrap(body: Tree)                         = q"..$stmts; $body"

      }

    override def call(function: Tree, args: List[Tree]): Tree = {
      import internal._

      function match {
        case Function(params, body) =>

          // From scala/test/files/run/macro-range/Common_1.scala
          class TreeSubstituter(from: List[Symbol], to: List[Tree]) extends Transformer {
            override def transform(tree: Tree): Tree = tree match {
              case Ident(_) =>
                def subst(from: List[Symbol], to: List[Tree]): Tree =
                  if (from.isEmpty) tree
                  else if (tree.symbol == from.head) to.head.duplicate
                  else subst(from.tail, to.tail);
                subst(from, to)
              case _ =>
                val tree1 = super.transform(tree)
                if (tree1 ne tree) setType(tree1, null)
                tree1
            }
          }
          val t = new TreeSubstituter(params.map(_.symbol), args)
          t.transform(body)

        case _ =>
          Apply(Select(function, "apply"), args)
      }
    }

    override protected def vdomRawNode(vdom: Tree) =
      q"$vdom.rawNode"

    override protected def useStateFn(tpe: Tree, body: Tree) =
      q"$React.useStateFn(() => $Box[$tpe]($body))"

    override protected def useStateFromJsBoxed(tpe: Tree, raw: HookRef) =
      q"$Hooks.UseState.fromJsBoxed[$tpe]($raw)"

    override protected def useStateWithReuseFromJsBoxed(tpe: Tree, raw: HookRef, reuse: Tree, ct: Tree) =
      q"$Hooks.UseStateWithReuse.fromJsBoxed[$tpe]($raw)($reuse, $ct)"
  }

  // ===================================================================================================================

  def render[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]]
            (f: c.Tree)(step: c.Tree, s: c.Tree)
            (implicit P: c.WeakTypeTag[P], C: c.WeakTypeTag[C]): c.Tree = {

    val hookMacros = new HookMacrosImpl
    import hookMacros.log

    log.enabled = showCode(c.macroApplication).contains("DEBUG") // TODO: DELETE
    log.header()

    val result: Tree =
      hookMacros(c.macroApplication) match {

        case Right(newBody) =>
          c.untypecheck(q"""
            val rawComponent: $JsFn.RawComponent[${Box(P)}] = props => $newBody
            $ScalaFn.fromBoxed($JsFn.fromJsFn[${Box(P)}, $C](rawComponent)($s))
          """)

        case Left(err) =>
          log(err()) // TODO: make a proper warning?
          val self = c.prefix
          q"""
            val f = $step.squash($f)
            $self.render(f)($s)
          """
    }

    log.footer(showCode(result))
    result
  }
}
