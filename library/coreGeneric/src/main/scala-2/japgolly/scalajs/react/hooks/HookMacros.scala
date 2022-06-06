package japgolly.scalajs.react.hooks

import japgolly.microlibs.compiletime.MacroUtils
import japgolly.scalajs.react.component.ScalaFn.Component
import japgolly.scalajs.react.hooks.Api._
import japgolly.scalajs.react.internal.{Box, MacroLogger}
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, CtorType}
import scala.annotation.{nowarn, tailrec}
import scala.reflect.macros.blackbox.Context

object HookMacros {

  trait ApiSecondaryWithRenderMacros[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]] {
      self: PrimaryWithRender[P, C, Ctx, Step] with Secondary[Ctx, CtxFn, Step] =>

    final def render(f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], C]): Component[P, s.CT] =
      // Without macros: render(step.squash(f)(_))
      macro HookMacros.render[P, C, Ctx, CtxFn, Step]
  }
}

// =====================================================================================================================

class HookMacros(val c: Context) extends MacroUtils {
  import c.universe._

  private implicit def autoTagToType[A](t: c.WeakTypeTag[A]): Type = t.tpe

  private def Box          : Tree = q"_root_.japgolly.scalajs.react.internal.Box"
  private def Box(t: Type) : Type = appliedType(c.typeOf[Box[_]], t)
  private def HookCtx      : Tree = q"_root_.japgolly.scalajs.react.hooks.HookCtx"
  private def Hooks        : Tree = q"_root_.japgolly.scalajs.react.hooks.Hooks"
  private def JsFn         : Tree = q"_root_.japgolly.scalajs.react.component.JsFn"
  private def PropsChildren: Tree = q"_root_.japgolly.scalajs.react.PropsChildren"
  private def React        : Tree = q"_root_.japgolly.scalajs.react.facade.React"
  private def ScalaFn      : Tree = q"_root_.japgolly.scalajs.react.component.ScalaFn"
  private def withHooks           = "withHooks"

  private case class HookDefn(steps: List[HookStep])

  private case class HookStep(name: String, targs: List[Tree], args: List[List[Tree]])

  private class HookRewriter(props: Tree, initChildren: Tree, propsChildren: Tree) {
    private var stmts = Vector.empty[Tree]
    private var hooks = List.empty[Ident]
    private var _hookCount = 0
    private var _usesChildren = false

    def usesChildren() =
      _usesChildren

    def useChildren(): Unit = {
      _usesChildren = true
      this += initChildren
    }

    def +=(stmt: Tree): Unit =
      stmts :+= stmt

    def hookCount(): Int =
      _hookCount

    def nextHookName(suffix: String = ""): TermName =
      TermName("hook" + (hookCount() + 1) + suffix)

    def registerHook(h: TermName): Unit = {
      hooks :+= Ident(h)
      _hookCount += 1
    }

    def args(): List[Tree] =
      if (usesChildren())
        props :: propsChildren :: hooks
      else
        props :: hooks

    def ctxArg(): Tree = {
      val hookCtxObj = if (usesChildren()) q"$HookCtx.withChildren" else HookCtx
      val create = Apply(hookCtxObj, args())
      val name = nextHookName("_ctx")
      this += q"val $name = $create"
      Ident(name)
    }

    def wrap(body: Tree): Tree =
      q"..$stmts; $body"
  }

  // -------------------------------------------------------------------------------------------------------------------

  def render[P, C <: Children, Ctx, CtxFn[_], Step <: SubsequentStep[Ctx, CtxFn]]
            (f: c.Tree)(step: c.Tree, s: c.Tree)
            (implicit P: c.WeakTypeTag[P], C: c.WeakTypeTag[C]): c.Tree = {

    implicit val log = MacroLogger()
    log.enabled = showCode(c.macroApplication).contains("DEBUG") // TODO: DELETE
    log.header()
    log("macroApplication", showRaw(c.macroApplication))

    val self = c.prefix

    val parsed = parseHookDefn(c.macroApplication, Nil, Nil, Nil)

    val inlined = parsed
      .flatMap(inlineHookDefn)
      .map(inlineHookRawComponent[P])
      .map(inlineHookComponent[P, C](_, s))

    val result: Tree =
      inlined match {
        case Right(r) =>
          r
        case Left(e) =>
          log(e())
          q"""
            val f = $step.squash($f)
            $self.render(f)($s)
          """
    }

    log.footer(showCode(result))
    result
  }

