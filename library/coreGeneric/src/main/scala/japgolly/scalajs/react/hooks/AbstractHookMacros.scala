package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.React.Context
import japgolly.scalajs.react.component.{Js => JsComponent, Scala => ScalaComponent}
import japgolly.scalajs.react.facade.React
import japgolly.scalajs.react.facade.React.HookDeps
import japgolly.scalajs.react.hooks.CustomHook.ReusableDepState
import japgolly.scalajs.react.hooks.Hooks
import japgolly.scalajs.react.internal.{Box, MacroLogger}
import japgolly.scalajs.react.util.DefaultEffects
import japgolly.scalajs.react.vdom.{TopNode, VdomNode}
import japgolly.scalajs.react.{CtorType, Ref, Reusability, Reusable}
import scala.annotation.{nowarn, tailrec}
import scala.reflect.ClassTag
import scala.scalajs.js
import scala.scalajs.js.|

/* Coverage: render
 * ================
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
 *
 *
 * Coverage: renderReusable
 * ========================
 *
 *   - P
 *     - behaviour tested in HooksRRTest.xxxxx()
 *     - react-refresh integration tested in RenderReusable.P
 *
 *   - CtxObj(P)
 *     - behaviour tested in HooksRRTest.xxxxx()
 *     - react-refresh integration tested in RenderReusable.CtxObj_P
 *
 *   - CtxFn(P)
 *     - behaviour tested in HooksRRTest.xxxxx()
 *     - react-refresh integration tested in RenderReusable.CtxFn_P
 *
 *   - (P, PC)
 *     - behaviour tested in HooksRRTest.xxxxx()
 *     - react-refresh integration tested in RenderReusable.P_PC
 *
 *   - CtxObj(P, PC)
 *     - behaviour tested in HooksRRTest.xxxxx()
 *     - react-refresh integration tested in RenderReusable.CtxObj_P_PC
 *
 *   - CtxFn(P, PC)
 *     - behaviour tested in HooksRRTest.xxxxx()
 *     - react-refresh integration tested in RenderReusable.CtxFn_P_PC
 *
 *
 * Coverage: renderWithReuse
 * =========================
 *
 *   - P
 *     - behaviour tested in HooksRRTest.testRenderWithReuseNever()
 *     - react-refresh integration tested in RenderWithReuse.P
 *
 *   - CtxObj(P)
 *     - behaviour tested in HooksRRTest.testRenderWithReuseAndUseRef()
 *     - react-refresh integration tested in RenderWithReuse.CtxObj_P
 *
 *   - CtxFn(P)
 *     - behaviour tested in HooksRRTest.testRenderWithReuse()
 *     - react-refresh integration tested in RenderWithReuse.CtxFn_P
 *
 *   - (P, PC)
 *     - behaviour tested in HooksRRTest.testRenderWithReuseNeverPC()
 *     - react-refresh integration tested in RenderWithReuse.P_PC
 *
 *   - CtxObj(P, PC)
 *     - behaviour tested in HooksRRTest.testRenderWithReuseAndUseRefToVdomO()
 *     - react-refresh integration tested in RenderWithReuse.CtxObj_P_PC
 *
 *   - CtxFn(P, PC)
 *     - behaviour tested in HooksRRTest.testRenderWithReuseAndUseRefToVdom()
 *     - react-refresh integration tested in RenderWithReuse.CtxFn_P_PC
 *
 *
 * Coverage: renderWithReuseBy
 * ===========================
 *
 *   - P
 *     - behaviour tested in HooksRRTest.testRenderWithReuseByNever()
 *     - react-refresh integration tested in RenderWithReuseBy.P
 *
 *   - CtxObj(P)
 *     - behaviour tested in HooksRRTest.testRenderWithReuseByAndUseRef()
 *     - react-refresh integration tested in RenderWithReuseBy.CtxObj_P
 *
 *   - CtxFn(P)
 *     - behaviour tested in HooksRRTest.testRenderWithReuseBy()
 *     - react-refresh integration tested in RenderWithReuseBy.CtxFn_P
 *
 *   - (P, PC)
 *     - behaviour tested in HooksRRTest.testRenderWithReuseByNeverPC()
 *     - react-refresh integration tested in RenderWithReuseBy.P_PC
 *
 *   - CtxObj(P, PC)
 *     - behaviour tested in HooksRRTest.testRenderWithReuseByAndUseRefToVdomO()
 *     - react-refresh integration tested in RenderWithReuseBy.CtxObj_P_PC
 *
 *   - CtxFn(P, PC)
 *     - behaviour tested in HooksRRTest.testRenderWithReuseByAndUseRefToVdom()
 *     - react-refresh integration tested in RenderWithReuseBy.CtxFn_P_PC
 */
