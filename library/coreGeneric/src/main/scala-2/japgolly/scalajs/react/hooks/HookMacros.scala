package japgolly.scalajs.react.hooks

import japgolly.microlibs.compiletime.MacroUtils
import japgolly.scalajs.react.component.{Js => JsComponent, Scala => ScalaComponent}
import japgolly.scalajs.react.hooks.Api._
import japgolly.scalajs.react.hooks.CustomHook.ReusableDepState
import japgolly.scalajs.react.internal.{Box, MacroLogger}
import japgolly.scalajs.react.vdom.TopNode
import japgolly.scalajs.react.{Children, CtorType}
import scala.reflect.macros.blackbox.Context
import scala.scalajs.js

object HookMacros {
  type With[A, B] = A with B
}

class HookMacros(val c: Context) extends MacroUtils {
  import c.universe._

  // -------------------------------------------------------------------------------------------------------------------

  def render1[P, C <: Children](f: c.Tree)(s: c.Tree)(implicit P: c.WeakTypeTag[P], C: c.WeakTypeTag[C]): c.Tree =
    _render1(f, s, false)(P, C)

  def renderDebug1[P, C <: Children](f: c.Tree)(s: c.Tree)(implicit P: c.WeakTypeTag[P], C: c.WeakTypeTag[C]): c.Tree =
    _render1(f, s, true)(P, C)

  private def _render1[P, C <: Children](f: c.Tree, s: c.Tree, debug: Boolean)(implicit P: c.WeakTypeTag[P], C: c.WeakTypeTag[C]): c.Tree =
    _render[P, C](f, None, s, debug)

  // -------------------------------------------------------------------------------------------------------------------

  def renderC1[P](f: c.Tree)(s: c.Tree)(implicit P: c.WeakTypeTag[P]): c.Tree =
    _renderC1(f, s, false)(P)

  def renderDebugC1[P](f: c.Tree)(s: c.Tree)(implicit P: c.WeakTypeTag[P]): c.Tree =
    _renderC1(f, s, true)(P)

  private def _renderC1[P](renderFn: c.Tree, summoner: c.Tree, debug: Boolean)(implicit P: c.WeakTypeTag[P]): c.Tree =
    _render[P, Children.Varargs](renderFn, None, summoner, debug)

  // -------------------------------------------------------------------------------------------------------------------