  @tailrec
  private def parseHookDefn(tree: Tree, targs: List[Tree], args: List[List[Tree]], steps: List[HookStep])
                           (implicit log: MacroLogger): Either[() => String, HookDefn] =
      tree match {

        case Apply(t, a) =>
           parseHookDefn(t, targs, a :: args, steps)

        case TypeApply(t, a) =>
          if (targs.isEmpty)
            parseHookDefn(t, a, args, steps)
          else
            Left(() => "Multiple type arg clauses found at " + showRaw(tree))

        case Select(t, n) =>
          val name = n.toString
          if (name == withHooks) {
            if (args.nonEmpty)
              Left(() => s"$withHooks called with args when none exepcted: ${args.map(_.map(showCode(_)))}")
            else
              Right(HookDefn(steps))
          } else {
            val step = HookStep(name, targs, args)
            log(s"Found step '$name'", step)
            parseHookDefn(t, Nil, Nil, step :: steps)
          }

        case _ =>
          Left(() => "Don't know how to parse " + showRaw(tree))
      }

  private def inlineHookDefn(h: HookDefn)(implicit log: MacroLogger): Either[() => String, HookRewriter => Tree] = {
    val it = h.steps.iterator
    var renderStep: HookStep = null
    var hooks = Vector.empty[HookRewriter => TermName]
    var withPropsChildren = false
    while (it.hasNext) {
      val step = it.next()
      if (it.hasNext) {
        if (hooks.isEmpty && step.name == "withPropsChildren")
          withPropsChildren = true
        else
          inlineHookStep(step) match {
            case Right(h) => hooks :+= h
            case Left(e)  => return Left(e)
          }
      } else
        renderStep = step
    }

    hookRenderInliner(renderStep).map { buildRender => b =>
      if (withPropsChildren)
        b.useChildren()
      for (h <- hooks)
        b registerHook h(b)
      buildRender(b)
    }
  }

  private def inlineHookStep(step: HookStep)(implicit log: MacroLogger): Either[() => String, HookRewriter => TermName] = {
    log("inlineHookStep." + step.name, step)

    def useState(b: HookRewriter, tpe: Tree, body: Tree) = {
      val rawName = b.nextHookName("_raw")
      val name    = b.nextHookName()
      b += q"val $rawName = $React.useStateFn(() => $Box[$tpe]($body))"
      b += q"val $name = $Hooks.UseState.fromJsBoxed[$tpe]($rawName)"
      name
    }

    step.name match {

      case "useState" =>
        val targ = step.targs.head
        val arg  = step.args.head.head
        Right(useState(_, targ, arg))

      case "useStateBy" =>
        val targ = step.targs.head
        val arg  = step.args.head.head
        arg match {
          case f@ Function(params, _) =>
            if (params.sizeIs == 1)
              Right { b =>
                val ctxArg = b.ctxArg()
                useState(b, targ, call(f, ctxArg :: Nil))
              }
            else
              Right(b => useState(b, targ, call(f, b.args())))

          case _ =>
            Left(() => s"Expected a function, found: ${showRaw(arg)}")
        }

      case _ =>
        Left(() => s"Inlining of hook method '${step.name}' not yet supported.")
    }
  }

  private def hookRenderInliner(step: HookStep)(implicit log: MacroLogger): Either[() => String, HookRewriter => Tree] = {
    log("inlineHookRender." + step.name, step)
    step.name match {
      case "render" =>
        @nowarn("msg=exhaustive") val List(List(renderFn), _) = step.args
        Right(b => call(renderFn, b.args()))

      case _ =>
        Left(() => s"Inlining of hook render method '${step.name}' not yet supported.")
    }
  }

  private def inlineHookRawComponent[P](rewrite: HookRewriter => Tree)(implicit P: c.WeakTypeTag[P]): Tree = {
    val b       = new HookRewriter(q"props.unbox", q"val children = $PropsChildren.fromRawProps(props)", q"children")
    val render1 = rewrite(b)
    val render2 = b.wrap(q"$render1.rawNode")
    q"(props => $render2): $JsFn.RawComponent[${Box(P)}]"
  }

  private def inlineHookComponent[P, C <: Children](rawComp: Tree, summoner: c.Tree)(implicit P: c.WeakTypeTag[P], C: c.WeakTypeTag[C]): Tree = {
    c.untypecheck(q"""
      val rawComponent = $rawComp
      $ScalaFn.fromBoxed($JsFn.fromJsFn[${Box(P)}, $C](rawComponent)($summoner))
    """)
  }

  // -------------------------------------------------------------------------------------------------------------------

  private def call(function: Tree, args: List[Tree]): Tree = {
    import internal._

    function match {
      case Function(params, body) =>

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
}
