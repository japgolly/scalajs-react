package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.Reusability
import japgolly.scalajs.react.facade.React
import japgolly.scalajs.react.internal.{Box, MacroLogger}
import japgolly.scalajs.react.vdom.VdomNode
import scala.annotation.{nowarn, tailrec}
import scala.reflect.ClassTag

object AbstractHookMacros {

  // ===================================================================================================================
  // Hook Rewriter

  trait HookRewriterApi[Stmt, Term, HookRef] {
    def +=(stmt: Stmt): Unit
    def args(): List[Term]
    def ctxArg(): Term
    def hookCount(): Int
    def nextHookName(): String
    def registerHook(h: HookRef): Unit
    def useChildren(): Unit
    def usesChildren(): Boolean
    def valDef(name: String, body: Term): HookRef
    def wrap(body: Term): Term

    final def createRawAndHook(raw: Term, hook: HookRef => Term): HookRef = {
      val rawDef = valDef(raw, "_raw")
      hookDef(hook(rawDef))
    }

    final def hookDef(body: Term): HookRef =
      valDef(nextHookName(), body)

    final def valDef(body: Term, suffix: String): HookRef =
      valDef(nextHookName() + suffix, body)
  }

  // -------------------------------------------------------------------------------------------------------------------

  final case class HookRewriterCtx[Stmt, Term](props: Term, initChildren: Stmt, children: Term)

  trait HookRewriter[Stmt, Term, HookRef] extends HookRewriterApi[Stmt, Term, HookRef] {
    protected var stmts       = Vector.empty[Stmt]
    private var hooks         = List.empty[Term]
    private var _hookCount    = 0
    private var _usesChildren = false

    protected val ctx: HookRewriterCtx[Stmt, Term]
    protected def Apply(t: Term, args: List[Term]): Term
    protected def hookCtx(withChildren: Boolean, args: List[Term]): Term
    protected def hookRefToTerm(ref: HookRef): Term

    final override def usesChildren() =
      _usesChildren

    final override def useChildren(): Unit = {
      _usesChildren = true
      this += ctx.initChildren
    }

    final override def +=(stmt: Stmt): Unit =
      stmts :+= stmt

    final override def hookCount(): Int =
      _hookCount

    final override def nextHookName(): String =
      "hook" + (hookCount() + 1)

    final override def registerHook(h: HookRef): Unit = {
      hooks :+= hookRefToTerm(h)
      _hookCount += 1
    }

    final override def args(): List[Term] =
      if (usesChildren())
        ctx.props :: ctx.children :: hooks
      else
        ctx.props :: hooks

    final override def ctxArg(): Term = {
      val create = hookCtx(usesChildren(), args())
      val ctx = valDef(create, "_ctx")
      hookRefToTerm(ctx)
    }
  }
}

// =====================================================================================================================

trait AbstractHookMacros {
  import AbstractHookMacros._

  // Abstractions

  type Expr[A]
  type HookRef
  type Stmt
  type Term
  type Type[A]
  type TypeTree

  protected def asTerm[A](e: Expr[A]): Term
  protected def Expr[A](t: Term): Expr[A]
  protected def hookRefToTerm(r: HookRef): Term
  protected def Type[A](t: TypeTree): Type[A]

  protected val ApplyLike: ApplyExtractor
  protected abstract class ApplyExtractor {
    def unapply(apply: Term): Option[(Term, List[Term])]
  }

  protected val TypeApplyLike: TypeApplyExtractor
  protected abstract class TypeApplyExtractor {
    def unapply(typeApply: Term): Option[(Term, List[TypeTree])]
  }

  protected val SelectLike: SelectExtractor
  protected abstract class SelectExtractor {
    def unapply(select: Term): Option[(Term, String)]
  }

  protected val FunctionLike: FunctionExtractor
  protected abstract class FunctionExtractor {
    def unapply(function: Term): Option[Int]
  }

  protected def showRaw(t: Term): String
  protected def showCode(t: Term): String

  def rewriter(ctx: HookRewriterCtx[Stmt, Term]): Rewriter

  protected def call(function: Term, args: List[Term]): Term

  protected def useStateFn[S]: (Type[S], Expr[S]) => Expr[React.UseState[Box[S]]]
  protected def useStateFromJsBoxed[S]: (Type[S], Expr[React.UseState[Box[S]]]) => Expr[Hooks.UseState[S]]
  protected def useStateWithReuseFromJsBoxed[S]: (Type[S], Expr[React.UseState[Box[S]]], Expr[Reusability[S]], Expr[ClassTag[S]]) => Expr[Hooks.UseStateWithReuse[S]]
  protected def vdomRawNode: Expr[VdomNode] => Expr[React.Node]

  // Concrete

  protected object AutoTypeImplicits {
    @inline implicit def autoTerm[A](e: Expr[A]): Term = asTerm(e)
    @inline implicit def autoHookRefToTerm(r: HookRef): Term = hookRefToTerm(r)
    @inline implicit def autoHookRefToExpr[A](r: HookRef): Expr[A] = asTerm(hookRefToTerm(r))
    @inline implicit def autoTypeOf[A](t: TypeTree): Type[A] = Type(t)
    @inline implicit def autoExprOf[A](t: Term): Expr[A] = Expr(t)
  }

