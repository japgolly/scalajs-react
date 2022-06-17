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

  // -------------------------------------------------------------------------------------------------------------------

  type PrimaryProxy[P, C <: Children, Ctx, Step <: AbstractStep] =
    ApiPrimaryWithRenderMacros[P, C, Ctx, Step]

  type PrimaryApi[P, C <: Children, Ctx, Step <: AbstractStep] =
    PrimaryWithRender[P, C, Ctx, Step]

  // https://github.com/lampepfl/dotty/issues/15357
  inline def render1Workaround[P, C <: Children, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: PrimaryProxy[P, C, Ctx, Step],
      inline f    : Ctx => VdomNode,
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT]): Component[P, CT] =
    ${ render1[P, C, Ctx, Step, CT]('proxy, 'f, 's) }

  inline def renderDebug1Workaround[P, C <: Children, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: PrimaryProxy[P, C, Ctx, Step],
      inline f    : Ctx => VdomNode,
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT]): Component[P, CT] =
    ${ renderDebug1[P, C, Ctx, Step, CT]('proxy, 'f, 's) }

  def render1[P, C <: Children, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[PrimaryProxy[P, C, Ctx, Step]],
      f    : Expr[Ctx => VdomNode],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]])
      (using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], Step: Type[Step]): Expr[Component[P, CT]] =
    _render1(proxy, f, s, false)

  def renderDebug1[P, C <: Children, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[PrimaryProxy[P, C, Ctx, Step]],
      f    : Expr[Ctx => VdomNode],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]])
     (using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], Step: Type[Step]): Expr[Component[P, CT]] =
    _render1(proxy, f, s, true)

  // -------------------------------------------------------------------------------------------------------------------

  // https://github.com/lampepfl/dotty/issues/15357
  inline def renderC1Workaround[P, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: ComponentPCMacros[P],
      inline f    : (P, PropsChildren) => VdomNode,
      inline s    : CtorType.Summoner.Aux[Box[P], Children.Varargs, CT]): Component[P, CT] =
    ${ renderC1[P, CT]('proxy, 'f, 's) }

  inline def renderDebugC1Workaround[P, CT[-p, +u] <: CtorType[p, u]](
      inline proxy: ComponentPCMacros[P],
      inline f    : (P, PropsChildren) => VdomNode,
      inline s    : CtorType.Summoner.Aux[Box[P], Children.Varargs, CT]): Component[P, CT] =
    ${ renderDebugC1[P, CT]('proxy, 'f, 's) }

  def renderC1[P, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[ComponentPCMacros[P]],
      f    : Expr[(P, PropsChildren) => VdomNode],
      s    : Expr[CtorType.Summoner.Aux[Box[P], Children.Varargs, CT]])
      (using q: Quotes, P: Type[P], CT: Type[CT]): Expr[Component[P, CT]] =
    _renderC1(proxy, f, s, false)

  def renderDebugC1[P, CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[ComponentPCMacros[P]],
      f    : Expr[(P, PropsChildren) => VdomNode],
      s    : Expr[CtorType.Summoner.Aux[Box[P], Children.Varargs, CT]])
      (using q: Quotes, P: Type[P], CT: Type[CT]): Expr[Component[P, CT]] =
    _renderC1(proxy, f, s, true)

  // -------------------------------------------------------------------------------------------------------------------

  type SecondaryProxy[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]] =
    ApiSecondaryWithRenderMacros[P, C, Ctx, CtxFn, Step]

  type SecondaryApi[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]] =
    PrimaryWithRender[P, C, Ctx, Step] with Secondary[Ctx, CtxFn, Step]

  // https://github.com/lampepfl/dotty/issues/15357
  inline def render2Workaround[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      inline proxy: SecondaryProxy[P, C, Ctx, CtxFn, Step],
      inline f    : CtxFn[VdomNode],
      inline step : Step,
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT]): Component[P, CT] =
    ${ render2[P, C, Ctx, CtxFn, Step, CT]('proxy, 'f, 'step, 's) }

  inline def renderDebug2Workaround[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      inline proxy: SecondaryProxy[P, C, Ctx, CtxFn, Step],
      inline f    : CtxFn[VdomNode],
      inline step : Step,
      inline s    : CtorType.Summoner.Aux[Box[P], C, CT]): Component[P, CT] =
    ${ renderDebug2[P, C, Ctx, CtxFn, Step, CT]('proxy, 'f, 'step, 's) }

  def render2[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[SecondaryProxy[P, C, Ctx, CtxFn, Step]],
      f    : Expr[CtxFn[VdomNode]],
      step : Expr[Step],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]])
      (using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], CtxFn: Type[CtxFn], Step: Type[Step]): Expr[Component[P, CT]] =
    _render2(proxy, f, step, s, false)

  def renderDebug2[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      proxy: Expr[SecondaryProxy[P, C, Ctx, CtxFn, Step]],
      f    : Expr[CtxFn[VdomNode]],
      step : Expr[Step],
      s    : Expr[CtorType.Summoner.Aux[Box[P], C, CT]])
      (using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], CtxFn: Type[CtxFn], Step: Type[Step]): Expr[Component[P, CT]] =
    _render2(proxy, f, step, s, true)

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

    override protected def asTerm    [A] = _.asTerm
    override protected def Expr      [A] = _.asExprOf[Any].asInstanceOf[Expr[A]]
    override protected def refToTerm     = _.ref
    override protected def showCode      = _.show(using q.reflect.Printer.TreeShortCode)
    override protected def showRaw       = _.show(using q.reflect.Printer.TreeStructure)
    override protected def typeOfTerm    = _.tpe.asTypeTree
    override protected def uninline      = _uninline
    override protected def unitTerm      = '{ () }
    override protected def unitType      = scala.quoted.Type.of[Unit]
    override protected def wrap          = (s, b) => Block(s.toList, b)

    // TODO: Move into microlibs (and make tailrec and non-recursive versions)
    @tailrec
    private def _uninline(t: Term): Term =
      t match {
        case Inlined(_, _, e) => _uninline(e)
        case _                => t
      }

    override protected def call = (function, args) => {
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

    override protected def isUnit =
      _.tpe.asType match {
        case '[Unit] => true
        case _       => false
      }

    override protected def Type[A] = t => {
      val x: scala.quoted.Type[?] = t.asType
      x.asInstanceOf[scala.quoted.Type[A]]
    }

    override protected val rewriterBridge: RewriterBridge =
      HookRewriter.Bridge[Stmt, Term, Ref](
        apply     = Apply(_, _),
        hookCtx   = (usesChildren, args) => {
                      if (usesChildren) {
                        // .patch below removes the PropsChildren arg
                        val targs = args.patch(1, Nil, 1).map(_.tpe)
                        Select.overloaded('{HookCtx.withChildren}.asTerm, "apply", targs, args)
                      } else
                        Select.overloaded('{HookCtx}.asTerm, "apply", args.map(_.tpe), args)
                    },
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
      @tailrec
      override def unapply(function: Term) = function match {

        case Apply(TypeApply(f, args), _) if f.tpe.show.startsWith("scala.scalajs.js.Any.toFunction") =>
          Some(args.size - 1)

        case Block(List(DefDef(_, List(TermParamClause(params)), _, Some(_))), Closure(Ident(_), _)) =>
          Some(params.size)

        case Block(Nil, expr) =>
          unapply(expr)

        case Block(stmts1, Block(stmts2, expr)) =>
          val stmts = if (stmts2.isEmpty) stmts1 else stmts1 ::: stmts2
          unapply(Block(stmts, expr))

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

  import AbstractHookMacros.HookStep

  def _render1[P, C <: Children, Ctx, Step <: AbstractStep, CT[-p, +u] <: CtorType[p, u]](
      proxy   : Expr[PrimaryProxy[P, C, Ctx, Step]],
      renderFn: Expr[Ctx => VdomNode],
      summoner: Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      debug   : Boolean)
      (using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], Step: Type[Step]): Expr[Component[P, CT]] = {

    import quotes.reflect.*

    val self = proxy.asInstanceOf[Expr[PrimaryApi[P, C, Ctx, Step]]]

    _render[P, C, CT](
      macroApplication = self.asTerm,
      summoner         = summoner,
      debug            = debug,
      renderStep       = HookStep("renderRR", Nil, List(List(renderFn.asTerm), List(summoner.asTerm))),
      giveUp           = () => '{ $self.render($renderFn)($summoner) },
    )
  }

  def _renderC1[P, CT[-p, +u] <: CtorType[p, u]](
      proxy   : Expr[ComponentPCMacros[P]],
      renderFn: Expr[(P, PropsChildren) => VdomNode],
      summoner: Expr[CtorType.Summoner.Aux[Box[P], Children.Varargs, CT]],
      debug   : Boolean)
      (using q: Quotes, P: Type[P], CT: Type[CT]): Expr[Component[P, CT]] = {

    import quotes.reflect.*

    val self = proxy.asInstanceOf[Expr[HookComponentBuilder.ComponentPC.First[P]]]

    _render[P, Children.Varargs, CT](
      macroApplication = self.asTerm,
      summoner         = summoner,
      debug            = debug,
      renderStep       = HookStep("renderRR", Nil, List(List(renderFn.asTerm), List(summoner.asTerm))),
      giveUp           = () => '{ $self.render($renderFn)($summoner) },
    )
  }

  def _render2[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn], CT[-p, +u] <: CtorType[p, u]](
      proxy   : Expr[SecondaryProxy[P, C, Ctx, CtxFn, Step]],
      renderFn: Expr[CtxFn[VdomNode]],
      step    : Expr[Step],
      summoner: Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      debug   : Boolean)
      (using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT], Ctx: Type[Ctx], CtxFn: Type[CtxFn], Step: Type[Step]): Expr[Component[P, CT]] = {

    import quotes.reflect.*

    val self = proxy.asInstanceOf[Expr[SecondaryApi[P, C, Ctx, CtxFn, Step]]]

    _render[P, C, CT](
      macroApplication = self.asTerm,
      summoner         = summoner,
      debug            = debug,
      renderStep       = HookStep("renderRR", Nil, List(List(renderFn.asTerm), List(step.asTerm, summoner.asTerm))),
      giveUp           = () => '{ $self.render($step.squash($renderFn)(_))($summoner) },
    )
  }

  // ===================================================================================================================

  private def _render[P, C <: Children, CT[-p, +u] <: CtorType[p, u]](
      using q: Quotes, P: Type[P], C: Type[C], CT: Type[CT])(
      macroApplication: q.reflect.Term,
      summoner        : Expr[CtorType.Summoner.Aux[Box[P], C, CT]],
      debug           : Boolean,
      renderStep      : HookStep[q.reflect.Term, q.reflect.TypeTree],
      giveUp          : () => Expr[Component[P, CT]]): Expr[Component[P, CT]] = {

    import quotes.reflect.*

    val hookMacros = HookMacrosImpl(q)

    import hookMacros.log
    log.enabled = debug
    log.header()
    // log("self", macroApplication.show)

    def onFailure(msg: String): Expr[Component[P, CT]] = {
      import Console._
      log(RED_B + WHITE + "Giving up. " + msg + RESET)
      report.warning(msg)
      giveUp()
    }

    val rewriteAttempt =
      for {
        hookDefn <- hookMacros.parse(macroApplication)
        rewriter <- hookMacros.rewriteComponent(hookDefn + renderStep)
      } yield rewriter

    val result: Expr[Component[P, CT]] =
      try
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
              ScalaFn.fromBoxed(JsFn.fromJsFn[Box[P], C](rawComponent)($summoner))
            }

          case Left(err) =>
            onFailure(err())
        }
      catch {
        case err: Throwable =>
          // err.printStackTrace()
          onFailure(err.getMessage())
      }

    log.footer(result.show)
    result
  }

}
