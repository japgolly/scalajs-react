package japgolly.scalajs.react.macros

import scala.reflect.macros.blackbox.Context
import japgolly.scalajs.react.ChildrenArg
import japgolly.scalajs.react.ScalaComponentB.RenderFn

// TODO AnyVal
sealed trait RenderBackend[P, S, B] {
  type C <: ChildrenArg
  val renderFn: RenderFn[P, S, B]
}

object RenderBackend {
  type Aux[P, CC <: ChildrenArg, S, B] =
    RenderBackend[P, S, B] { type C = CC }

  def apply[P, CC <: ChildrenArg, S, B](f: RenderFn[P, S, B]): Aux[P, CC, S, B] =
    new RenderBackend[P, S, B] {
      override type C = CC
      override val renderFn = f
    }

  implicit def materializeOmg[P, C <: ChildrenArg, S, B]: RenderBackend.Aux[P, C, S, B] =
    macro CompBuilderMacros.renderBackend[P, S, B]
}
/*
case class RenderBackend[P, C <: ChildrenArg, S, B](renderFn: RenderFn[P, S, B])
object RenderBackend {
  implicit def materializeIso[P, C <: ChildrenArg, S, B]: RenderBackend[P, C, S, B] =
    macro CompBuilderMacros.renderBackend[P, C, S, B]
}
*/


final class CompBuilderMacros (val c: Context) extends ReactMacroUtils {
  import c.universe._

//  def backendAndRender[P: c.WeakTypeTag, S: c.WeakTypeTag, B: c.WeakTypeTag]: c.Tree = {
//    val B = weakTypeOf[B]
//    val backend = replaceMacroMethod("backend")
//    q"$backend[$B](x => new $B(x)).renderBackend"
//  }

  private final class RenderParam[T: c.WeakTypeTag](val code: Tree)(_names: String*) {
    val spec    : Type        = weakTypeOf[T]
    val dealised: Type        = spec.dealias
    val names   : Set[String] = _names.toSet
  }

//  private def _renderBackend[P: c.WeakTypeTag, C <: ChildrenArg: c.WeakTypeTag, S: c.WeakTypeTag, B: c.WeakTypeTag](rps: List[RenderParam[_]]): c.Expr[RenderBackend[P,C,S,B]] = {
//  private def _renderBackend[P: c.WeakTypeTag, S: c.WeakTypeTag, B: c.WeakTypeTag](rps: List[RenderParam[_]]): c.Expr[RenderBackend[P,S,B]] = {
  private def _renderBackend[P: c.WeakTypeTag, S: c.WeakTypeTag, B: c.WeakTypeTag](rps: List[RenderParam[_]]): c.Tree = {
    println("ah!!!!!!!!!! 1")

    val P = weakTypeOf[P]
    val S = weakTypeOf[S]

    println("ah!!!!!!!!!! 2")

    val B = concreteWeakTypeOf[B]
    println("ah!!!!!!!!!! 3")
    val render = TermName("render")
//    def callRender = replaceMacroCallWith("render")
    println("ah!!!!!!!!!! 4")

    val C0 = weakTypeOf[ChildrenArg.None]

    println("ah!!!!!!!!!! 5a")
    val CN = weakTypeOf[ChildrenArg.Varargs]

    println("ah!!!!!!!!!! 5b")

//    def renderWithParams(params: List[Symbol]): c.Expr[RenderBackend[P,S,B]] = {
    def renderWithParams(params: List[Symbol]): c.Tree = {
      def getPropsChildren = q"x.propsChildren"

      def attempt(test: RenderParam[_] => Boolean): Option[Tree] = {
        val it = rps.iterator.filter(test)
        if (it.isEmpty)
          None
        else {
          val rp = it.next()
          if (it.nonEmpty)
            None // Avoid ambiguity
          else
            Some(rp.code)
        }
      }

      def isPropsChildren (t: Type)   = if (t.toString == "japgolly.scalajs.react.PropsChildren") Some(getPropsChildren) else None
      def byExactTypeAlias(t: Type)   = attempt(_.spec == t)
      def byExactType     (t: Type)   = attempt(_.dealised == t)
      def bySubType       (t: Type)   = attempt(_.dealised <:< t)
      def byName          (s: Symbol) = attempt(_.names contains s.name.toString)

      var args = Vector.empty[Tree]
      for (p <- params) {
        val pt = p.info
        val ptd = pt.dealias
        args :+= (
          isPropsChildren(pt)  orElse
          byExactTypeAlias(pt) orElse
          byExactType(ptd)     orElse
          bySubType(ptd)       orElse
          byName(p)            getOrElse
          fail(s"Don't know what to feed (${p.name}: ${p.info}) in $B.$render."))
      }
      val containsC = true
      val C = if (containsC) CN else C0
//      c.Expr[RenderBackend[P,S,B]](q"japgolly.scalajs.react.macros.RenderBackend[$P,$C,$S,$B](x => x.backend.$render(..$args))")
      q"japgolly.scalajs.react.macros.RenderBackend[$P,$C,$S,$B](x => x.backend.$render(..$args))"
    }

    val renderMethod = B.member(render) match {
      case NoSymbol                         => fail(s"$B missing a render method.")
      case s if !(s.isMethod && s.isPublic) => fail(s"${s.fullName} must be a public method.")
      case s                                => s.asMethod
    }

    val x =
    renderMethod.paramLists match {
      case Nil       =>
        println("aaaaaaaaaaaahhhhhhhhhhhhhhhhhhh ----------- 0")
//        c.Expr[RenderBackend[P,C,S,B]](q"japgolly.scalajs.react.macros.RenderBackend[$P,$C0,$S,$B](_.backend.$render)")
//        c.Expr[RenderBackend[P,S,B]](q"japgolly.scalajs.react.macros.RenderBackend[$P,$C0,$S,$B](_.backend.$render)")
        q"japgolly.scalajs.react.macros.RenderBackend[$P,$C0,$S,$B](_.backend.$render)"
      case ps :: Nil => renderWithParams(ps)
      case _ :: t    => fail(s"${renderMethod.fullName} mustn't have more than one set of parameters. Found: $t")
    }

    println()
    println(x)
    println()
    println(showRaw(x))
    println()
    x
  }

//  def renderBackend[P: c.WeakTypeTag, C <: ChildrenArg: c.WeakTypeTag, S: c.WeakTypeTag, B: c.WeakTypeTag]: c.Expr[RenderBackend[P,C,S,B]] =
//    _renderBackend[P, C, S, B](
//  def renderBackend[P: c.WeakTypeTag, S: c.WeakTypeTag, B: c.WeakTypeTag]: c.Expr[RenderBackend[P,S,B]] =
  def renderBackend[P: c.WeakTypeTag, S: c.WeakTypeTag, B: c.WeakTypeTag]: c.Tree =
    _renderBackend[P, S, B](
      new RenderParam[P](q"x.props")("p", "props") ::
      new RenderParam[S](q"x.state")("s", "state") ::
      Nil)

//  def renderBackendSP[P: c.WeakTypeTag, Q: c.WeakTypeTag, S: c.WeakTypeTag, B: c.WeakTypeTag]: c.Tree =
//    _renderBackend[B](
//      new RenderParam[P](q"x.props.static") ()             ::
//      new RenderParam[Q](q"x.props.dynamic")("p", "props") ::
//      new RenderParam[S](q"x.state")        ("s", "state") ::
//      Nil)
}