  type Rewriter = HookRewriterApi[Stmt, Term, HookRef]
  type RewriterCtx = HookRewriterCtx[Stmt, Term]

  def rewriterCtx(props: Term, initChildren: Stmt, children: Term): RewriterCtx =
    HookRewriterCtx(props, initChildren, children)

  implicit val log: MacroLogger =
    MacroLogger()

  case class HookDefn(steps: List[HookStep]) {
    def +(s: HookStep): HookDefn =
      HookDefn(steps ::: s :: Nil)
  }

  case class HookStep(name: String, targs: List[TypeTree], args: List[List[Term]])

  private val withHooks = "withHooks"

  final def parse(tree: Term): Either[() => String, HookDefn] =
    _parse(tree, Nil, Nil, Nil)

  @tailrec
  private def _parse(tree: Term, targs: List[TypeTree], args: List[List[Term]], steps: List[HookStep]): Either[() => String, HookDefn] =
    tree match {

      case ApplyLike(t, a) =>
         _parse(t, targs, a :: args, steps)

      case TypeApplyLike(t, a) =>
        if (targs.isEmpty)
          _parse(t, a, args, steps)
        else
          Left(() => "Multiple type arg clauses found at " + showRaw(tree))

      case SelectLike(t, name) =>
        if (name == withHooks) {
          if (args.nonEmpty)
            Left(() => s"$withHooks called with args when none exepcted: ${args.map(_.map(showCode(_)))}")
          else
            Right(HookDefn(steps))
        } else {
          val step = HookStep(name, targs, args)
          log(s"Found step '$name'", step)
          _parse(t, Nil, Nil, step :: steps)
        }

      case _ =>
        Left(() => "Don't know how to parse " + showRaw(tree))
    }

  def applyRewrite(rewrite: Rewriter => Term): RewriterCtx => Expr[React.Node] = ctx => {
    import AutoTypeImplicits._
    val b    = rewriter(ctx)
    val vdom = rewrite(b)
    b.wrap(vdomRawNode(vdom))
  }

  def rewriteComponent(h: HookDefn): Either[() => String, Rewriter => Term] = {
    val it                = h.steps.iterator
    var renderStep        = null : HookStep
    var hooks             = Vector.empty[Rewriter => HookRef]
    var withPropsChildren = false
    while (it.hasNext) {
      val step = it.next()
      if (it.hasNext) {
        if (hooks.isEmpty && step.name == "withPropsChildren")
          withPropsChildren = true
        else
          rewriteStep(step) match {
            case Right(h) => hooks :+= h
            case Left(e)  => return Left(e)
          }
      } else
        renderStep = step
    }

    rewriteRender(renderStep).map { buildRender => b =>
      if (withPropsChildren)
        b.useChildren()
      for (h <- hooks)
        b registerHook h(b)
      buildRender(b)
    }
  }

  def rewriteStep(step: HookStep): Either[() => String, Rewriter => HookRef] = {
    log("rewriteStep:" + step.name, step)

    def by[A](fn: Term)(use: (Rewriter, Term) => A): Either[() => String, Rewriter => A] =
      fn match {
        case FunctionLike(paramCount) =>
          Right { b =>
            val takesHookCtx = (
              b.hookCount() > 1  // HookCtx only provided from the second hook onwards
              && paramCount == 1 // Function argument takes a single param
            )
            val args =
              if (takesHookCtx)
                b.ctxArg() :: Nil
              else
                b.args()
            use(b, call(fn, args))
          }

        case _ =>
          Left(() => s"Expected a function, found: ${showRaw(fn)}")
      }

    def maybeBy[A](f: Term)(use: (Rewriter, Term) => A): Either[() => String, Rewriter => A] =
      if (step.name endsWith "By")
        by(f)(use)
      else
        Right(use(_, f))

    import AutoTypeImplicits._
    trait X

    step.name match {

      case "useState" | "useStateBy" =>
        val List(tpe) = step.targs : @nowarn
        val List(List(f), _) = step.args : @nowarn
        maybeBy(f) { (b, body) =>
          b.createRawAndHook(
            raw  = useStateFn[X](tpe, body),
            hook = useStateFromJsBoxed[X](tpe, _))
        }

      case "useStateWithReuse" | "useStateWithReuseBy" =>
        val List(tpe) = step.targs : @nowarn
        val List(List(f), List(ct, reuse, _)) = step.args : @nowarn
        maybeBy(f) { (b, body) =>
          b.createRawAndHook(
            raw  = useStateFn[X](tpe, body),
            hook = useStateWithReuseFromJsBoxed[X](tpe, _, reuse, ct))
        }

      case _ =>
        Left(() => s"Inlining of hook method '${step.name}' not yet supported.")
    }
  }

  def rewriteRender(step: HookStep)(implicit log: MacroLogger): Either[() => String, Rewriter => Term] = {
    log("rewriteRender:" + step.name, step)
    step.name match {

      case "render" =>
        val List(List(renderFn), _) = step.args : @nowarn
        Right(b => call(renderFn, b.args()))

      case _ =>
        Left(() => s"Inlining of render method '${step.name}' not yet supported.")
    }
  }

}
