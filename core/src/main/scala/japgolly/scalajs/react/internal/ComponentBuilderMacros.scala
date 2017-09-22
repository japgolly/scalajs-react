package japgolly.scalajs.react.internal

import scala.reflect.macros.blackbox.Context
import japgolly.scalajs.react.Children

final class ComponentBuilderMacros(val c: Context) extends MacroUtils {
  import c.universe._

  private def createBackend[B: c.WeakTypeTag] = {
    val B = weakTypeOf[B]
    val backend = replaceMacroMethod("backend")
    val fn = {
      var args = Vector.empty[Tree]
      val ctor = primaryConstructorParams(B)
      ctor.size match {
        case 0 => ()
        case 1 => args :+= q"x"
        case n => fail(s"Constructor of $B has $n parameters: $ctor.")
      }
      q"new $B(..$args)"
    }
    q"$backend[$B](x => $fn)"
  }

  def backendAndRender[P: c.WeakTypeTag, S: c.WeakTypeTag, B: c.WeakTypeTag]: c.Tree =
    q"${createBackend[B]}.renderBackend"

  def backendAndRenderWithChildren[P: c.WeakTypeTag, S: c.WeakTypeTag, B: c.WeakTypeTag]: c.Tree =
    q"${createBackend[B]}.renderBackendWithChildren"

  private final class RenderParam[T: c.WeakTypeTag](val code: Tree)(_names: String*) {
    val spec    : Type        = weakTypeOf[T]
    val dealised: Type        = spec.dealias
    val names   : Set[String] = _names.toSet
  }

  private def _renderBackend[B: c.WeakTypeTag](allowChildren: Boolean, rps: List[RenderParam[_]]): c.Tree = {
    val B = concreteWeakTypeOf[B]
    val C = if (allowChildren) weakTypeOf[Children.Varargs] else weakTypeOf[Children.None]
    val render = TermName("render")
    def genericRender = replaceMacroMethod("renderWith")

    def assertChildrenTypeMatches(childrenUsed: Boolean): Unit =
      if (childrenUsed != allowChildren)
        fail {
          val pc = "PropsChildren"
          val rb = "renderBackend"
          val rbc = "renderBackendWithChildren"
          if (allowChildren)
            s"Use of $pc not detected. Use $rb instead of $rbc."
          else
            s"Use of $pc detected. Use $rbc instead of $rb."
        }

    def renderWithoutParams(): c.Tree = {
      assertChildrenTypeMatches(false)
      q"$genericRender[$C](_.backend.$render)"
    }

    def renderWithParams(params: List[Symbol]): c.Tree = {

      var childrenUsed = false
      def getPropsChildren = {
        childrenUsed = true
        q"x.propsChildren"
      }

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

      assertChildrenTypeMatches(childrenUsed)
      q"$genericRender[$C](x => x.backend.$render(..$args))"
    }

    val renderMethod = B.member(render) match {
      case NoSymbol                         => fail(s"$B missing a render method.")
      case s if !(s.isMethod && s.isPublic) => fail(s"${s.fullName} must be a public method.")
      case s                                => s.asMethod
    }

    renderMethod.paramLists match {
      case Nil       => renderWithoutParams()
      case ps :: Nil => renderWithParams(ps)
      case _ :: t    => fail(s"${renderMethod.fullName} mustn't have more than one set of parameters. Found: $t")
    }
  }

  private def renderParams[P: c.WeakTypeTag, S: c.WeakTypeTag] =
    new RenderParam[P](q"x.props")("p", "props") ::
    new RenderParam[S](q"x.state")("s", "state") ::
    Nil

  def renderBackend[P: c.WeakTypeTag, S: c.WeakTypeTag, B: c.WeakTypeTag]: c.Tree =
    _renderBackend[B](false, renderParams[P, S])

  def renderBackendWithChildren[P: c.WeakTypeTag, S: c.WeakTypeTag, B: c.WeakTypeTag]: c.Tree =
    _renderBackend[B](true, renderParams[P, S])

//  def renderBackendSP[P: c.WeakTypeTag, Q: c.WeakTypeTag, S: c.WeakTypeTag, B: c.WeakTypeTag]: c.Tree =
//    _renderBackend[B](
//      new RenderParam[P](q"x.props.static") ()             ::
//      new RenderParam[Q](q"x.props.dynamic")("p", "props") ::
//      new RenderParam[S](q"x.state")        ("s", "state") ::
//      Nil)
}
