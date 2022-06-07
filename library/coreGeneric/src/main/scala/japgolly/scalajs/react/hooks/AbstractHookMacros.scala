package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.internal.MacroLogger
import scala.annotation.{nowarn, tailrec}

object AbstractHookMacros {

  trait HookRewriterApi[Tree, TermName] {
    def +=(stmt: Tree): Unit
    def valDef(name: TermName, body: Tree): Unit
    def args(): List[Tree]
    def ctxArg(): Tree
    def hookCount(): Int
    def nextHookName(suffix: String): TermName
    def registerHook(h: TermName): Unit
    def useChildren(): Unit
    def usesChildren(): Boolean
    def wrap(body: Tree): Tree

    final def nextHookName(): TermName = nextHookName("")
  }

  trait HookRewriter[Tree, TermName] extends HookRewriterApi[Tree, TermName] {
    protected var stmts = Vector.empty[Tree]
    private var hooks = List.empty[Tree]
    private var _hookCount = 0
    private var _usesChildren = false

    final override def usesChildren() =
      _usesChildren

    final override def useChildren(): Unit = {
      _usesChildren = true
      this += initChildren
    }

    final override def +=(stmt: Tree): Unit =
      stmts :+= stmt

    final def valDef(name: TermName, body: Tree): Unit =
      this += ValDef(name, body)

    final override def hookCount(): Int =
      _hookCount

    final override def nextHookName(suffix: String = ""): TermName =
      TermName("hook" + (hookCount() + 1) + suffix)

    final override def registerHook(h: TermName): Unit = {
      hooks :+= Ident(h)
      _hookCount += 1
    }

    final override def args(): List[Tree] =
      if (usesChildren())
        props :: propsChildren :: hooks
      else
        props :: hooks

    final override def ctxArg(): Tree = {
      val hookCtxObj = hookCtx(usesChildren())
      val create = Apply(hookCtxObj, args())
      val name = nextHookName("_ctx")
      valDef(name, create)
      Ident(name)
    }

    protected def props: Tree
    protected def initChildren: Tree
    protected def propsChildren: Tree

    protected def Apply(t: Tree, args: List[Tree]): Tree
    protected def hookCtx(withChildren: Boolean): Tree
    protected def Ident(name: TermName): Tree
    protected def TermName(name: String): TermName
    protected def ValDef(name: TermName, body: Tree): Tree
  }
}

@nowarn("msg=unchecked since it is eliminated by erasure|type test cannot be checked at run time")
trait AbstractHookMacros {
  import AbstractHookMacros._

  // ===================================================================================================================
  // Abstractions

  type Tree

  protected type Apply <: Tree
  protected val Apply: ApplyExtractor
  protected abstract class ApplyExtractor {
    def apply(fun: Tree, args: List[Tree]): Apply
    def unapply(apply: Apply): Option[(Tree, List[Tree])]
  }

  protected type TypeApply <: Tree
  protected val TypeApply: TypeApplyExtractor
  protected abstract class TypeApplyExtractor {
    def apply(fun: Tree, args: List[Tree]): TypeApply
    def unapply(typeApply: TypeApply): Option[(Tree, List[Tree])]
  }

  protected type Select <: Tree
  protected val Select: SelectExtractor
  protected abstract class SelectExtractor {
    def apply(qualifier: Tree, name: String): Select
    def unapply(select: Select): Option[(Tree, String)]
  }

  protected type Function <: Tree
  protected val Function: FunctionExtractor
  protected abstract class FunctionExtractor {
    def unapply(function: Function): Option[(List[Tree], Tree)]
  }

  protected def showRaw(t: Tree): String
  protected def showCode(t: Tree): String

  type HookRef

  def rewriter(): Rewriter

  protected def call(function: Tree, args: List[Tree]): Tree

  protected def useStateFn                  (tpe: Tree, body: Tree)                         : Tree
  protected def useStateFromJsBoxed         (tpe: Tree, raw: HookRef)                       : Tree
  protected def useStateWithReuseFromJsBoxed(tpe: Tree, raw: HookRef, reuse: Tree, ct: Tree): Tree
  protected def vdomRawNode                 (vdom: Tree)                                    : Tree

