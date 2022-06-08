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
    def valDef(name: HookRef, body: Term): Unit
    def args(): List[Term]
    def ctxArg(): Term
    def hookCount(): Int
    def nextHookName(suffix: String): HookRef
    def registerHook(h: HookRef): Unit
    def useChildren(): Unit
    def usesChildren(): Boolean
    def wrap(body: Term): Term

    final def createRawAndHook(raw: Term, hook: HookRef => Term): HookRef = {
      val rawName = nextHookName("_raw")
      val name    = nextHookName()
      valDef(rawName, raw)
      valDef(name, hook(rawName))
      name
    }

    final def nextHookName(): HookRef =
      nextHookName("")
  }

  trait HookRewriter[Stmt, Term, HookRef] extends HookRewriterApi[Stmt, Term, HookRef] {
    protected var stmts       = Vector.empty[Stmt]
    private var hooks         = List.empty[Term]
    private var _hookCount    = 0
    private var _usesChildren = false

    protected val ctx: HookRewriterCtx[Stmt, Term]
    protected def Apply(t: Term, args: List[Term]): Term
    protected def hookCtx(withChildren: Boolean): Term
    protected def HookRef(name: String): HookRef
    protected def Ident(name: HookRef): Term
    protected def ValDef(name: HookRef, body: Term): Stmt

    final override def usesChildren() =
      _usesChildren

    final override def useChildren(): Unit = {
      _usesChildren = true
      this += ctx.initChildren
    }

    final override def +=(stmt: Stmt): Unit =
      stmts :+= stmt

    final def valDef(name: HookRef, body: Term): Unit =
      this += ValDef(name, body)

    final override def hookCount(): Int =
      _hookCount

    final override def nextHookName(suffix: String = ""): HookRef =
      HookRef("hook" + (hookCount() + 1) + suffix)

    final override def registerHook(h: HookRef): Unit = {
      hooks :+= Ident(h)
      _hookCount += 1
    }

    final override def args(): List[Term] =
      if (usesChildren())
        ctx.props :: ctx.children :: hooks
      else
        ctx.props :: hooks

    final override def ctxArg(): Term = {
      val hookCtxObj = hookCtx(usesChildren())
      val create = Apply(hookCtxObj, args())
      val name = nextHookName("_ctx")
      valDef(name, create)
      Ident(name)
    }
  }

  final case class HookRewriterCtx[Stmt, Term](props: Term, initChildren: Stmt, children: Term)
}

// @nowarn("msg=unchecked since it is eliminated by erasure|cannot be checked at run ?time")
trait AbstractHookMacros {
  import AbstractHookMacros._

  // ===================================================================================================================
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

  protected type Apply <: Term
  protected val Apply: ApplyExtractor
  protected abstract class ApplyExtractor {
    def apply(fun: Term, args: List[Term]): Apply
    def unapply(apply: Term): Option[(Term, List[Term])]
  }

  protected type TypeApply <: Term
  protected val TypeApply: TypeApplyExtractor
  protected abstract class TypeApplyExtractor {
    def apply(fun: Term, args: List[TypeTree]): TypeApply
    def unapply(typeApply: Term): Option[(Term, List[TypeTree])]
  }

  protected type Select <: Term
  protected val Select: SelectExtractor
  protected abstract class SelectExtractor {
    def apply(qualifier: Term, name: String): Select
    def unapply(select: Term): Option[(Term, String)]
  }

  protected type Function <: Term
  protected val Function: FunctionExtractor
  protected abstract class FunctionExtractor {
    def unapply(function: Term): Option[(List[Term], Term)]
  }

  protected def showRaw(t: Term): String
  protected def showCode(t: Term): String

  def rewriter(ctx: HookRewriterCtx[Stmt, Term]): Rewriter

  protected def call(function: Term, args: List[Term]): Term

  protected def useStateFn[S]: (Type[S], Expr[S]) => Expr[React.UseState[Box[S]]]
  protected def useStateFromJsBoxed[S]: (Type[S], Expr[React.UseState[Box[S]]]) => Expr[Hooks.UseState[S]]
  protected def useStateWithReuseFromJsBoxed[S]: (Type[S], Expr[React.UseState[Box[S]]], Expr[Reusability[S]], Expr[ClassTag[S]]) => Expr[Hooks.UseStateWithReuse[S]]
  protected def vdomRawNode: Expr[VdomNode] => Expr[React.Node]

  // ===================================================================================================================
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

  case class HookDefn(steps: List[HookStep])

  case class HookStep(name: String, targs: List[TypeTree], args: List[List[Term]])

  private val withHooks = "withHooks"

  final def apply(tree: Term): Either[() => String, RewriterCtx => Term] =
    for {
      p <- parse(tree)
      r <- rewriteComponent(p)
    } yield applyRewrite(_, r)

  final def parse(tree: Term): Either[() => String, HookDefn] =
    _parse(tree, Nil, Nil, Nil)

  @tailrec
  private def _parse(tree: Term, targs: List[TypeTree], args: List[List[Term]], steps: List[HookStep]): Either[() => String, HookDefn] =
    tree match {

      case Apply(t, a) =>
         _parse(t, targs, a :: args, steps)

      case TypeApply(t, a) =>
        if (targs.isEmpty)
          _parse(t, a, args, steps)
        else
          Left(() => "Multiple type arg clauses found at " + showRaw(tree))

      case Select(t, name) =>
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

  def applyRewrite(ctx: RewriterCtx, rewrite: Rewriter => Term): Term = {
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
        case f@ Function(params, _) =>
          Right { b =>
            val takesHookCtx = (
              b.hookCount() > 1 && // HookCtx only provided from the second hook onwards
              params.sizeIs == 1   // Function argument takes a single param
            )
            val args =
              if (takesHookCtx)
                b.ctxArg() :: Nil
              else
                b.args()
            use(b, call(f, args))
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