  def render2[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]](f: c.Tree)(step: c.Tree, s: c.Tree)(implicit P: c.WeakTypeTag[P], C: c.WeakTypeTag[C]): c.Tree =
    _render2(f, step, s, false)(P, C)

  def renderDebug2[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]](f: c.Tree)(step: c.Tree, s: c.Tree)(implicit P: c.WeakTypeTag[P], C: c.WeakTypeTag[C]): c.Tree =
    _render2(f, step, s, true)(P, C)

  private def _render2[P, C <: Children](f: c.Tree, step: c.Tree, s: c.Tree, debug: Boolean)(implicit P: c.WeakTypeTag[P], C: c.WeakTypeTag[C]): c.Tree =
    _render[P, C](f, Some(step), s, debug)

  // ===================================================================================================================

  private implicit def autoTagToType[A](t: c.WeakTypeTag[A]): Type = t.tpe

  private def AHM          : Tree = q"_root_.japgolly.scalajs.react.hooks.AbstractHookMacros"
  private def Box          : Tree = q"_root_.japgolly.scalajs.react.internal.Box"
  private def Box(t: Type) : Type = appliedType(c.typeOf[Box[_]], t)
  private def CustomHook   : Tree = q"_root_.japgolly.scalajs.react.hooks.CustomHook"
  private def HookCtx      : Tree = q"_root_.japgolly.scalajs.react.hooks.HookCtx"
  private def Hooks        : Tree = q"_root_.japgolly.scalajs.react.hooks.Hooks"
  private def JsComp       : Tree = q"_root_.japgolly.scalajs.react.component.Js"
  private def JsFn         : Tree = q"_root_.japgolly.scalajs.react.component.JsFn"
  private def PropsChildren: Tree = q"_root_.japgolly.scalajs.react.PropsChildren"
  private def React        : Tree = q"_root_.japgolly.scalajs.react.facade.React"
  private def Reusable     : Tree = q"_root_.japgolly.scalajs.react.Reusable"
  private def ScalaFn      : Tree = q"_root_.japgolly.scalajs.react.component.ScalaFn"
  private def ScalaRef     : Tree = q"_root_.japgolly.scalajs.react.Ref"
  private def SJS          : Tree = q"_root_.scala.scalajs.js"

  private final class HookMacrosImpl extends AbstractHookMacros {
    import AbstractHookMacros._

    override type Expr[+A] = Term
    override type Ref      = TermName
    override type Stmt     = c.universe.Tree
    override type Term     = c.universe.Tree
    override type Type[A]  = c.universe.Type
    override type TypeTree = c.universe.TypeTree

    override protected def asTerm    [A] = identity
    override protected def Expr      [A] = identity
    override protected def isUnit        = _.tpe == unitType
    override protected def refToTerm     = Ident(_)
    override           def showCode      = c.universe.showCode(_).replace("_root_.", "").replace(".`package`.", ".")
    override protected def showRaw       = c.universe.showRaw(_)
    override protected def Type      [A] = _.tpe
    override protected def typeOfTerm    = t => c.universe.TypeTree(t.tpe)
    override protected def uninline      = identity
    override protected def unitTerm      = q"()"
    override protected def unitType      = c.universe.definitions.UnitTpe
    override protected def wrap          = (s, b) => Block(s.toList, b)

    override protected def call = (function, args, betaReduce) => {
      import internal._
      function match {
        case Function(params, body) if betaReduce =>

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

    override protected val rewriterBridge: RewriterBridge =
      HookRewriter.Bridge[Stmt, Term, Ref](
        apply     = Apply(_, _),
        hookCtx   = (c, as) => Apply(if (c) q"$HookCtx.withChildren" else HookCtx, as),
        refToTerm = r => Ident(r),
        scalaVer  = 2,
        valDef    = (n, t, l) => {
                      val r = TermName(n)
                      val stmt = if (l) q"lazy val $r = $t" else q"val $r = $t"
                      (stmt, r)
                    },
      )

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

    protected lazy val debugLog = MacroLogger(true)

    override protected def custom[I, O] = (_, _, hook, i) =>
      q"$hook.unsafeInit($i)"

    override protected def customArg[Ctx, Arg] = (_, _, hookArg, ctx) =>
      q"$hookArg.convert($ctx)"

    override protected def hookDepsEmptyArray =
      q"$SJS.Array[Any]()"

    override protected def hookDepsIntArray1 = i =>
      q"$SJS.Array[Int]($i)"

    override protected def hooksVar[A] = (_, body) =>
      q"$Hooks.Var($body)"

    override protected def jsComponentRawMountedType[P <: js.Object, S <: js.Object] = (tpeP, tpeS) =>
      appliedType(c.typeOf[JsComponent.RawMounted[_, _]], tpeP, tpeS)

    override protected def jsComponentRawMountedTypeWithFacade[P <: js.Object, S <: js.Object, F] = (tpeP, tpeS, tpeF) =>
      appliedType(c.typeOf[HookMacros.With[_, _]],
        appliedType(c.typeOf[JsComponent.RawMounted[_, _]], tpeP, tpeS),
        tpeF)

    override protected def none[A] = _ =>
      q"None"

    override protected def optionType[A] = a =>
      appliedType(c.typeOf[Option[_]], a)

    override protected def refFromJs[A] = (ref, _) =>
      q"$ScalaRef.fromJs($ref)"

    override protected def refMapJsMounted[P <: js.Object, S <: js.Object] = (ref, _, _) =>
      q"$ref.map($JsComp.mounted(_))"

    override protected def refMapJsMountedWithFacade[P <: js.Object, S <: js.Object, F <: js.Object] = (ref, tpeP, tpeS, tpeF) =>
      q"$ref.map(JsComponent.mounted[$tpeP, $tpeS](_).addFacade[$tpeF])"

    override protected def refMapMountedImpure[P, S, B] = (ref, _, _, _) =>
      q"$ref.map(_.mountedImpure)"

    override protected def refWithJsComponentArgHelper[F[_], A[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]] =
      (r, a, _, _, _, _, _, _, _, _) =>
        q"$AHM.helperRefToJsComponent($r, $a)"

    override protected def refNarrowOption[A, B <: A] = (ref, ct, _, tpeB) =>
      q"$ref.narrowOption[$tpeB]($ct)"

    override protected def refToComponentInject[P, S, B, CT[-p, +u] <: CtorType[p, u]] = (c, r, _, _, _, _) =>
      q"$AHM.helperRefToComponentInject($c, $r)"

    override protected def reusableDepsLogic[D] = (d, s, r, tpeD) =>
      q"$CustomHook.reusableDepsLogic[$tpeD]($d)($s)($r)"

    override protected def reusableDepStateRev = rds =>
      q"$rds.rev"

    override protected def reusableDepStateType[D] =  d =>
      appliedType(c.typeOf[ReusableDepState[_]], d)

    override protected def reusableDepStateValue[D] = (rds, _) =>
      q"$rds.value"

    override protected def reusableValueByInt[A] = (i, a, _) =>
      q"$Reusable.implicitly($i).withValue($a)"

    override protected def topNodeType =
      c.typeOf[TopNode]

    override protected def scalaComponentRawMountedType[P, S, B] = (tpeS, tpeP, tpeB) =>
      appliedType(c.typeOf[ScalaComponent.RawMounted[_, _, _]], tpeS, tpeP, tpeB)

    override protected def scalaFn0[A] = (_, body) =>
      q"() => $body"

    override protected def useCallback[F <: js.Function] = (f, deps, _) =>
      q"$React.useCallback($f, $deps)"

    override protected def useCallbackArgFromJs[A, J <: js.Function] = (x, j, _, _) =>
      q"$x.fromJs($j)"

    override protected def useCallbackArgToJs[A, J <: js.Function] = (x, a, _, _) =>
      q"$x.toJs($a)"

    override protected def useCallbackArgTypeJs[A, J <: js.Function] = _ =>
      null // Not used in Scala 2 (i.e. in useCallbackArg{From,To}Js)

    override protected def useContext[A] = (ctx, _) =>
      q"$React.useContext($ctx.raw)"

    override protected def useDebugValue = desc =>
      q"$React.useDebugValue[Null](null, _ => $desc)"

    override protected def useEffect = (a, d) =>
      q"$React.useEffect($a, $d)"

    override protected def useEffectArgToJs[A] = (arg, a, _) =>
      q"$arg.toJs($a)"

    override protected def useForceUpdate1 =
      q"$React.useStateValue(0)"

    override protected def useForceUpdate2 = s =>
      q"$CustomHook.useForceUpdateRaw($s)"

    override protected def useLayoutEffect = (a, d) =>
      q"$React.useLayoutEffect($a, $d)"

    override protected def useMemo[A] = (a, d, _) =>
      q"$React.useMemo(() => $a, $d)"

    override protected def useReducer[S, A] = (r, s, tpeS, tpeA) =>
      q"$React.useReducer[Null, $tpeS, $tpeA]($r, null, _ => $s)"

    override protected def useReducerFromJs[S, A] = (raw, _, _) =>
      q"$Hooks.UseReducer.fromJs($raw)"

    override protected def useRef[A] = (a, _) =>
      q"$React.useRef($a)"

    override protected def useRefOrNull[A] = (tpe) => {
      val t = appliedType(c.typeOf[js.|[_, _]], tpe, definitions.NullTpe)
      q"$React.useRef[$t](null)"
    }

    override protected def useRefFromJs[A] = (ref, _) =>
      q"$Hooks.UseRef.fromJs($ref)"

    override protected def useStateFn[S] = (tpe, body) =>
      q"$React.useStateFn(() => $Box[$tpe]($body))"

    override protected def useStateValue[S] = (tpe, body) =>
      q"$React.useStateValue($Box[$tpe]($body))"

    override protected def useStateFromJsBoxed[S] = (tpe, raw) =>
      q"$Hooks.UseState.fromJsBoxed[$tpe]($raw)"

    override protected def useStateWithReuseFromJsBoxed[S] = (tpe, raw, reuse, ct) =>
      q"$Hooks.UseStateWithReuse.fromJsBoxed[$tpe]($raw)($reuse, $ct)"

    override protected def vdomRawNode = vdom =>
      q"$vdom.rawNode"
  }

  // ===================================================================================================================

  def _render[P, C <: Children]
             (renderFn: c.Tree, stepOption: Option[c.Tree], summoner: c.Tree, debug: Boolean)
             (implicit P: c.WeakTypeTag[P], C: c.WeakTypeTag[C]): c.Tree = {

    val hookMacros = new HookMacrosImpl

    import hookMacros.log
    log.enabled = debug
    log.header()

    def giveUp: Tree = {
      val self = c.prefix
      stepOption match {
        case Some(step) =>
          q"""
            val f = $step.squash($renderFn)
            $self.render(f)($summoner)
          """
        case None =>
          q"$self.render($renderFn)($summoner)"
      }
    }

    def onFailure(msg: String): Tree = {
      import Console._
      log(RED_B + WHITE + "Giving up. " + msg + RESET)
      c.warning(c.enclosingPosition, msg)
      giveUp
    }

    val result: Tree =
      try {

        val rewriteAttempt =
          for {
            hookDefn <- hookMacros.parse(c.macroApplication)
            rewriter <- hookMacros.rewriteComponent(hookDefn)
          } yield rewriter

        rewriteAttempt match {

          case Right(rewriter) =>
            val ctx = hookMacros.rewriterCtx(
              props        = q"props.unbox",
              initChildren = q"val children = $PropsChildren.fromRawProps(props)",
              children     = q"children",
            )
            val newBody = rewriter(ctx)
            c.untypecheck(q"""
              val rawComponent: $JsFn.RawComponent[${Box(P)}] = props => $newBody
              $ScalaFn.fromBoxed($JsFn.fromJsFn[${Box(P)}, $C](rawComponent)($summoner))
            """)

          case Left(err) =>
            onFailure(err())
        }

      } catch {
        case err: Throwable =>
          err.printStackTrace()
          onFailure(err.getMessage())
      }

    log.footer(hookMacros.showCode(result))
    result
  }
}
