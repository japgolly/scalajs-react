package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{Callback, Ref, raw => Raw}

class TagOf[+N <: TopNode] private[vdom](final val tag: String,
                                         final protected val modifiers: List[Seq[TagMod]],
                                         final val namespace: Namespace) extends TagMod {

  @deprecated("Use .withRef instead", "1.2.0")
  def ref[NN >: N <: TopNode](r: Ref.Set[NN]): TagOf[NN] =
    (this: TagOf[NN])(Attr.Ref(r))

  def withRef[NN >: N <: TopNode, R](ref: TagOf.RefArg[NN]): TagOf[NN] =
    ref.value match {
      case Some(r) => this(Attr.Ref(r))
      case None    => this
    }

  override def apply(xs: TagMod*): TagOf[N] =
    copy(modifiers = xs :: modifiers)

  protected def copy(tag: String = this.tag,
                     modifiers: List[Seq[TagMod]] = this.modifiers,
                     namespace: Namespace = this.namespace): TagOf[N] =
    new TagOf(tag, modifiers, namespace)

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

  lazy val render: VdomElement = {
    val b = new Builder.ToVdomElement()
    build(b)
    b.render(tag)
  }

  override def toString =
    render.toString

  override def applyTo(b: Builder): Unit =
    b.appendChild(render.rawElement)

  def renderIntoDOM(container: Raw.ReactDOM.Container, callback: Callback = Callback.empty) =
    render.renderIntoDOM(container, callback)
}

object TagOf {
  final case class RefArg[N <: TopNode](value: Option[Ref.Set[N]]) extends AnyVal
  object RefArg {
    implicit def set[N <: TopNode](r: Ref.Set[N]): RefArg[N] = apply(Some(r))
    implicit def option[N <: TopNode](o: Option[Ref.Set[N]]): RefArg[N] = apply(o)
  }
}

// =====================================================================================================================

final case class HtmlTagOf[+N <: HtmlTopNode](name: String) extends AnyVal {
  def apply(xs: TagMod*): TagOf[N] =
    new TagOf(name, xs :: Nil, Namespace.Html)
}

object HtmlTagOf {
  implicit def autoToTag[N <: HtmlTopNode](t: HtmlTagOf[N]): TagOf[N] =
    new TagOf[N](t.name, Nil, Namespace.Html)
}

// =====================================================================================================================

final case class SvgTagOf[+N <: SvgTopNode](name: String) extends AnyVal {
  def apply(xs: TagMod*): TagOf[N] =
    new TagOf(name, xs :: Nil, Namespace.Svg)
}

object SvgTagOf {
  implicit def autoToTag[N <: SvgTopNode](t: SvgTagOf[N]): TagOf[N] =
    new TagOf[N](t.name, Nil, Namespace.Svg)
}

