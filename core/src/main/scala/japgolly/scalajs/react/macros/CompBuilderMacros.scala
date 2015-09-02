package japgolly.scalajs.react.macros

import scala.reflect.macros.blackbox.Context

class CompBuilderMacros (val c: Context) extends ReactMacroUtils {
  import c.universe._

  def backendAndRender[P: c.WeakTypeTag, S: c.WeakTypeTag, B: c.WeakTypeTag]: c.Tree = {
    val B = weakTypeOf[B]
    val backend = replaceMacroMethod("backend")
    q"$backend[$B](x => new $B(x)).renderBackend"
  }

  def renderBackend[P: c.WeakTypeTag, S: c.WeakTypeTag, B: c.WeakTypeTag]: c.Tree = {
    val B = concreteWeakTypeOf[B]
    val render = TermName("render")
    def build = replaceMacroMethod("render")

    def renderWithParams(params: List[Symbol]): Tree = {
      val P = weakTypeOf[P]
      val S = weakTypeOf[S]
      val Pd = P.dealias
      val Sd = S.dealias
      def getProps         = q"x.props"
      def getState         = q"x.state"
      def getPropsChildren = q"x.propsChildren"
      def tryB(p: Boolean, s: Boolean): Option[Tree] =
        if (p == s)
          None
        else if (p)
          Some(getProps)
        else
          Some(getState)
      def tryPS[A](p: A, s: A)(t: A => Boolean) =
        tryB(t(p), t(s))
      def isPropsChildren(p: Type)  = if (p.toString == "japgolly.scalajs.react.PropsChildren") Some(getPropsChildren) else None
      def byExactTypeAlias(p: Type) = tryPS(P, S)(p == _)
      def byExactType(p: Type)      = tryPS(Pd, Sd)(p == _)
      def bySubType(p: Type)        = tryPS(Pd, Sd)(_ <:< p)
      def byName(p: Symbol)         = tryPS(Set("p", "props"), Set("s", "state"))(_ contains p.name.toString)

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
      q"$build(x => x.backend.$render(..$args))"
    }

    val m = B.member(render) match {
      case NoSymbol                         => fail(s"$B missing a render method.")
      case s if !(s.isMethod && s.isPublic) => fail(s"${s.fullName} must be a public method.")
      case s                                => s.asMethod
    }

    m.paramLists match {
      case Nil       => q"$build(_.backend.$render)"
      case ps :: Nil => renderWithParams(ps)
      case _ :: t    => fail(s"${m.fullName} mustn't have more than one set of parameters. Found: $t")
    }
  }
}
