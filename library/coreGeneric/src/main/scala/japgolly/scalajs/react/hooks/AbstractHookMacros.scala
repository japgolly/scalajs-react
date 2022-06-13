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

  trait HookRewriterApi[Stmt, Term <: Stmt, HookRef] {
    def +=(stmt: Stmt): Unit
    def args(): List[Term]
    def createCtxArg(): Term
    def hookCount(): Int
    def nextHookName(): String
    def registerHook(h: HookRef): Unit
    def skipHook(): Unit
    def useChildren(): Unit
    def usesChildren(): Boolean
    def valDef(name: String, body: Term): HookRef
    def wrap(body: Term): Term

    final def argsOrCreateCtxArg(paramCount: Int): List[Term] = {
      val takesHookCtx = (
        hookCount() > 1    // HookCtx only provided from the second hook onwards
        && paramCount == 1 // Function argument takes a single param
      )
      if (takesHookCtx)
        createCtxArg() :: Nil
      else
        args()
    }

    final def createRawAndHook(raw: Term, hook: HookRef => Term): HookRef = {
      val rawDef = valDef(raw, "_raw")
      createHook(hook(rawDef))
    }

    final def createHook(body: Term): HookRef =
      valDef(nextHookName(), body)

    final def createHook(body: Term, discard: Boolean): Option[HookRef] =
      if (discard) {
        this += body
        None
      } else
        Some(createHook(body))

    final def valDef(body: Term, suffix: String): HookRef =
      valDef(nextHookName() + suffix, body)
  }

  // -------------------------------------------------------------------------------------------------------------------

  final case class HookRewriterCtx[Stmt, Term](props: Term, initChildren: Stmt, children: Term)

  trait HookRewriter[Stmt, Term <: Stmt, HookRef] extends HookRewriterApi[Stmt, Term, HookRef] {
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

    final override def skipHook(): Unit =
      _hookCount += 1

    final override def args(): List[Term] =
      if (usesChildren())
        ctx.props :: ctx.children :: hooks
      else
        ctx.props :: hooks

    final override def createCtxArg(): Term = {
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
  type Term <: Stmt
  type Type[A]
  type TypeTree

  protected def asTerm[A](e: Expr[A]): Term
  protected def Expr[A](t: Term): Expr[A]
  protected def hookRefToTerm(r: HookRef): Term
  protected def Type[A](t: TypeTree): Type[A]
  protected def typeOfTerm(t: Term): TypeTree

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
    final type Success = Int
    def unapply(function: Term): Option[Success]
  }

  protected def showRaw(t: Term): String
  protected def showCode(t: Term): String

  def rewriter(ctx: HookRewriterCtx[Stmt, Term]): Rewriter

  protected def call(function: Term, args: List[Term]): Term
  protected def isUnit(t: TypeTree): Boolean
  protected def unitTerm: Expr[Unit]
  protected def unitType: Type[Unit]

  protected def custom[I, O]: (Type[I], Type[O], Expr[CustomHook[I, O]], Expr[I]) => Expr[O]
  protected def customArg[Ctx, Arg]: (Type[Ctx], Type[Arg], Expr[CustomHook.Arg[Ctx, Arg]], Expr[Ctx]) => Expr[Arg]

  protected def useStateFn[S]: (Type[S], Expr[S]) => Expr[React.UseState[Box[S]]]
  protected def useStateFromJsBoxed[S]: (Type[S], Expr[React.UseState[Box[S]]]) => Expr[Hooks.UseState[S]]
  protected def useStateWithReuseFromJsBoxed[S]: (Type[S], Expr[React.UseState[Box[S]]], Expr[Reusability[S]], Expr[ClassTag[S]]) => Expr[Hooks.UseStateWithReuse[S]]
  protected def vdomRawNode: Expr[VdomNode] => Expr[React.Node]

  // -----------------------------------------------------------------------------------------------------------
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

  case class HookStep(name: String, targs: List[TypeTree], args: List[List[Term]]) {
    def sig = (targs, args)
  }

  private val withHooks = "withHooks"

  final def parse(tree: Term): Either[() => String, HookDefn] = {
    log.hold()
    val r = _parse(tree, Nil, Nil, Nil)
    log.release(printPending = r.isLeft)
    r
  }

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
    var hookFns           = Vector.empty[Rewriter => Option[HookRef]]
    var withPropsChildren = false
    while (it.hasNext) {
      val step = it.next()
      if (it.hasNext) {
        if (hookFns.isEmpty && step.name == "withPropsChildren")
          withPropsChildren = true
        else
          rewriteStep(step) match {
            case Right(h) => hookFns :+= h
            case Left(e)  => return Left(e)
          }
      } else
        renderStep = step
    }

    rewriteRender(renderStep).map { buildRender => b =>
      if (withPropsChildren)
        b.useChildren()
      for (hf <- hookFns)
        hf(b) match {
          case Some(h) => b.registerHook(h)
          case None    => b.skipHook()
        }
      buildRender(b)
    }
  }

  def rewriteStep(step: HookStep): Either[() => String, Rewriter => Option[HookRef]] = {
    log("rewriteStep:" + step.name, step)

    def by[A](fn: Term)(use: (Rewriter, Term => Term) => A): Either[() => String, Rewriter => A] =
      fn match {
        case FunctionLike(paramCount) =>
          Right { b =>
            val args = b.argsOrCreateCtxArg(paramCount)
            use(b, call(_, args))
          }

        case _ =>
          Left(() => s"Expected a function, found: ${showRaw(fn)}")
      }

    def maybeBy[A](f: Term)(use: (Rewriter, Term => Term) => A): Either[() => String, Rewriter => A] =
      if (step.name endsWith "By")
        by(f)(use)
      else
        Right(use(_, identity))

    implicit def autoSomeHookRefs(r: HookRef): Option[HookRef] =
      Some(r)

    import AutoTypeImplicits._
    trait X

    step.name match {

      // val (List(), List(List())) = step.sig : @nowarn

      case "custom" =>
        val (List(i, o), List(List(h), List(_, a, _))) = step.sig : @nowarn
        Right { b =>
          val initHook: Term =
            if (isUnit(i))
              custom[Unit, X](unitType, o, h, unitTerm)
            else {
              val ctxArgs      = b.argsOrCreateCtxArg(1)
              val List(ctxArg) = ctxArgs : @nowarn
              val ctxArgType   = typeOfTerm(ctxArg)
              val ctx          = b.valDef(customArg[X, X](ctxArgType, o, a, ctxArg), "_ctx")
              custom[X, X](i, o, h, ctx)
            }
          b.createHook(initHook, discard = isUnit(o))
        }

      case "customBy" =>
        val (List(o), List(List(h), List(_, _))) = step.sig : @nowarn
        by(h) { (b, withCtx) =>
          b.createRawAndHook(
            raw  = withCtx(h),
            hook = custom[Unit, X](unitType, o, _, unitTerm))
        }

      // case "useMemo" | "useMemoBy" =>
      //   val (List(d, a), List(List(deps), List(create), List(reuse, step))) = step.sig : @nowarn
      //   maybeBy(deps) { (b, withCtx) =>
      //   }

      case "useState" | "useStateBy" =>
        val (List(tpe), List(List(initialState), List(_))) = step.sig : @nowarn
        maybeBy(initialState) { (b, withCtx) =>
          b.createRawAndHook(
            raw  = useStateFn[X](tpe, withCtx(initialState)),
            hook = useStateFromJsBoxed[X](tpe, _))
        }

      case "useStateWithReuse" | "useStateWithReuseBy" =>
        val (List(tpe), List(List(initialState), List(ct, reuse, _)))  = step.sig : @nowarn
        maybeBy(initialState) { (b, withCtx) =>
          b.createRawAndHook(
            raw  = useStateFn[X](tpe, withCtx(initialState)),
            hook = useStateWithReuseFromJsBoxed[X](tpe, _, reuse, ct))
        }

      case _ =>
        Left(() => s"Inlining of hook method '${step.name}' not yet supported.")
    }
  }

  def rewriteRender(step: HookStep)(implicit log: MacroLogger): Either[() => String, Rewriter => Term] = {
    log("rewriteRender:" + step.name, step)
    step.name match {

      case "render" | "renderDebug" =>
        val List(List(renderFn), _) = step.args : @nowarn
        Right(b => call(renderFn, b.args()))

      case _ =>
        Left(() => s"Inlining of render method '${step.name}' not yet supported.")
    }
  }

}