  // ===================================================================================================================
  // Concrete

  type Rewriter = HookRewriterApi[Tree, HookRef]

  implicit val log = MacroLogger()

  case class HookDefn(steps: List[HookStep])

  case class HookStep(name: String, targs: List[Tree], args: List[List[Tree]])

  private val withHooks = "withHooks"

  final def apply(tree: Tree): Either[() => String, Tree] =
    for {
      p <- parse(tree)
      r <- rewriteComponent(p)
    } yield applyRewrite(r)

  final def parse(tree: Tree): Either[() => String, HookDefn] =
    _parse(tree, Nil, Nil, Nil)

  @tailrec
  private def _parse(tree: Tree, targs: List[Tree], args: List[List[Tree]], steps: List[HookStep]): Either[() => String, HookDefn] =
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

  def applyRewrite(rewrite: Rewriter => Tree): Tree = {
    val b    = rewriter()
    val vdom = rewrite(b)
    b.wrap(vdomRawNode(vdom))
  }

  def rewriteComponent(h: HookDefn): Either[() => String, Rewriter => Tree] = {
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

    def by[A](fn: Tree)(use: (Rewriter, Tree) => A): Either[() => String, Rewriter => A] =
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

    def maybeBy[A](f: Tree)(use: (Rewriter, Tree) => A): Either[() => String, Rewriter => A] =
      if (step.name endsWith "By")
        by(f)(use)
      else
        Right(use(_, f))

    step.name match {

      case "useState" | "useStateBy" =>
        val List(tpe) = step.targs : @nowarn
        val List(List(f), _) = step.args : @nowarn
        maybeBy(f) { (b, body) =>
          val rawName = b.nextHookName("_raw")
          val name    = b.nextHookName()
          b.valDef(rawName, useStateFn(tpe, body))
          b.valDef(name, useStateFromJsBoxed(tpe, rawName))
          name
        }

      case "useStateWithReuse" | "useStateWithReuseBy" =>
        val List(tpe) = step.targs : @nowarn
        val List(List(f), List(ct, reuse, _)) = step.args : @nowarn
        maybeBy(f) { (b, body) =>
          val rawName = b.nextHookName("_raw")
          val name    = b.nextHookName()
          b.valDef(rawName, useStateFn(tpe, body))
          b.valDef(name, useStateWithReuseFromJsBoxed(tpe, rawName, reuse, ct))
          name
        }

      case _ =>
        Left(() => s"Inlining of hook method '${step.name}' not yet supported.")
    }
  }

  def rewriteRender(step: HookStep)(implicit log: MacroLogger): Either[() => String, Rewriter => Tree] = {
    log("rewriteRender:" + step.name, step)
    step.name match {

      case "render" =>
        val List(List(renderFn), _) = step.args : @nowarn
        Right(b => call(renderFn, b.args()))

      case _ =>
        Left(() => s"Inlining of hook render method '${step.name}' not yet supported.")
    }
  }

  // private def inlineHookRawComponent[P](rewrite: HookRewriter2 => Tree)(implicit P: c.WeakTypeTag[P]): Tree = {
  //   val b       = new HookRewriter2(q"props.unbox", q"val children = $PropsChildren.fromRawProps(props)", q"children")
  //   val render1 = rewrite(b)
  //   val render2 = b.wrap(q"$render1.rawNode")
  //   q"(props => $render2): $JsFn.RawComponent[${Box(P)}]"
  // }

  // private def inlineHookComponent[P, C <: Children](rawComp: Tree, summoner: c.Tree)(implicit P: c.WeakTypeTag[P], C: c.WeakTypeTag[C]): Tree = {
  //   c.untypecheck(q"""
  //     val rawComponent = $rawComp
  //     $ScalaFn.fromBoxed($JsFn.fromJsFn[${Box(P)}, $C](rawComponent)($summoner))
  //   """)
  // }

}
