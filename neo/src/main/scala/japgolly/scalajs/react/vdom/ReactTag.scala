package japgolly.scalajs.react.vdom

import org.scalajs.dom

class ReactTagOf[+N <: TopNode] private[vdom](final val tag: String,
                                              final val modifiers: List[Seq[TagMod]],
                                              final val namespace: Namespace)
// extends DomFrag
{

  def apply(xs: TagMod*): ReactTagOf[N] =
    copy(modifiers = xs :: modifiers)

  def copy(tag: String = this.tag,
           modifiers: List[Seq[TagMod]] = this.modifiers,
           namespace: Namespace = this.namespace): ReactTagOf[N] =
    new ReactTagOf(tag, modifiers, namespace)

  /**
    * Walks the [[modifiers]] to apply them to a particular [[Builder]].
    * Super sketchy/procedural for max performance.
    */
  private[this] def build(b: Builder): Unit = {
    var current = modifiers
    val arr = new Array[Seq[TagMod]](modifiers.length)

    var i = 0
    while (current != Nil) {
      arr(i) = current.head
      current = current.tail
      i += 1
    }

    var j = arr.length
    while (j > 0) {
      j -= 1
      val frag = arr(j)
      var i = 0
      while (i < frag.length) {
        frag(i).applyTo(b)
        i += 1
      }
    }
  }

  //  override def render: ReactElement = {
  //    val b = new Builder()
  //    build(b)
  //    b.render(tag)
  //  }

  //  override def toString =
  //    render.toString
}

// =====================================================================================================================

final class HtmlTagOf[+N <: HtmlTopNode](val name: String) extends AnyVal {
  def apply(xs: TagMod*): ReactTagOf[N] =
    new ReactTagOf(name, xs :: Nil, Namespace.Html)
}

object HtmlTagOf {
  implicit def autoToTag[N <: HtmlTopNode](t: HtmlTagOf[N]): ReactTagOf[N] =
    new ReactTagOf[N](t.name, Nil, Namespace.Html)
}

// =====================================================================================================================

final class SvgTagOf[+N <: SvgTopNode](val name: String) extends AnyVal {
  def apply(xs: TagMod*): ReactTagOf[N] =
    new ReactTagOf(name, xs :: Nil, Namespace.Svg)
}

object SvgTagOf {
  implicit def autoToTag[N <: SvgTopNode](t: SvgTagOf[N]): ReactTagOf[N] =
    new ReactTagOf[N](t.name, Nil, Namespace.Svg)
}