object AbstractHookMacros {

  final case class HookDefn[Term, TypeTree](steps: Vector[HookStep[Term, TypeTree]]) {
    def +(s: HookStep[Term, TypeTree]): HookDefn[Term, TypeTree] =
      HookDefn(steps :+ s)
  }

  final case class HookStep[Term, TypeTree](name: String, targs: List[TypeTree], args: List[List[Term]]) {
    def desc = name
    def sig = (targs, args)
  }

  @inline def helperRefToComponentInject[P, S, B, CT[-p, +u] <: CtorType[p, u]](c: ScalaComponent.Component[P, S, B, CT], r: Ref.ToScalaComponent[P, S, B]): Ref.WithScalaComponent[P, S, B, CT] =
    Ref.ToComponent.inject(c, r)

  def helperRefToJsComponent[F[_], A[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]](
      ref: Ref.Simple[JsComponent.RawMounted[P0, S0] with R],
      arg: Ref.WithJsComponentArg[F, A, P1, S1, CT1, R, P0, S0]
     ): Ref.WithJsComponent[F, A, P1, S1, CT1, R, P0, S0] =
    arg.wrap(ref.map(JsComponent.mounted[P0, S0](_).addFacade[R]))

  // ===================================================================================================================
  // Hook Rewriter

  // Avoid shadowing in Scala 2.
  final val hookValPrefix = "__japgolly__"

  sealed trait HookRewriter[Stmt, Term <: Stmt, Type, Ref] {
              val bridge      : HookRewriter.Bridge[Stmt, Term, Ref]
    protected val hookNo      : Int
    protected val initialCtx  : HookRewriter.InitialCtx[Stmt, Term, Type]
    protected def initialStmts: Vector[Stmt]
    protected val prevHooks   : List[Term] // excludes discarded hooks
              val usesChildren: Boolean

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
        props :: children :: prevHooks
      else
        props :: prevHooks

    final def argsOrCtxArg(paramCount: Int): List[Term] = {
      val takesHookCtx = (
        ctxContainsHookResults // HookCtx only provided when previous hook results exist
        && paramCount == 1 // Function argument takes a single param
      )
      if (takesHookCtx)
        ctxArg :: Nil
      else
        args
    }

    @inline final def children =
      initialCtx.children

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

    final def ctxContainsHookResults: Boolean =
      prevHooks.nonEmpty

    @inline final def ctxType =
      initialCtx.ctxType

    @inline final def isScala2 =
      bridge.isScala2

    @inline final def isScala3 =
      bridge.isScala3

    @inline final def props =
      initialCtx.props

    final def stmts() =
      _stmts

    final def valDef(body: Term, suffix: String, isLazy: Boolean = false): Ref = {
      val x = bridge.valDef(hookName + suffix, body, isLazy)
      this += x._1
      x._2
    }
  }

  object HookRewriter {

    final case class InitialCtx[Stmt, Term, Type](props: Term, initChildren: Stmt, children: Term, ctxType: Type)

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

    def start[Stmt, Term <: Stmt, Type, Ref](ctx        : InitialCtx[Stmt, Term, Type],
                                             bridg      : Bridge[Stmt, Term, Ref],
                                             useChildren: Boolean): HookRewriter[Stmt, Term, Type, Ref] =
      new HookRewriter[Stmt, Term, Type, Ref] {
        override           val bridge       = bridg
        override protected val hookNo       = 1
        override protected val initialCtx   = ctx
        override protected def initialStmts = if (useChildren) Vector(ctx.initChildren) else Vector.empty
        override protected val prevHooks    = Nil
        override           val usesChildren = useChildren
      }

