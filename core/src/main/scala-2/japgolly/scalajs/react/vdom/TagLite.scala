package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{Ref, raw => Raw}
import scala.scalajs.js

final case class HtmlTagOf[+N <: HtmlTopNode](name: String) extends AnyVal {
  def apply(xs: TagMod*): TagOf[N] =
    new TagOf(name, xs :: Nil)
}

object HtmlTagOf {
  implicit def autoToTag[N <: HtmlTopNode](t: HtmlTagOf[N]): TagOf[N] =
    new TagOf[N](t.name, Nil)
}

// =====================================================================================================================

final case class SvgTagOf[+N <: SvgTopNode](name: String) extends AnyVal {
  def apply(xs: TagMod*): TagOf[N] =
    new TagOf(name, xs :: Nil)
}

object SvgTagOf {
  implicit def autoToTag[N <: SvgTopNode](t: SvgTagOf[N]): TagOf[N] =
    new TagOf[N](t.name, Nil)
}

