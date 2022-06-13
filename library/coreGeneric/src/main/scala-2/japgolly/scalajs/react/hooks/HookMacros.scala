package japgolly.scalajs.react.hooks

import japgolly.microlibs.compiletime.MacroUtils
import japgolly.scalajs.react.hooks.Api._
import japgolly.scalajs.react.internal.Box
import japgolly.scalajs.react.Children
import scala.reflect.macros.blackbox.Context

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

    override type Expr[A]  = Term
    override type HookRef  = TermName
    override type Stmt     = c.universe.Tree
    override type Term     = c.universe.Tree
    override type Type[A]  = c.universe.Type
    override type TypeTree = c.universe.TypeTree

    override protected def asTerm[A](e: Expr[A]) = e
    override protected def Expr[A](t: Term) = t
    override protected def hookRefToTerm(r: HookRef) = Ident(r)
    override protected def Type[A](t: TypeTree) = t.tpe
    override protected def typeOfTerm(t: Term) = c.universe.TypeTree(t.tpe)

    override val ApplyLike = new ApplyExtractor {
      override def unapply(a: Term) = a match {
        case Apply(x, y) => Some((x, y))
        case _ => None
      }
    }

    override val TypeApplyLike = new TypeApplyExtractor {
      override def unapply(a: Term) = a match {
        case TypeApply(x, y) => Some((x, y.map(t => TypeTree(t.tpe))))
        case _ => None
      }
    }

    override val SelectLike = new SelectExtractor {
      override def unapply(a: Term) = a match {
        case Select(x, y) => Some((x, y.toString))
        case _ => None
      }
    }

    override val FunctionLike = new FunctionExtractor {
      override def unapply(a: Term) = a match {

        case Function(params, _) =>
          Some(params.size)

        case Apply(ta@ TypeApply(_, targs), _) if ta.tpe.resultType.typeSymbol.name.toString.startsWith("Function") =>
          Some(targs.size - 1)

        case s: Select =>
          s.tpe match {
            case TypeRef(_, f, args) if f.name.toString.startsWith("Function") => Some(args.size - 1)
            case _ => None
          }

        case _ =>
          None
      }
    }

    override def showRaw(t: Term): String = c.universe.showRaw(t)
    override def showCode(t: Term): String = c.universe.showCode(t)

    override def rewriter(rc: RewriterCtx) =
      new HookRewriter[Tree, Term, HookRef] {
        override protected val ctx                            = rc
        override protected def Apply(t: Term, as: List[Term]) = c.universe.Apply(t, as)
        override protected def hookRefToTerm(r: HookRef)      = Ident(r)
        override def valDef(n: String, t: Term)               = { val r = TermName(n); this += q"val $r = $t"; r }
        override def wrap(body: Term)                         = q"..$stmts; $body"

        override protected def hookCtx(withChildren: Boolean, args: List[Term]) = {
          val obj = if (withChildren) q"$HookCtx.withChildren" else HookCtx
          Apply(obj, args)
        }
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
          Apply(Select(function, TermName("apply")), args)
      }
    }

    override protected def isUnit(t: TypeTree): Boolean =
      t.tpe == typeOf[Unit]

    override protected def unitTerm =
      q"()"

    override protected def unitType =
      c.universe.definitions.UnitTpe

    override protected def custom[I, O] = (_, _, hook, i) =>
      q"$hook.unsafeInit($i)"

    override protected def customArg[Ctx, Arg] = (_, _, hookArg, ctx) =>
      q"$hookArg.convert($ctx)"

    override protected def useStateFn[S] = (tpe, body) =>
      q"$React.useStateFn(() => $Box[$tpe]($body))"

    override protected def useStateFromJsBoxed[S] = (tpe, raw) =>
      q"$Hooks.UseState.fromJsBoxed[$tpe]($raw)"

    override protected def useStateWithReuseFromJsBoxed[S] = (tpe, raw, reuse, ct) =>
      q"$Hooks.UseStateWithReuse.fromJsBoxed[$tpe]($raw)($reuse, $ct)"

    override protected def vdomRawNode = vdom =>
      q"$vdom.rawNode"
  }

  // ===================================================================================================================

  def render[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]]
            (f: c.Tree)(step: c.Tree, s: c.Tree)
            (implicit P: c.WeakTypeTag[P], C: c.WeakTypeTag[C]): c.Tree =
    _render(f, step, s, false)(P, C)

  def renderDebug[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]]
                 (f: c.Tree)(step: c.Tree, s: c.Tree)
                 (implicit P: c.WeakTypeTag[P], C: c.WeakTypeTag[C]): c.Tree =
    _render(f, step, s, true)(P, C)

  def _render[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]]
             (f: c.Tree, step: c.Tree, s: c.Tree, debug: Boolean)
             (implicit P: c.WeakTypeTag[P], C: c.WeakTypeTag[C]): c.Tree = {

    val hookMacros = new HookMacrosImpl
    import hookMacros.log

    log.enabled = debug
    log.header()

    val rewriteAttempt =
      for {
        p <- hookMacros.parse(c.macroApplication)
        r <- hookMacros.rewriteComponent(p)
      } yield hookMacros.applyRewrite(r)

    val result: Tree =
      rewriteAttempt match {

        case Right(rewrite) =>
          val ctx = hookMacros.rewriterCtx(
            props        = q"props.unbox",
            initChildren = q"val children = $PropsChildren.fromRawProps(props)",
            children     = q"children",
          )
          val newBody = rewrite(ctx)
          c.untypecheck(q"""
            val rawComponent: $JsFn.RawComponent[${Box(P)}] = props => $newBody
            $ScalaFn.fromBoxed($JsFn.fromJsFn[${Box(P)}, $C](rawComponent)($s))
          """)

        case Left(err) =>
          c.warning(c.enclosingPosition, err())
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
