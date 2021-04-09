package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{Ref, raw => Raw}
import scala.scalajs.js

class TagOf[+N <: TopNode] private[vdom](final val tag: String,
                                         final protected val modifiers: List[Seq[TagMod]]) extends VdomElement {

  def this(tag: String) =
    this(tag, Nil)

  def withRef[NN >: N <: TopNode, R](ref: TagOf.RefArg[NN]): TagOf[NN] =
    ref.value match {
      case Some(r) => this(Attr.ref(r))
      case None    => this
    }

  def withOptionalRef[NN >: N <: TopNode, R](optionalRef: Option[Ref.Set[NN]]): TagOf[NN] =
    optionalRef match {
      case None    => this
      case Some(r) => withRef(r)
    }

  def apply(xs: TagMod*): TagOf[N] =
    copy(modifiers = xs :: modifiers)

  protected def copy(tag      : String            = this.tag,
                     modifiers: List[Seq[TagMod]] = this.modifiers,
                    ): TagOf[N] =
    new TagOf(tag, modifiers)

  override lazy val rawElement: Raw.React.Element = {
    val b = new VdomBuilder.ToRawReactElement()

    val arr = new js.Array[Seq[TagMod]]
    var current = modifiers
    while (current != Nil) {
      arr.push(current.head)
      current = current.tail
    }
    var j = arr.length
    while (j > 0) {
      j -= 1
      arr(j).foreach(_.applyTo(b))
    }

    b.render(tag)
  }

  override def applyTo(b: VdomBuilder): Unit =
    b.appendChild(rawElement)

  override def toString =
    if (modifiers.isEmpty)
      s"<$tag />"
    else
      s"<$tag>…</$tag>"

}

object TagOf {
  final case class RefArg[N <: TopNode](value: Option[Ref.Set[N]]) extends AnyVal
  object RefArg {
    implicit def set[N <: TopNode](r: Ref.Set[N]): RefArg[N] = apply(Some(r))

    @deprecated("Use .withOptionalRef", "1.7.0")
    implicit def option[N <: TopNode](o: Option[Ref.Set[N]]): RefArg[N] = apply(o)
  }
}
