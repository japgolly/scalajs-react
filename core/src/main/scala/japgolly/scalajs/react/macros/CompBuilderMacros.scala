package japgolly.scalajs.react.macros

import scala.reflect.macros.blackbox.Context

final class CompBuilderMacros (val c: Context) extends ReactMacroUtils {
  import c.universe._

  def backendAndRender[P: c.WeakTypeTag, S: c.WeakTypeTag, B: c.WeakTypeTag]: c.Tree = {
    val B = weakTypeOf[B]
    val backend = replaceMacroMethod("backend")
    q"$backend[$B](x => new $B(x)).renderBackend"
  }

  final class RenderParam[T: c.WeakTypeTag](val code: Tree)(_names: String*) {
    val spec    : Type        = weakTypeOf[T]
    val dealised: Type        = spec.dealias
    val names   : Set[String] = _names.toSet
  }

  private def _renderBackend[B: c.WeakTypeTag](rps: List[RenderParam[_]]): c.Tree = {
    val B = concreteWeakTypeOf[B]
    val render = TermName("render")
    def build = replaceMacroMethod("render")

    def renderWithParams(params: List[Symbol]): Tree = {
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

  def renderBackend[P: c.WeakTypeTag, S: c.WeakTypeTag, B: c.WeakTypeTag]: c.Tree =
    _renderBackend[B](
      new RenderParam[P](q"x.props")("p", "props") ::
      new RenderParam[S](q"x.state")("s", "state") ::
      Nil)

  def renderBackendSP[P: c.WeakTypeTag, Q: c.WeakTypeTag, S: c.WeakTypeTag, B: c.WeakTypeTag]: c.Tree =
    _renderBackend[B](
      new RenderParam[P](q"x.props.static") ()             ::
      new RenderParam[Q](q"x.props.dynamic")("p", "props") ::
      new RenderParam[S](q"x.state")        ("s", "state") ::
      Nil)
}
