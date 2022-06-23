package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.React.Context
import japgolly.scalajs.react.facade.React
import japgolly.scalajs.react.facade.React.HookDeps
import japgolly.scalajs.react.hooks.CustomHook.ReusableDepState
import japgolly.scalajs.react.hooks.Hooks
import japgolly.scalajs.react.internal.{Box, MacroLogger}
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Reusability, Reusable}
import scala.annotation.{nowarn, tailrec}
import scala.reflect.ClassTag
import scala.scalajs.js

/* Coverage
 * ========
 *
 *   - P
 *     - behaviour tested in HooksRRTest.testProps()
 *     - react-refresh integration tested in JustPropsViaHookApi
 *
 *   - CtxObj(P)
 *     - behaviour tested in HooksRRTest.testUseState()
 *     - react-refresh integration tested in UseStateWithReuse
 *
 *   - CtxFn(P)
 *     - behaviour tested in HooksRRTest.testUseCallback()
 *     - react-refresh integration tested in UseState
 *
 *   - (P, PC)
 *     - behaviour tested in HooksRRTest.testPropsChildren()
 *     - react-refresh integration tested in JustPropsChildrenViaHookApi
 *
 *   - CtxObj(P, PC)
 *     - behaviour tested in HooksRRTest.testPropsChildrenCtxObj()
 *     - react-refresh integration tested in HooksWithChildrenCtxObj
 *
 *   - CtxFn(P, PC)
 *     - behaviour tested in HooksRRTest.testPropsChildrenCtxFn()
 *     - react-refresh integration tested in HooksWithChildrenCtxFn
 */

object AbstractHookMacros {

  final case class HookDefn[Term, TypeTree](steps: Vector[HookStep[Term, TypeTree]]) {
    def +(s: HookStep[Term, TypeTree]): HookDefn[Term, TypeTree] =
      HookDefn(steps :+ s)
  }

  final case class HookStep[Term, TypeTree](name: String, targs: List[TypeTree], args: List[List[Term]]) {
    def sig = (targs, args)
  }

  // ===================================================================================================================
  // Hook Rewriter

  // Avoid shadowing in Scala 2.
  final val hookValPrefix = "__japgolly__"

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
      if (hookNo < 0)
        hookValPrefix + "render"
      else
        hookValPrefix + "hook" + hookNo

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

    final def createHook(body: Term): Ref = {
      assert(hookNo > 0)
      valDef(body, "")
    }

    final def createHook(body: Term, discard: Boolean): Option[Ref] = {
      assert(hookNo > 0)
      if (discard) {
        this += body
        None
      } else
        Some(createHook(body))
    }

    final def createRaw(body: Term, isLazy: Boolean = false): Ref =
      valDef(body, "_raw", isLazy = isLazy)

    @inline final def isScala2 = bridge.isScala2
    @inline final def isScala3 = bridge.isScala3

    final def stmts() =
      _stmts

    final def valDef(body: Term, suffix: String, isLazy: Boolean = false): Ref = {
      val x = bridge.valDef(hookName + suffix, body, isLazy)
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
      scalaVer : Int,
      valDef   : (String, Term, Boolean) => (Stmt, Ref), // Bool = isLazy
    ) {
      @inline def isScala2 = scalaVer == 2
      @inline def isScala3 = scalaVer == 3
    }

    def start[Stmt, Term <: Stmt, Ref](ctx        : InitialCtx[Stmt, Term],
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

    def end[Stmt, Term <: Stmt, Ref](prev: HookRewriter[Stmt, Term, Ref]): HookRewriter[Stmt, Term, Ref] =
      new HookRewriter[Stmt, Term, Ref] {
        override           val bridge       = prev.bridge
        override protected val hookNo       = -1
        override protected val initialCtx   = prev.initialCtx
        override protected def initialStmts = prev.stmts()
        override protected val prevHooks    = prev.prevHooks
        override protected val usesChildren = prev.usesChildren
      }
  }
}

// =====================================================================================================================

trait AbstractHookMacros {
  import AbstractHookMacros.HookRewriter

  type Expr[+A]
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