    def next[Stmt, Term <: Stmt, Type, Ref](prev: HookRewriter[Stmt, Term, Type, Ref])(newHook: Option[Ref]): HookRewriter[Stmt, Term, Type, Ref] =
      new HookRewriter[Stmt, Term, Type, Ref] {
        override           val bridge       = prev.bridge
        override protected val hookNo       = prev.hookNo + 1
        override protected val initialCtx   = prev.initialCtx
        override protected def initialStmts = prev.stmts()
        override protected val prevHooks    = newHook.fold(prev.prevHooks)(prev.prevHooks :+ prev.bridge.refToTerm(_))
        override           val usesChildren = prev.usesChildren
      }

    def end[Stmt, Term <: Stmt, Type, Ref](prev: HookRewriter[Stmt, Term, Type, Ref]): HookRewriter[Stmt, Term, Type, Ref] =
      new HookRewriter[Stmt, Term, Type, Ref] {
        override           val bridge       = prev.bridge
        override protected val hookNo       = -1
        override protected val initialCtx   = prev.initialCtx
        override protected def initialStmts = prev.stmts()
        override protected val prevHooks    = prev.prevHooks
        override           val usesChildren = prev.usesChildren
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
  final type Rewriter       = HookRewriter[Stmt, Term, TypeTree, Ref]
  final type RewriterBridge = HookRewriter.Bridge[Stmt, Term, Ref]
  final type RewriterCtx    = HookRewriter.InitialCtx[Stmt, Term, TypeTree]

  final implicit val log: MacroLogger =
    MacroLogger()

  final def rewriterCtx(props: Term, initChildren: Stmt, children: Term, ctxType: TypeTree): RewriterCtx =
    HookRewriter.InitialCtx(props, initChildren, children, ctxType)

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
      hookFns  <- traverseVector(hookSteps)(rewriteStep(_))
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
  // Rewriting util

  private def by[A](fn: Term, betaReduce: Rewriter => Boolean = null)(use: (Rewriter, Term => Term) => A)
                   (implicit step: HookStep): Either[() => String, Rewriter => A] =
    uninline(fn) match {
      case FunctionLike(paramCount) =>
        Right { b =>
          val args = b.argsOrCtxArg(paramCount)
          val br = if (betaReduce eq null) true else betaReduce(b)
          use(b, call(_, args, br))
        }

      case _ =>
        Left(() => s"Expected a function in ${step.desc}, found: ${showRaw(fn)}")
    }

  private def maybeBy[A](f: Term, betaReduce: Rewriter => Boolean = null)(use: (Rewriter, Term => Term) => A)
                        (implicit step: HookStep): Either[() => String, Rewriter => A] =
    if (step.name endsWith "By")
      by(f, betaReduce = betaReduce)(use)
    else
      Right(use(_, identity))

  private def reusableDeps[D](b: Rewriter, depsExpr: Expr[D], reuse: Expr[Reusability[D]], tpeD: Type[D], depsVal: Boolean = true): (Expr[D], Expr[Int]) = {
    import AutoTypeImplicits._
    type DS      = ReusableDepState[D]
    val tpeODS   = optionType(reusableDepStateType(tpeD))
    val stateRaw = b.valDef(useStateValue[Option[DS]](tpeODS, none(tpeODS)), "_state_raw")
    val state    = b.valDef(useStateFromJsBoxed[Option[DS]](tpeODS, stateRaw), "_state")
    val newDeps  = if (depsVal) autoRefToExpr[D](b.valDef(depsExpr, "_deps")) else depsExpr
    val rds      = b.valDef(reusableDepsLogic[D](newDeps, state, reuse, tpeD), "_deps_state")
    val deps     = reusableDepStateValue(rds, tpeD)
    val rev      = reusableDepStateRev(rds)
    (deps, rev)
  }

  // -------------------------------------------------------------------------------------------------------------------
  def rewriteStep(implicit step: HookStep): Either[() => String, Rewriter => Option[Ref]] = {
    log("rewriteStep:" + step.name, step)

    import AutoTypeImplicits._
    type F[A] = List[A]
    trait X
    trait Y
    trait Z
    type JX = js.Object with X
    type JY = js.Object with Y
    type JZ = js.Object with Z

    def createUseCallbackHook[C](b: Rewriter, ucArg: Term, callback: Expr[C], hookDeps: Expr[HookDeps], tpeC: Type[C]): Ref = {
      val a          = refToTerm(b.valDef(ucArg, "_arg")) // stablise for dependent types
      type J         = js.Function
      val tpeJ       = useCallbackArgTypeJs[C, J](a)
      val jsCallback = b.valDef(useCallbackArgToJs[C, J](a, callback, tpeC, tpeJ), "_jscb")
      val raw        = b.createRaw(useCallback[J](jsCallback, hookDeps, tpeJ))
      val hook       = useCallbackArgFromJs[C, J](a, raw, tpeC, tpeJ)
      b.createHook(hook)
    }

    def reusableByDeps[D, A](b: Rewriter, depsExpr: Expr[D], reuse: Expr[Reusability[D]], tpeD: Type[D], tpeA: Type[A])
                            (create: (Expr[D], Expr[Int]) => Expr[A]): Expr[Reusable[A]] = {
      val (deps, rev) = reusableDeps[D](b, depsExpr, reuse, tpeD)
      val value       = b.valDef(create(deps, rev), "_val")
      reusableValueByInt[A](rev, value, tpeA)
    }

    // This is `def unsafeCreateSimple[A](): Ref.Simple[A]` in `Hooks`
    def simpleRef[A](b: Rewriter, tpe: Type[A]): Expr[Ref.Simple[A]] = {
      val raw = b.createRaw(useRefOrNull[A](tpe))
      refFromJs(raw, tpe)
    }

    implicit def autoSomeRefs(r: Ref): Option[Ref] =
      Some(r)

    def fail =
      Left(() => s"Inlining of '${step.desc}' not yet supported.")

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

      case "useForceUpdate" =>
        Right { b =>
          val s = b.valDef(useForceUpdate1, "_state")
          b.createHook(useForceUpdate2(s))
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

      case "useRef" | "useRefBy" =>
        val (List(tpe), List(List(valueFn), List(_))) = step.sig : @nowarn
        maybeBy(valueFn) { (b, withCtx) =>
          val value = withCtx(valueFn)
          val raw = b.createRaw(useRef[X](value, tpe))
          b.createHook(useRefFromJs[X](raw, tpe))
        }

      case "useRefToAnyVdom" =>
        Right { b =>
          b.createHook(simpleRef[TopNode](b, topNodeType))
        }

      case "useRefToJsComponent" =>
        step.sig match {
          case (List(tpeP, tpeS), List(_)) =>
            type P = JX
            type S = JY
            Right { b =>
              val tpeM = jsComponentRawMountedType[P, S](tpeP, tpeS)
              val ref  = simpleRef[JsComponent.RawMounted[P, S]](b, tpeM)
              b.createHook(refMapJsMounted[P, S](ref, tpeP, tpeS))
            }

          case (List(tpeF, tpeA, tpeP1, tpeS1, tpeCT1, tpeR, tpeP0, tpeS0), List(List(arg), List(_))) =>
            type P    = JX
            type S    = JY
            type R    = js.Object with JsComponent.RawMounted[P, S]
            Right { b =>
              val tpeM = jsComponentRawMountedTypeWithFacade[P, S, R](tpeP0, tpeS0, tpeR)
              val ref  = simpleRef[JsComponent.RawMounted[P, S] with R](b, tpeM)
              val hook = refWithJsComponentArgHelper[F, F, P, S, CtorType, R, P, S, CtorType](ref, arg, tpeF, tpeA, tpeP1, tpeS1, tpeCT1, tpeR, tpeP0, tpeS0)
              b.createHook(hook)
            }

          case _ => fail
        }

      case "useRefToJsComponentWithMountedFacade" =>
        val (List(tpeP, tpeS, tpeF), List(List(_))) = step.sig : @nowarn
        type P = JX
        type S = JY
        type F = JZ
        Right { b =>
          val tpeM = jsComponentRawMountedTypeWithFacade[P, S, F](tpeP, tpeS, tpeF)
          val ref  = simpleRef[JsComponent.RawMounted[P, S] with F](b, tpeM)
          val hook = refMapJsMountedWithFacade[P, S, F](ref, tpeP, tpeS, tpeF)
          b.createHook(hook)
        }

      case "useRefToScalaComponent" =>
        def build[P, S, B](b: Rewriter, tpeP: Type[P], tpeS: Type[S], tpeB: Type[B]) = {
          val tpeM = scalaComponentRawMountedType[P, S, B](tpeP, tpeS, tpeB)
          val ref  = simpleRef[ScalaComponent.RawMounted[P, S, B]](b, tpeM)
          refMapMountedImpure(ref, tpeP, tpeS, tpeB)
        }
        step.sig match {
          case (List(tpeP, tpeS, tpeB), List(List(_))) =>
            Right { b =>
              b.createHook(build[X, Y, Z](b, tpeP, tpeS, tpeB))
            }

          case (List(tpeP, tpeS, tpeB, tpeCT), List(List(comp), List(_))) =>
            Right { b =>
              val ref = build[X, Y, Z](b, tpeP, tpeS, tpeB)
              b.createHook(refToComponentInject[X, Y, Z, CtorType](comp, ref, tpeP, tpeS, tpeB, tpeCT))
            }

          case _ => fail
        }

      case "useRefToVdom" =>
        val (List(tpe), List(List(ct, _))) = step.sig : @nowarn
        Right { b =>
          val sup = simpleRef[TopNode](b, topNodeType)
          b.createHook(refNarrowOption[TopNode, TopNode](sup, ct, topNodeType, tpe))
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
        fail
    }
  }

  protected def custom[I, O]: (Type[I], Type[O], Expr[CustomHook[I, O]], Expr[I]) => Expr[O]

  protected def customArg[C, A]: (Type[C], Type[A], Expr[CustomHook.Arg[C, A]], Expr[C]) => Expr[A]

  protected def hookDepsEmptyArray: Expr[HookDeps]

  protected def hookDepsIntArray1: Expr[Int] => Expr[HookDeps]

  protected def hookDepsUndefined: Expr[HookDeps] =
    unitTerm.asInstanceOf[Expr[HookDeps]]

  protected def hooksVar[A]: (Type[A], Expr[A]) => Expr[Hooks.Var[A]]

  protected def jsComponentRawMountedType[P <: js.Object, S <: js.Object]: (Type[P], Type[S]) => Type[JsComponent.RawMounted[P, S]]

  protected def jsComponentRawMountedTypeWithFacade[P <: js.Object, S <: js.Object, F]: (Type[P], Type[S], Type[F]) => Type[JsComponent.RawMounted[P, S] with F]

  protected def none[A]: Type[Option[A]] => Expr[Option[A]]

  protected def optionType[A]: Type[A] => Type[Option[A]]

  protected def refFromJs[A]: (Expr[React.RefHandle[A | Null]], Type[A]) => Expr[Ref.Simple[A]]

  protected def refMapJsMounted[P <: js.Object, S <: js.Object]: (Expr[Ref.Simple[JsComponent.RawMounted[P, S]]], Type[P], Type[S]) => Expr[Ref.ToJsComponent[P, S, JsComponent.RawMounted[P, S]]]

  protected def refMapJsMountedWithFacade[P <: js.Object, S <: js.Object, F <: js.Object]: (Expr[Ref.Simple[JsComponent.RawMounted[P, S] with F]], Type[P], Type[S], Type[F]) => Expr[Ref.ToJsComponent[P, S, JsComponent.RawMounted[P, S] with F]]

  protected def refMapMountedImpure[P, S, B]: (Expr[Ref.Simple[ScalaComponent.RawMounted[P, S, B]]], Type[P], Type[S], Type[B]) => Expr[Ref.ToScalaComponent[P, S, B]]

  protected def refWithJsComponentArgHelper[F[_], A[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]]:
    (Expr[Ref.Simple[JsComponent.RawMounted[P0, S0] with R]], Expr[Ref.WithJsComponentArg[F, A, P1, S1, CT1, R, P0, S0]],
     TypeTree, TypeTree, Type[P1], Type[S1], TypeTree, Type[R], Type[P0], Type[S0]
    ) => Expr[Ref.WithJsComponent[F, A, P1, S1, CT1, R, P0, S0]]

  protected def refNarrowOption[A, B <: A]: (Expr[Ref.Simple[A]], Expr[ClassTag[B]], Type[A], Type[B]) => Expr[Ref.Full[A, A, B]]

  protected def refToComponentInject[P, S, B, CT[-p, +u] <: CtorType[p, u]]: (Expr[ScalaComponent.Component[P, S, B, CT]], Expr[Ref.ToScalaComponent[P, S, B]], Type[P], Type[S], Type[B], TypeTree) => Expr[Ref.WithScalaComponent[P, S, B, CT]]

  protected def reusableDepsLogic[D]: (Expr[D], Expr[Hooks.UseState[Option[ReusableDepState[D]]]], Expr[Reusability[D]], Type[D]) => Expr[ReusableDepState[D]]

  protected def reusableDepStateRev: Expr[ReusableDepState[Any]] => Expr[Int]

  protected def reusableDepStateType[D]: Type[D] => Type[ReusableDepState[D]]

  protected def reusableDepStateValue[D]: (Expr[ReusableDepState[D]], Type[D]) => Expr[D]

  protected def reusableValueByInt[A]: (Expr[Int], Expr[A], Type[A]) => Expr[Reusable[A]]

  protected def topNodeType: Type[TopNode]

  protected def scalaComponentRawMountedType[P, S, B]: (Type[P], Type[S], Type[B]) => Type[ScalaComponent.RawMounted[P, S, B]]

  protected def scalaFn0[A]: (Type[A], Expr[A]) => Expr[() => A]

  protected def useCallback[F <: js.Function]: (Expr[F], Expr[HookDeps], Type[F]) => Expr[F]

  protected def useCallbackArgFromJs[A, J <: js.Function]: (Expr[Hooks.UseCallbackArg.To[A, J]], Expr[J], Type[A], Type[J]) => Expr[Reusable[A]]

  protected def useCallbackArgToJs[A, J <: js.Function]: (Expr[Hooks.UseCallbackArg.To[A, J]], Expr[A], Type[A], Type[J]) => Expr[J]

  protected def useCallbackArgTypeJs[A, J <: js.Function]: (Expr[Hooks.UseCallbackArg.To[A, J]]) => Type[J]

  protected def useContext[A]: (Expr[Context[A]], Type[A]) => Expr[A]

  protected def useDebugValue: Expr[Any] => Expr[Unit]

  protected def useEffect: (Expr[React.UseEffectArg], Expr[HookDeps]) => Expr[Unit]

  protected def useEffectArgToJs[A]: (Expr[Hooks.UseEffectArg[A]], Expr[A], Type[A]) => Expr[React.UseEffectArg]

  protected def useForceUpdate1: Expr[React.UseState[Int]]

  protected def useForceUpdate2: Expr[React.UseState[Int]] => Expr[Reusable[DefaultEffects.Sync[Unit]]]

  protected def useLayoutEffect: (Expr[React.UseEffectArg], Expr[HookDeps]) => Expr[Unit]

  protected def useMemo[A]: (Expr[A], Expr[HookDeps], Type[A]) => Expr[A]

  protected def useReducer[S, A]: (Expr[(S, A) => S], Expr[S], Type[S], Type[A]) => Expr[React.UseReducer[S, A]]

  protected def useReducerFromJs[S, A]: (Expr[React.UseReducer[S, A]], Type[S], Type[A]) => Expr[Hooks.UseReducer[S, A]]

  protected def useRef[A]: (Expr[A], Type[A]) => Expr[React.RefHandle[A]]

  protected def useRefOrNull[A]: Type[A] => Expr[React.RefHandle[A | Null]]

  protected def useRefFromJs[A]: (Expr[React.RefHandle[A]], Type[A]) => Expr[Hooks.UseRef[A]]

  protected def useStateFn[S]: (Type[S], Expr[S]) => Expr[React.UseState[Box[S]]]

  protected def useStateValue[S]: (Type[S], Expr[S]) => Expr[React.UseState[Box[S]]]

  protected def useStateFromJsBoxed[S]: (Type[S], Expr[React.UseState[Box[S]]]) => Expr[Hooks.UseState[S]]

  protected def useStateWithReuseFromJsBoxed[S]: (Type[S], Expr[React.UseState[Box[S]]], Expr[Reusability[S]], Expr[ClassTag[S]]) => Expr[Hooks.UseStateWithReuse[S]]

  protected def vdomRawNode: Expr[VdomNode] => Expr[React.Node]

  // -------------------------------------------------------------------------------------------------------------------
  private def rewriteRender(implicit step: HookStep): Either[() => String, Rewriter => Term] = {
    log("rewriteRender:" + step.name, step)

    def withParamCount(fn: Term): Either[() => String, Int] =
        uninline(fn) match {
          case FunctionLike(paramCount) => Right(paramCount)
          case f                        => Left(() => s"Expected a function in ${step.desc}, found: ${showRaw(f)}")
        }

    import AutoTypeImplicits._

    def shouldComponentUpdate[D](b: Rewriter, depsExpr: Expr[D], render: Expr[D => VdomNode], reuse: Expr[Reusability[D]], tpe: Type[D]): Expr[VdomNode] = {
      val (deps, rev) = reusableDeps[D](b, depsExpr, reuse, tpe, depsVal = false)
      val body        = call(render, deps :: Nil, true)
      shouldComponentUpdateComponent(rev, body)
    }

    trait Ctx
    trait D

    def fail =
      Left(() => s"Inlining of '${step.desc}' not yet supported.")

    step.name.stripSuffix("Debug") match {

      case "renderRR" =>
        val List(List(fn), _) = step.args : @nowarn
        withParamCount(fn).map { paramCount => b =>
          val args = b.argsOrCtxArg(paramCount)
          call(fn, args, true)
        }

      case "renderRRReusable" =>
        val (List(tpe), List(List(renderFn), implicits)) = step.sig : @nowarn
        val implicitCount = implicits.size
        withParamCount(renderFn).map { paramCount => b =>
          val mkVdom      = implicits.last
          val reusableA   = call(renderFn, b.argsOrCtxArg(paramCount), true)
          val tpeV        = vdomNodeType
          val tpeRV       = reusableType[VdomNode](tpeV)
          val reusableV   = reusableMap[D, VdomNode](reusableA, mkVdom, tpe, tpeV)
          val reuse       = reusabilityReusable[VdomNode](tpeV)
          val (deps, rev) = reusableDeps[Reusable[VdomNode]](b, reusableV, reuse, tpeRV)
          val body        = reusableValue[VdomNode](deps, tpeV)
          shouldComponentUpdateComponent(rev, body)
        }

      case "renderRRWithReuse" =>
        val List(List(renderFn), implicits) = step.args : @nowarn
        val implicitCount = implicits.size
        withParamCount(renderFn).map { paramCount => b =>
          val reuse       = implicits.last
          val ctx         = if (paramCount == 1 && !b.ctxContainsHookResults) b.props else b.ctxArg
          val (deps, rev) = reusableDeps[Ctx](b, ctx, reuse, b.ctxType, depsVal = false)
          // log(s"paramCount = $paramCount, implicitCount = $implicitCount, usesChildren = ${b.usesChildren}, ctxContainsHookResults = ${b.ctxContainsHookResults}")
          val renderArgs: List[Term] =
            if (implicitCount == 3)
              b.argsOrCtxArg(paramCount)
            else if (b.usesChildren && paramCount == 2)
              b.props :: b.children :: Nil
            else if (b.ctxContainsHookResults)
              deps :: Nil
            else
              b.props :: Nil
          val body = call(renderFn, renderArgs, true)
          shouldComponentUpdateComponent(rev, body)
        }

      case "renderRRWithReuseBy" =>
        val List(tpe) = step.targs : @nowarn
        val List(List(reusableInputs), List(renderFn), implicits) = step.args : @nowarn
        by(reusableInputs) { (b, withCtx) =>
          val reuse = implicits.last
          val deps: Expr[D] =
            if (b.ctxContainsHookResults)
              withCtx(reusableInputs)
            else if (b.usesChildren)
              call(reusableInputs, b.props :: b.children :: Nil, true)
            else
              call(reusableInputs, b.props :: Nil, true)
          shouldComponentUpdate[D](b, deps, renderFn, reuse, tpe)
        }

      case _ =>
        fail
    }
  }

  protected def reusabilityReusable[A]: Type[A] => Expr[Reusability[Reusable[A]]]

  protected def reusableMap[A, B]: (Expr[Reusable[A]], Expr[A => B], Type[A], Type[B]) => Expr[Reusable[B]]

  protected def reusableType[A]: Type[A] => Type[Reusable[A]]

  protected def reusableValue[A]: (Expr[Reusable[A]], Type[A]) => Expr[A]

  protected def shouldComponentUpdateComponent: (Expr[Int], Expr[VdomNode]) => Expr[VdomNode]

  protected def vdomNodeType: Type[VdomNode]
}
