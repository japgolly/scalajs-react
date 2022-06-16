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

  sealed trait HookRewriter[Stmt, Term <: Stmt, Ref] {
              val bridge      : HookRewriter.Bridge[Stmt, Term, Ref]
    protected val hookNo      : Int
    protected val initialCtx  : HookRewriter.InitialCtx[Stmt, Term]
    protected def initialStmts: Vector[Stmt]
    protected val prevHooks   : List[Term]
    protected val usesChildren: Boolean

    private var _stmts: Vector[Stmt] =
      initialStmts

    // Don't make this an eager val. It depends on `val hookNo`
    protected def hookName =
      "hook" + hookNo

    final def +=(stmt: Stmt): Unit =
      _stmts :+= stmt

    final def args: List[Term] =
      if (usesChildren)
        initialCtx.props :: initialCtx.children :: prevHooks
      else
        initialCtx.props :: prevHooks

    final def argsOrCtxArg(paramCount: Int): List[Term] = {
      val takesHookCtx = (
        prevHooks.nonEmpty // HookCtx only provided when previous hook results exist
        && paramCount == 1 // Function argument takes a single param
      )
      if (takesHookCtx)
        ctxArg :: Nil
      else
        args
    }

    final lazy val ctxArg: Term = {
      val create = bridge.hookCtx(usesChildren, args)
      val ctx = valDef(create, "_ctx")
      bridge.refToTerm(ctx)
    }

    final def createHook(body: Term): Ref =
      valDef(body, "")

    final def createHook(body: Term, discard: Boolean): Option[Ref] =
      if (discard) {
        this += body
        None
      } else
        Some(createHook(body))

    final def createRawAndHook(raw: Term, hook: Ref => Term): Ref = {
      val rawDef = valDef(raw, "_raw")
      createHook(hook(rawDef))
    }

    final def createRawAndHook(raw: Term, hook: Ref => Term, discard: Boolean): Option[Ref] = {
      val rawDef = valDef(raw, "_raw")
      createHook(hook(rawDef), discard = discard)
    }

    final def stmts() =
      _stmts

    final def valDef(body: Term, suffix: String): Ref = {
      val x = bridge.valDef(hookName + suffix, body)
      this += x._1
      x._2
    }
  }

  object HookRewriter {

    final case class InitialCtx[Stmt, Term](props: Term, initChildren: Stmt, children: Term)

    final case class Bridge[Stmt, Term, Ref](
      apply    : (Term, List[Term]) => Term,
      hookCtx  : (Boolean, List[Term]) => Term,
      refToTerm: Ref => Term,
      valDef   : (String, Term) => (Stmt, Ref),
    )

    def init[Stmt, Term <: Stmt, Ref](ctx        : InitialCtx[Stmt, Term],
                                      bridg      : Bridge[Stmt, Term, Ref],
                                      useChildren: Boolean): HookRewriter[Stmt, Term, Ref] =
      new HookRewriter[Stmt, Term, Ref] {
        override           val bridge       = bridg
        override protected val hookNo       = 1
        override protected val initialCtx   = ctx
        override protected def initialStmts = if (useChildren) Vector(ctx.initChildren) else Vector.empty
        override protected val prevHooks    = Nil
        override protected val usesChildren = useChildren
      }

    def next[Stmt, Term <: Stmt, Ref](prev: HookRewriter[Stmt, Term, Ref])(newHook: Option[Ref]): HookRewriter[Stmt, Term, Ref] =
      new HookRewriter[Stmt, Term, Ref] {
        override           val bridge       = prev.bridge
        override protected val hookNo       = prev.hookNo + 1
        override protected val initialCtx   = prev.initialCtx
        override protected def initialStmts = prev.stmts()
        override protected val prevHooks    = newHook.fold(prev.prevHooks)(prev.prevHooks :+ prev.bridge.refToTerm(_))
        override protected val usesChildren = prev.usesChildren
      }
  }
}

// =====================================================================================================================

trait AbstractHookMacros {
  import AbstractHookMacros._

  type Expr[A]
  type Ref
  type Stmt
  type Term <: Stmt
  type Type[A]
  type TypeTree

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

  protected def asTerm        [A](e: Expr[A])                      : Term
  protected def call             (function: Term, args: List[Term]): Term
  protected def Expr          [A](t: Term)                         : Expr[A]
  protected def isUnit           (t: TypeTree)                     : Boolean
  protected def refToTerm        (r: Ref)                          : Term
  protected def showCode         (t: Term)                         : String
  protected def showRaw          (t: Term)                         : String
  protected def Type          [A](t: TypeTree)                     : Type[A]
  protected def typeOfTerm       (t: Term)                         : TypeTree
  protected def unitTerm                                           : Expr[Unit]
  protected def unitType                                           : Type[Unit]
  protected def wrap                                               : (Vector[Stmt], Term) => Term
  protected val rewriterBridge                                     : RewriterBridge

  protected def custom[I, O]: (Type[I], Type[O], Expr[CustomHook[I, O]], Expr[I]) => Expr[O]
  protected def customArg[C, A]: (Type[C], Type[A], Expr[CustomHook.Arg[C, A]], Expr[C]) => Expr[A]
  protected def useStateFn[S]: (Type[S], Expr[S]) => Expr[React.UseState[Box[S]]]
  protected def useStateFromJsBoxed[S]: (Type[S], Expr[React.UseState[Box[S]]]) => Expr[Hooks.UseState[S]]
  protected def useStateWithReuseFromJsBoxed[S]: (Type[S], Expr[React.UseState[Box[S]]], Expr[Reusability[S]], Expr[ClassTag[S]]) => Expr[Hooks.UseStateWithReuse[S]]
  protected def vdomRawNode: Expr[VdomNode] => Expr[React.Node]