  protected def asTerm     [A]: Expr[A] => Term
  protected def call          : (Term, List[Term], Boolean) => Term // Bool = betaReduce (i.e. inline args)
  protected def Expr       [A]: Term => Expr[A]
  protected def isUnit        : TypeTree => Boolean
  protected def refToTerm     : Ref => Term
  protected def showCode      : Term => String
  protected def showRaw       : Term => String
  protected def Type       [A]: TypeTree => Type[A]
  protected def typeOfTerm    : Term => TypeTree
  protected def uninline      : Term => Term
  protected def unitTerm      : Expr[Unit]
  protected def unitType      : Type[Unit]
  protected def wrap          : (Vector[Stmt], Term) => Term
  protected val rewriterBridge: RewriterBridge

  // -------------------------------------------------------------------------------------------------------------------
  // Concrete


  final type HookDefn       = AbstractHookMacros.HookDefn[Term, TypeTree]
  final type HookStep       = AbstractHookMacros.HookStep[Term, TypeTree]
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
    uninline(tree) match {

      case ApplyLike(t, a) =>
         _parse(t, targs, a :: args, steps)

      case ta@ TypeApplyLike(t, a) =>
        if (targs.isEmpty)
          _parse(t, a, args, steps)
        else
          Left(() => "Multiple type arg clauses found at " + showRaw(ta))

      case SelectLike(t, name) =>
        if (name == withHooks) {
          if (args.nonEmpty)
            Left(() => s"$withHooks called with args when none exepcted: ${args.map(_.map(showCode(_)))}")
          else
            Right(AbstractHookMacros.HookDefn(steps.toVector))
        } else {
          val step = AbstractHookMacros.HookStep(name, targs, args)
          log(s"Found step '$name'", step)
          _parse(t, Nil, Nil, step :: steps)
        }

      case t =>
        Left(() => "Don't know how to parse " + showRaw(t))
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
      val r0   = HookRewriter.start(rctx, rewriterBridge, withPropsChildren)
      val rH   = hookFns.foldLeft(r0)((r, hf) => HookRewriter.next(r)(hf(r)))
      val rR   = HookRewriter.end(rH)
      val vdom = renderFn(rR)

      import AutoTypeImplicits._
      wrap(rR.stmts(), vdomRawNode(vdom))
    }
  }

  // -------------------------------------------------------------------------------------------------------------------
  def rewriteStep(step: HookStep): Either[() => String, Rewriter => Option[Ref]] = {
    log("rewriteStep:" + step.name, step)

    def by[A](fn: Term, betaReduce: Rewriter => Boolean = null)(use: (Rewriter, Term => Term) => A): Either[() => String, Rewriter => A] =
      fn match {
        case FunctionLike(paramCount) =>
          Right { b =>
            val args = b.argsOrCtxArg(paramCount)
            val br = if (betaReduce eq null) true else betaReduce(b)
            use(b, call(_, args, br))
          }

        case _ =>
          Left(() => s"Expected a function, found: ${showRaw(fn)}")
      }

    def maybeBy[A](f: Term, betaReduce: Rewriter => Boolean = null)(use: (Rewriter, Term => Term) => A): Either[() => String, Rewriter => A] =
      if (step.name endsWith "By")
        by(f, betaReduce = betaReduce)(use)
      else
        Right(use(_, identity))

    import AutoTypeImplicits._
    trait X
    trait Y

    def createUseCallbackHook[C](b: Rewriter, ucArg: Term, callback: Expr[C], hookDeps: Expr[HookDeps], tpeC: Type[C]): Ref = {
      val a          = refToTerm(b.valDef(ucArg, "_arg")) // stablise for dependent types
      type J         = js.Function
      val tpeJ       = useCallbackArgTypeJs[C, J](a)
      val jsCallback = b.valDef(useCallbackArgToJs[C, J](a, callback, tpeC, tpeJ), "_jscb")
      val raw        = b.createRaw(useCallback[J](jsCallback, hookDeps, tpeJ))
      val hook       = useCallbackArgFromJs[C, J](a, raw, tpeC, tpeJ)
      b.createHook(hook)
    }

    def reusableDeps[D](b: Rewriter, depsExpr: Expr[D], reuse: Expr[Reusability[D]], tpeD: Type[D]): (Expr[D], Expr[Int]) = {
      type DS      = ReusableDepState[D]
      val tpeODS   = optionType(reusableDepStateType(tpeD))
      val stateRaw = b.valDef(useStateValue[Option[DS]](tpeODS, none(tpeODS)), "_state_raw")
      val state    = b.valDef(useStateFromJsBoxed[Option[DS]](tpeODS, stateRaw), "_state")
      val newDeps  = b.valDef(depsExpr, "_deps")
      val rds      = b.valDef(reusableDepsLogic[D](newDeps, state, reuse, tpeD), "_deps_state")
      val deps     = reusableDepStateValue(rds, tpeD)
      val rev      = reusableDepStateRev(rds)
      (deps, rev)
    }

    def reusableByDeps[D, A](b: Rewriter, depsExpr: Expr[D], reuse: Expr[Reusability[D]], tpeD: Type[D], tpeA: Type[A])
                            (create: (Expr[D], Expr[Int]) => Expr[A]): Expr[Reusable[A]] = {
      val (deps, rev) = reusableDeps[D](b, depsExpr, reuse, tpeD)
      val value       = b.valDef(create(deps, rev), "_val")
      reusableValueByInt[A](rev, value, tpeA)
    }

    implicit def autoSomeRefs(r: Ref): Option[Ref] =
      Some(r)

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
          val raw = b.createRaw(withCtx(h))
          b.createHook(custom[Unit, X](unitType, o, raw, unitTerm), discard = isUnit(o))
        }

      case "localVal" | "localValBy" =>
        val (List(_), List(List(valueFn), List(_))) = step.sig : @nowarn
        maybeBy(valueFn) { (b, withCtx) =>
          b.createHook(withCtx(valueFn))
        }

      case "localLazyVal" | "localLazyValBy" =>
        val (List(tpe), List(List(valueFn), List(_))) = step.sig : @nowarn
        // Here we avoid beta-reduction in Scala 3 because it causes a crash if props are referenced
        maybeBy(valueFn, betaReduce = _.isScala2) { (b, withCtx) =>
          val raw = b.createRaw(withCtx(valueFn), isLazy = true)
          b.createHook(scalaFn0[X](tpe, raw))
        }

      case "localVar" | "localVarBy" =>
        val (List(tpe), List(List(valueFn), List(_))) = step.sig : @nowarn
        maybeBy(valueFn) { (b, withCtx) =>
          b.createHook(hooksVar[X](tpe, withCtx(valueFn)))
        }

      case "unchecked" | "uncheckedBy" =>
        val (List(tpe), List(List(valueFn), List(_, _))) = step.sig : @nowarn
        maybeBy(valueFn) { (b, withCtx) =>
          b.createHook(withCtx(valueFn), discard = isUnit(tpe))
        }

      case "useCallback" | "useCallbackBy" =>
        val (List(tpeC), List(List(callbackFn), List(ucArg, _))) = step.sig : @nowarn
        maybeBy(callbackFn) { (b, withCtx) =>
          createUseCallbackHook[X](b, ucArg, withCtx(callbackFn), hookDepsEmptyArray, tpeC)
        }

      case "useCallbackWithDeps" | "useCallbackWithDepsBy" =>
        val (List(tpeD, tpeC), List(List(depsFn), List(callbackFnFn), List(ucArg, reuse, _))) = step.sig : @nowarn
        maybeBy(callbackFnFn) { (b, withCtx) =>
          val (deps, rev) = reusableDeps[X](b, withCtx(depsFn), reuse, tpeD)
          val callbackFn  = withCtx(callbackFnFn)
          val callback    = call(callbackFn, deps :: Nil, false)
          val hookDeps    = hookDepsIntArray1(rev)
          createUseCallbackHook[X](b, ucArg, callback, hookDeps, tpeC)
        }

      case "useContext" | "useContextBy" =>
        val (List(tpe), List(List(ctxFn), List(_))) = step.sig : @nowarn
        maybeBy(ctxFn) { (b, withCtx) =>
          b.createHook(useContext[X](withCtx(ctxFn), tpe))
        }

      case "useDebugValue" | "useDebugValueBy" =>
        val (Nil, List(List(descFn), List(_))) = step.sig : @nowarn
        maybeBy(descFn) { (b, withCtx) =>
          b.createHook(useDebugValue(withCtx(descFn)), discard = true)
        }

      case "useEffect"
         | "useEffectBy"
         | "useEffectOnMount"
         | "useEffectOnMountBy"
         | "useLayoutEffect"
         | "useLayoutEffectBy"
         | "useLayoutEffectOnMount"
         | "useLayoutEffectOnMountBy" =>
        val (List(tpe), List(List(effectFn), List(arg, _))) = step.sig : @nowarn
        maybeBy(effectFn) { (b, withCtx) =>
          val hook        = if (step.name.contains("Layout")) useLayoutEffect else useEffect
          val hookDeps    = if (step.name.contains("Mount")) hookDepsEmptyArray else hookDepsUndefined
          val effect      = withCtx(effectFn)
          val effectJs    = useEffectArgToJs[X](arg, effect, tpe)
          b.createHook(hook(effectJs, hookDeps), discard = true)
          None
        }

      case "useEffectWithDeps"
         | "useEffectWithDepsBy"
         | "useLayoutEffectWithDeps"
         | "useLayoutEffectWithDepsBy" =>
        val (List(tpeD, tpeA), List(List(depsFn), List(effectFnFn), List(arg, reuse, _))) = step.sig : @nowarn
        maybeBy(effectFnFn) { (b, withCtx) =>
          val hook        = if (step.name.contains("Layout")) useLayoutEffect else useEffect
          val (deps, rev) = reusableDeps[X](b, withCtx(depsFn), reuse, tpeD)
          val hookDeps    = hookDepsIntArray1(rev)
          val effectFn    = withCtx(effectFnFn)
          val effect      = call(effectFn, deps :: Nil, false)
          val effectJs    = useEffectArgToJs[X](arg, effect, tpeA)
          b.createHook(hook(effectJs, hookDeps), discard = true)
          None
        }

      case "useMemo" | "useMemoBy" =>
        val (List(tpeD, tpeA), List(List(depsFn), List(createFnFn), List(reuse, _))) = step.sig : @nowarn
        maybeBy(depsFn) { (b, withCtx) =>
          b.createHook {
            reusableByDeps[X, Y](b, withCtx(depsFn), reuse, tpeD, tpeA) { (deps, rev) =>
              val createFn = withCtx(createFnFn)
              val newValue = call(createFn, asTerm(deps) :: Nil, false)
              val hookDeps    = hookDepsIntArray1(rev)
              useMemo[Y](newValue, hookDeps, tpeA)
            }
          }
        }

      case "useReducer" | "useReducerBy" =>
        val (List(tpeS, tpeA), List(List(reducerFn, initialStateFn), List(_))) = step.sig : @nowarn
        maybeBy(reducerFn) { (b, withCtx) =>
          val reducer      = withCtx(reducerFn)
          val initialState = withCtx(initialStateFn)
          val raw          = b.createRaw(useReducer[X, Y](reducer, initialState, tpeS, tpeA))
          b.createHook(useReducerFromJs[X, Y](raw, tpeS, tpeA))
        }

      case "useState" | "useStateBy" =>
        val (List(tpe), List(List(initialStateFn), List(_))) = step.sig : @nowarn
        maybeBy(initialStateFn) { (b, withCtx) =>
          val raw = b.createRaw(useStateFn[X](tpe, withCtx(initialStateFn)))
          b.createHook(useStateFromJsBoxed[X](tpe, raw))
        }

      case "useStateWithReuse" | "useStateWithReuseBy" =>
        val (List(tpe), List(List(initialStateFn), List(ct, reuse, _)))  = step.sig : @nowarn
        maybeBy(initialStateFn) { (b, withCtx) =>
          val raw = b.createRaw(useStateFn[X](tpe, withCtx(initialStateFn)))
          b.createHook(useStateWithReuseFromJsBoxed[X](tpe, raw, reuse, ct))
        }

      case _ =>
        Left(() => s"Inlining of hook method '${step.name}' not yet supported.")
    }
  }

  protected def custom[I, O]: (Type[I], Type[O], Expr[CustomHook[I, O]], Expr[I]) => Expr[O]

  protected def customArg[C, A]: (Type[C], Type[A], Expr[CustomHook.Arg[C, A]], Expr[C]) => Expr[A]

  protected def hookDepsEmptyArray: Expr[HookDeps]

  protected def hookDepsIntArray1: Expr[Int] => Expr[HookDeps]

  protected def hookDepsUndefined: Expr[HookDeps] =
    unitTerm.asInstanceOf[Expr[HookDeps]]

  protected def hooksVar[A]: (Type[A], Expr[A]) => Expr[Hooks.Var[A]]

  protected def none[A]: Type[Option[A]] => Expr[Option[A]]

  protected def optionType[A]: Type[A] => Type[Option[A]]

  protected def reusableDepsLogic[D]: (Expr[D], Expr[Hooks.UseState[Option[ReusableDepState[D]]]], Expr[Reusability[D]], Type[D]) => Expr[ReusableDepState[D]]

  protected def reusableDepStateRev: Expr[ReusableDepState[Any]] => Expr[Int]

  protected def reusableDepStateType[D]: Type[D] => Type[ReusableDepState[D]]

  protected def reusableDepStateValue[D]: (Expr[ReusableDepState[D]], Type[D]) => Expr[D]

  protected def reusableValueByInt[A]: (Expr[Int], Expr[A], Type[A]) => Expr[Reusable[A]]

  protected def scalaFn0[A]: (Type[A], Expr[A]) => Expr[() => A]

  protected def useCallback[F <: js.Function]: (Expr[F], Expr[HookDeps], Type[F]) => Expr[F]

  protected def useCallbackArgFromJs[A, J <: js.Function]: (Expr[Hooks.UseCallbackArg.To[A, J]], Expr[J], Type[A], Type[J]) => Expr[Reusable[A]]

  protected def useCallbackArgToJs[A, J <: js.Function]: (Expr[Hooks.UseCallbackArg.To[A, J]], Expr[A], Type[A], Type[J]) => Expr[J]

  protected def useCallbackArgTypeJs[A, J <: js.Function]: (Expr[Hooks.UseCallbackArg.To[A, J]]) => Type[J]

  protected def useContext[A]: (Expr[Context[A]], Type[A]) => Expr[A]

  protected def useDebugValue: Expr[Any] => Expr[Unit]

  protected def useEffect: (Expr[React.UseEffectArg], Expr[HookDeps]) => Expr[Unit]

  protected def useEffectArgToJs[A]: (Expr[Hooks.UseEffectArg[A]], Expr[A], Type[A]) => Expr[React.UseEffectArg]

  protected def useLayoutEffect: (Expr[React.UseEffectArg], Expr[HookDeps]) => Expr[Unit]

  protected def useMemo[A]: (Expr[A], Expr[HookDeps], Type[A]) => Expr[A]

  protected def useReducer[S, A]: (Expr[(S, A) => S], Expr[S], Type[S], Type[A]) => Expr[React.UseReducer[S, A]]

  protected def useReducerFromJs[S, A]: (Expr[React.UseReducer[S, A]], Type[S], Type[A]) => Expr[Hooks.UseReducer[S, A]]

  protected def useStateFn[S]: (Type[S], Expr[S]) => Expr[React.UseState[Box[S]]]

  protected def useStateValue[S]: (Type[S], Expr[S]) => Expr[React.UseState[Box[S]]]

  protected def useStateFromJsBoxed[S]: (Type[S], Expr[React.UseState[Box[S]]]) => Expr[Hooks.UseState[S]]

  protected def useStateWithReuseFromJsBoxed[S]: (Type[S], Expr[React.UseState[Box[S]]], Expr[Reusability[S]], Expr[ClassTag[S]]) => Expr[Hooks.UseStateWithReuse[S]]

  protected def vdomRawNode: Expr[VdomNode] => Expr[React.Node]

  // -------------------------------------------------------------------------------------------------------------------
  def rewriteRender(step: HookStep): Either[() => String, Rewriter => Term] = {
    log("rewriteRender:" + step.name, step)
    step.name match {

      case "renderRR" | "renderRRDebug" =>
        val List(List(fn), _) = step.args : @nowarn
        uninline(fn) match {
          case FunctionLike(paramCount) =>
            Right { b =>
              val args = b.argsOrCtxArg(paramCount)
              call(fn, args, true)
            }

          case _ =>
            Left(() => s"Expected a function, found: ${showRaw(fn)}")
        }

      case _ =>
        Left(() => s"Inlining of render method '${step.name}' not yet supported.")
    }
  }

}
