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

  // trait ApiSecondaryWithRenderMacros200[P, C <: Children, Ctx, CtxFn[_], _Step <: SubsequentStep[Ctx, CtxFn]]   {
  //     self: PrimaryWithRender[P, C, Ctx, _Step] with Secondary[Ctx, CtxFn, _Step] =>

  //   def render(f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
  //     render(step.squash(f)(_))
  // }

  trait ApiSecondaryWithRenderMacros[P, C <: Children, Ctx, CtxFn[_], _Step <: SubsequentStep[Ctx, CtxFn]]
  // extends ApiSecondaryWithRenderMacros200[P, C, Ctx, CtxFn, _Step]
  {
      self: PrimaryWithRender[P, C, Ctx, _Step] with Secondary[Ctx, CtxFn, _Step] =>

    inline final def render(inline f: CtxFn[VdomNode])(implicit inline step: Step, inline s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
      // Without macros: render(step.squash(f)(_))
      renderWorkaround[P, C, Ctx, CtxFn, Step, s.CT](this, f, step, s)
  }

  // ===================================================================================================================

  private def HookMacrosImpl(qq: Quotes): HookMacrosImpl { val q: qq.type } =
    new HookMacrosImpl {
      override implicit val q: qq.type = qq
      import q.reflect.*

      override def rewriter(rctx: RewriterCtx): Rewriter =
        new AbstractHookMacros.HookRewriter[Stmt, Term, HookRef] {
          override protected val ctx                            = rctx
          override protected def Apply(t: Term, as: List[Term]) = q.reflect.Apply(t, as)
          override protected def hookRefToTerm(r: HookRef)      = r.ref
          override def valDef(n: String, t: Term)               = { val r = untypedValDef(n, t.tpe, Flags.EmptyFlags)(t); this += r.valDef; r }
          override def wrap(body: Term)                         = Block(stmts.toList, body)

          override protected def hookCtx(withChildren: Boolean, args: List[Term]) = {
            val obj = if (withChildren) '{HookCtx.withChildren} else '{HookCtx}
            Select.overloaded(obj.asTerm, "apply", args.map(_.tpe), args)
          }
        }
    }

  @nowarn("msg=Consider using canonical type reference .+Underlying instead")
  private trait HookMacrosImpl extends AbstractHookMacros {
    import AbstractHookMacros._

    implicit val q: Quotes
    import q.reflect.*

    override type Expr[A]  = scala.quoted.Expr[A]
    override type HookRef  = UntypedValDef.WithQuotes[q.type]
    override type Stmt     = q.reflect.Statement
    override type Term     = q.reflect.Term
    override type Type[A]  = scala.quoted.Type[A]
    override type TypeTree = q.reflect.TypeTree

    override protected def asTerm[A](e: Expr[A]) = e.asTerm
    override protected def Expr[A](t: Term) = t.asExprOf[Any].asInstanceOf[Expr[A]]
    override protected def hookRefToTerm(r: HookRef) = r.ref
    override protected def Type[A](t: TypeTree) = {
      val x: scala.quoted.Type[?] = t.asType
      x.asInstanceOf[scala.quoted.Type[A]]
    }

    override val ApplyLike = new ApplyExtractor {
      override def unapply(a: Term) = a match {
        case Apply(x, y) => Some((x, y))
        case _ => None
      }
    }

    override val TypeApplyLike = new TypeApplyExtractor {
      override def unapply(a: Term) =  a match {
        case TypeApply(x, y) => Some((x, y))
        case _ => None
      }
    }

    override val SelectLike = new SelectExtractor {
      override def unapply(a: Term) =  a match {
        case Select(x, y) => Some((x, y))
        case _ => None
      }
    }

    override val FunctionLike = new FunctionExtractor {
      override def unapply(function: Term) = function match {
        case Block(List(DefDef(_, List(TermParamClause(params)), _, Some(_))), Closure(Ident(_), _)) =>
          Some(params.size)
        case _ =>
          None
      }
    }

    override def showRaw(t: Term): String = t.show(using q.reflect.Printer.TreeStructure)
    override def showCode(t: Term): String = t.show(using q.reflect.Printer.TreeShortCode)

    @tailrec
    private def extractFunction(term: Term): Term =
      term match {
        case Inlined(None, Nil, f) => extractFunction(f)
        case Block(Nil, expr)      => extractFunction(expr)
        case f                     => f
      }

    override def call(function: Term, args: List[Term]): Term = {
      val f = extractFunction(function)
      val t = Apply(Select.unique(f, "apply"), args)
      Term.betaReduce(t).getOrElse(t)
    }

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

  // https://github.com/lampepfl/dotty/issues/15357
  inline def renderWorkaround[
        P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]
      ](inline self: PrimaryWithRender[P, C, Ctx, Step] with Secondary[Ctx, CtxFn, Step],
        inline f    : CtxFn[VdomNode],
        inline step : Step,
        inline s    : CtorType.Summoner.Aux[Box[P], C, CT],
       ): Component[P, CT] =
    ${ renderMacro[P, C, Ctx, CtxFn, Step, CT]('self, 'f, 'step, 's) }

  def renderMacro[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]]
      (self: Expr[PrimaryWithRender[P, C, Ctx, Step] with Secondary[Ctx, CtxFn, Step]],
      f    : Expr[CtxFn[VdomNode]],
      step : Expr[Step],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
     )(using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], CtxFn: Type[CtxFn], Step: Type[Step]): Expr[Component[P, CT]] = {

    import quotes.reflect.*

    val hookMacros = HookMacrosImpl(q)
    import hookMacros.{log, HookStep}

    log.enabled = false // f.show.contains("DEBUG")
    log.header()

    val renderStep =
      HookStep("render", Nil, List(List(f.asTerm), List(step.asTerm, s.asTerm)))

    val rewriteAttempt =
      for {
        p <- hookMacros.parse(self.asTerm.underlying)
        r <- hookMacros.rewriteComponent(p + renderStep)
      } yield hookMacros.applyRewrite(r)

    val result: Expr[Component[P, CT]] =
      rewriteAttempt match {

        case Right(rewrite) =>
          type JsProps = Box[P] with facade.PropsWithChildren

          def newBody(props: Expr[JsProps]): Expr[React.Node] = {
            val children = typedValDef[PropsChildren]("children", Flags.EmptyFlags)('{ PropsChildren.fromRawProps($props) })
            val ctx = hookMacros.rewriterCtx(
              props        = '{ $props.unbox }.asTerm,
              initChildren = children.valDef,
              children     = children.ref.asTerm,
            )
            rewrite(ctx)
          }

          '{
            val rawComponent: JsFn.RawComponent[Box[P]] = props => ${newBody('props)}
            ScalaFn.fromBoxed(JsFn.fromJsFn[Box[P], C](rawComponent)($s))
          }

        case Left(err) =>
          log("Rewrite failure", err()) // TODO: make a proper warning?
          '{ $self.render($step.squash($f)(_))($s) }
      }

    log.footer(result.show)
    result
  }

}