  // -------------------------------------------------------------------------------------------------------------------
  // Concrete

  case class HookDefn(steps: Vector[HookStep]) {
    def +(s: HookStep): HookDefn =
      HookDefn(steps :+ s)
  }

  case class HookStep(name: String, targs: List[TypeTree], args: List[List[Term]]) {
    def sig = (targs, args)
  }

  final type Rewriter       = HookRewriter[Stmt, Term, Ref]
  final type RewriterBridge = HookRewriter.Bridge[Stmt, Term, Ref]
  final type RewriterCtx    = HookRewriter.InitialCtx[Stmt, Term]

  final implicit val log: MacroLogger =
    MacroLogger()

  final def rewriterCtx(props: Term, initChildren: Stmt, children: Term): RewriterCtx =
    HookRewriter.InitialCtx(props, initChildren, children)

  protected object AutoTypeImplicits {
    @inline implicit def autoTerm[A](e: Expr[A]): Term = asTerm(e)
    @inline implicit def autoRefToTerm(r: Ref): Term = refToTerm(r)
    @inline implicit def autoRefToExpr[A](r: Ref): Expr[A] = asTerm(refToTerm(r))
    @inline implicit def autoTypeOf[A](t: TypeTree): Type[A] = Type(t)
    @inline implicit def autoExprOf[A](t: Term): Expr[A] = Expr(t)
  }

  private def traverseVector[A, E, B](as: Vector[A])(f: A => Either[E, B]): Either[E, Vector[B]] = {
    var results = Vector.empty[B]
    var i = 0
    while (i < as.length) {
      f(as(i)) match {
        case Right(b) => results :+= b
        case Left(e)  => return Left(e)
      }
      i += 1
    }
    Right(results)
  }

  // -------------------------------------------------------------------------------------------------------------------
  final def parse(tree: Term): Either[() => String, HookDefn] = {
    log.hold()
    val r = _parse(tree, Nil, Nil, Nil)
    log.release(printPending = r.isLeft)
    r
  }

  private val withHooks = "withHooks"

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
            Right(HookDefn(steps.toVector))
        } else {
          val step = HookStep(name, targs, args)
          log(s"Found step '$name'", step)
          _parse(t, Nil, Nil, step :: steps)
        }

      case _ =>
        Left(() => "Don't know how to parse " + showRaw(tree))
    }

  // -------------------------------------------------------------------------------------------------------------------
  def rewriteComponent(h: HookDefn): Either[() => String, RewriterCtx => Expr[React.Node]] = {
    if (h.steps.isEmpty)
      return Left(() => "Failed to find any hook steps to parse.")

    val withPropsChildren = h.steps.head.name == "withPropsChildren"
    val hookSteps         = if (withPropsChildren) h.steps.init.tail else h.steps.init
    val renderStep        = h.steps.last

    for {
      hookFns  <- traverseVector(hookSteps)(rewriteStep)
      renderFn <- rewriteRender(renderStep)
    } yield rctx => {
      val r0   = HookRewriter.init(rctx, rewriterBridge, withPropsChildren)
      val rH   = hookFns.foldLeft(r0)((r, hf) => HookRewriter.next(r)(hf(r)))
      val vdom = renderFn(rH)

      import AutoTypeImplicits._
      wrap(rH.stmts(), vdomRawNode(vdom))
    }
  }

  // -------------------------------------------------------------------------------------------------------------------
  def rewriteStep(step: HookStep): Either[() => String, Rewriter => Option[Ref]] = {
    log("rewriteStep:" + step.name, step)

    def by[A](fn: Term)(use: (Rewriter, Term => Term) => A): Either[() => String, Rewriter => A] =
      fn match {
        case FunctionLike(paramCount) =>
          Right { b =>
            val args = b.argsOrCtxArg(paramCount)
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

    implicit def autoSomeRefs(r: Ref): Option[Ref] =
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
              val ctxArgs      = b.argsOrCtxArg(1)
              val List(ctxArg) = ctxArgs : @nowarn
              val ctxArgType   = typeOfTerm(ctxArg)
              val ctx          = b.valDef(customArg[X, X](ctxArgType, o, a, ctxArg), "_arg")
              custom[X, X](i, o, h, ctx)
            }
          b.createHook(initHook, discard = isUnit(o))
        }

      case "customBy" =>
        val (List(o), List(List(h), List(_, _))) = step.sig : @nowarn
        by(h) { (b, withCtx) =>
          b.createRawAndHook(
            raw     = withCtx(h),
            hook    = custom[Unit, X](unitType, o, _, unitTerm),
            discard = isUnit(o))
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

  // -------------------------------------------------------------------------------------------------------------------
  def rewriteRender(step: HookStep): Either[() => String, Rewriter => Term] = {
    log("rewriteRender:" + step.name, step)
    step.name match {

      case "renderRR" | "renderRRDebug" =>
        val List(List(renderFn), _) = step.args : @nowarn
        Right(b => call(renderFn, b.args))

      case _ =>
        Left(() => s"Inlining of render method '${step.name}' not yet supported.")
    }
  }

}
