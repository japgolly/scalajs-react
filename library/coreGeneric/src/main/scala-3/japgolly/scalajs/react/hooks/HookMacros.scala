package japgolly.scalajs.react.hooks

import japgolly.microlibs.compiletime.MacroEnv.*
import japgolly.scalajs.react.{Children, CtorType, PropsChildren, Reusability}
import japgolly.scalajs.react.component.{JsFn, ScalaFn}
import japgolly.scalajs.react.component.ScalaFn.Component
import japgolly.scalajs.react.facade
import japgolly.scalajs.react.facade.React
import japgolly.scalajs.react.hooks.Api.*
import japgolly.scalajs.react.hooks.HookCtx
import japgolly.scalajs.react.internal.{Box, MacroLogger}
import japgolly.scalajs.react.vdom.VdomNode
import scala.annotation._
import scala.language.`3.1`
import scala.quoted.*
import scala.reflect.ClassTag
import scala.scalajs.js

object HookMacros {

  // https://github.com/lampepfl/dotty/issues/15357
  inline def renderWorkaround[
        P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]
      ](self : PrimaryWithRender[P, C, Ctx, Step] with Secondary[Ctx, CtxFn, Step],
        f    : CtxFn[VdomNode],
        step : Step,
        s    : CtorType.Summoner.Aux[Box[P], C, CT],
       ): Component[P, CT] =
    ${ renderMacro[P, C, Ctx, CtxFn, Step, CT]('self, 'f, 'step, 's) }

  inline def renderWorkaroundDebug[
        P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]
      ](self : PrimaryWithRender[P, C, Ctx, Step] with Secondary[Ctx, CtxFn, Step],
        f    : CtxFn[VdomNode],
        step : Step,
        s    : CtorType.Summoner.Aux[Box[P], C, CT],
       ): Component[P, CT] =
    ${ renderMacroDebug[P, C, Ctx, CtxFn, Step, CT]('self, 'f, 'step, 's) }

  def renderMacro[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]]
      (self: Expr[PrimaryWithRender[P, C, Ctx, Step] with Secondary[Ctx, CtxFn, Step]],
      f    : Expr[CtxFn[VdomNode]],
      step : Expr[Step],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
     )(using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], CtxFn: Type[CtxFn], Step: Type[Step]): Expr[Component[P, CT]] =
    _renderMacro(self, f, step, s, false)

  def renderMacroDebug[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]]
      (self: Expr[PrimaryWithRender[P, C, Ctx, Step] with Secondary[Ctx, CtxFn, Step]],
      f    : Expr[CtxFn[VdomNode]],
      step : Expr[Step],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
     )(using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], CtxFn: Type[CtxFn], Step: Type[Step]): Expr[Component[P, CT]] =
    _renderMacro(self, f, step, s, true)

  // ===================================================================================================================

  private def HookMacrosImpl(qq: Quotes): HookMacrosImpl { val q: qq.type } =
    new HookMacrosImpl {
      override implicit val q: qq.type = qq
    }

  @nowarn("msg=Consider using canonical type reference .+Underlying instead")
  private trait HookMacrosImpl extends AbstractHookMacros {
    import AbstractHookMacros._

    implicit val q: Quotes
    import q.reflect.*

    override type Expr[A]  = scala.quoted.Expr[A]
    override type Ref      = UntypedValDef.WithQuotes[q.type]
    override type Stmt     = q.reflect.Statement
    override type Term     = q.reflect.Term
    override type Type[A]  = scala.quoted.Type[A]
    override type TypeTree = q.reflect.TypeTree

    override protected def asTerm    [A](e: Expr[A]) = e.asTerm
    override protected def Expr      [A](t: Term)    = t.asExprOf[Any].asInstanceOf[Expr[A]]
    override protected def refToTerm    (r: Ref)     = r.ref
    override protected def showCode     (t: Term)    = t.show(using q.reflect.Printer.TreeShortCode)
    override protected def showRaw      (t: Term)    = t.show(using q.reflect.Printer.TreeStructure)
    override protected def typeOfTerm   (t: Term)    = t.tpe.asTypeTree
    override protected def unitTerm                  = '{ () }
    override protected def unitType                  = scala.quoted.Type.of[Unit]
    override protected def wrap                      = (s, b) => Block(s.toList, b)

    override def call(function: Term, args: List[Term]): Term = {
      val f = extractFunction(function)
      val t = Apply(Select.unique(f, "apply"), args)
      Term.betaReduce(t).getOrElse(t)
    }

    @tailrec
    private def extractFunction(term: Term): Term =
      term match {
        case Inlined(None, Nil, f) => extractFunction(f)
        case Block(Nil, expr)      => extractFunction(expr)
        case f                     => f
      }

    override protected def isUnit(t: TypeTree): Boolean =
      t.tpe.asType match {
        case '[Unit] => true
        case _       => false
      }

    override protected def Type[A](t: TypeTree) = {
      val x: scala.quoted.Type[?] = t.asType
      x.asInstanceOf[scala.quoted.Type[A]]
    }

    override protected val rewriterBridge: RewriterBridge =
      HookRewriter.Bridge[Stmt, Term, Ref](
        apply     = Apply(_, _),
        hookCtx   = (c, as) => Select.overloaded((if (c) '{HookCtx.withChildren} else '{HookCtx}).asTerm, "apply", as.map(_.tpe), as),
        refToTerm = _.ref,
        valDef    = (n, t) => { val r = untypedValDef(n, t.tpe, Flags.EmptyFlags)(t); (r.valDef, r) },
      )

    override val ApplyLike = new ApplyExtractor {
      override def unapply(a: Term) = a match {
        case Apply(x, y) => Some((x, y))
        case _           => None
      }
    }

    override val TypeApplyLike = new TypeApplyExtractor {
      override def unapply(a: Term) =  a match {
        case TypeApply(x, y) => Some((x, y))
        case _               => None
      }
    }

    override val SelectLike = new SelectExtractor {
      override def unapply(a: Term) =  a match {
        case Select(x, y) => Some((x, y))
        case _            => None
      }
    }

    override val FunctionLike = new FunctionExtractor {
      override def unapply(function: Term) = function match {

        case Apply(TypeApply(f, args), _) if f.tpe.show.startsWith("scala.scalajs.js.Any.toFunction") =>
          Some(args.size - 1)

        case Block(List(DefDef(_, List(TermParamClause(params)), _, Some(_))), Closure(Ident(_), _)) =>
          Some(params.size)

        case i@ Ident(_) =>
          i.symbol.tree match {
            case d: DefDef => byTypeRepr(d.returnTpt.tpe)
            case v: ValDef => byTypeRepr(v.tpt.tpe)
            case _         => None
          }

        case _ =>
          None
      }

      private def byTypeRepr(t: TypeRepr): Option[Success] = t match {
        case AppliedType(TypeRef(ThisType(_), f), args) if f.startsWith("Function") =>
          Some(args.size - 1)
        case _ =>
          None
      }
    }

    override protected def custom[I, O] = implicit (ti, to, hook, i) =>
      '{ $hook.unsafeInit($i) }

    override protected def customArg[Ctx, Arg] = implicit (tc, ta, hookArg, ctx) =>
      '{ $hookArg.convert($ctx) }

    override protected def useStateFn[S] = implicit (tpe, body) =>
      '{ React.useStateFn(() => Box[$tpe]($body)) }

    override protected def useStateFromJsBoxed[S] = implicit (tpe, raw) =>
      '{ Hooks.UseState.fromJsBoxed[$tpe]($raw) }

    override protected def useStateWithReuseFromJsBoxed[S] = implicit (tpe, raw, reuse, ct) =>
      '{ Hooks.UseStateWithReuse.fromJsBoxed[$tpe]($raw)($reuse, $ct) }

    override protected def vdomRawNode = vdom =>
      '{ $vdom.rawNode }
  }

  // ===================================================================================================================

  def _renderMacro[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]]
      (self: Expr[PrimaryWithRender[P, C, Ctx, Step] with Secondary[Ctx, CtxFn, Step]],
      f    : Expr[CtxFn[VdomNode]],
      step : Expr[Step],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      debug: Boolean,
     )(using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], CtxFn: Type[CtxFn], Step: Type[Step]): Expr[Component[P, CT]] = {

    import quotes.reflect.*

    val hookMacros = HookMacrosImpl(q)
    import hookMacros.{log, HookStep}

    log.enabled = debug // f.show.contains("DEBUG")
    log.header()

    // log("self", self.asTerm.underlying.show)

    val renderStep =
      HookStep("renderRR", Nil, List(List(f.asTerm), List(step.asTerm, s.asTerm)))

    val rewriteAttempt =
      for {
        hookDefn <- hookMacros.parse(self.asTerm.underlying)
        rewriter <- hookMacros.rewriteComponent(hookDefn + renderStep)
      } yield rewriter

    val result: Expr[Component[P, CT]] =
      rewriteAttempt match {

        case Right(rewriter) =>
          type JsProps = Box[P] with facade.PropsWithChildren

          def newBody(props: Expr[JsProps]): Expr[React.Node] = {
            val children = typedValDef[PropsChildren]("children", Flags.EmptyFlags)('{ PropsChildren.fromRawProps($props) })
            val ctx = hookMacros.rewriterCtx(
              props        = '{ $props.unbox }.asTerm,
              initChildren = children.valDef,
              children     = children.ref.asTerm,
            )
            rewriter(ctx)
          }

          '{
            val rawComponent: JsFn.RawComponent[Box[P]] = props => ${newBody('props)}
            ScalaFn.fromBoxed(JsFn.fromJsFn[Box[P], C](rawComponent)($s))
          }

        case Left(err) =>
          report.warning(err())
          '{ $self.render($step.squash($f)(_))($s) }
      }

    log.footer(result.show)
    result
  }

}
