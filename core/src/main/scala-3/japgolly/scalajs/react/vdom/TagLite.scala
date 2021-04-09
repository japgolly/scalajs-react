package japgolly.scalajs.react.vdom

import scala.language.`3.0`

trait TagLite[T <: TopNode] {
  final opaque type Tag[+N <: T] = String

  @inline def apply[N <: T](name: String): Tag[N] =
    name

  extension [N <: T](self: Tag[N]) {

    @inline def name: String =
      self

    def apply(xs: TagMod*): TagOf[N] =
      new TagOf(self, xs :: Nil)
  }

  @inline implicit def toTagOf[N <: SvgTopNode](t: SvgTagOf[N]): TagOf[N] =
    new TagOf[N](t.name)
}

// =====================================================================================================================

object HtmlTagOf extends TagLite[HtmlTopNode]
export HtmlTagOf.{Tag => HtmlTagOf}

object SvgTagOf extends TagLite[SvgTopNode]
export SvgTagOf.{Tag => SvgTagOf}
