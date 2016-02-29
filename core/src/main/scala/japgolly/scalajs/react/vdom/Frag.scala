package japgolly.scalajs.react.vdom

import japgolly.scalajs.react._

/**
 * Marker sub-type of [[TagMod]] which signifies that that type can be
 * rendered as a standalone fragment of [[ReactNode]].
 */
trait Frag extends TagMod {
  def render: ReactNode
}

trait DomFrag extends Frag {
  final def applyTo(b: Builder): Unit =
    b.appendChild(this.render)
}

final class SeqFrag[A](xs: Seq[A])(implicit ev: A => Frag) extends Frag {
  override def applyTo(t: Builder): Unit =
    xs.foreach(_.applyTo(t))

  override def render: ReactElement = {
    val b = new Builder()
    applyTo(b)
    b.render("")
  }
}

final case class ReactNodeFrag(render: ReactNode) extends DomFrag

class ReactTagOf[+N <: TopNode] private[vdom](
  val tag      : String,
  val modifiers: List[Seq[TagMod]],
  val namespace: Namespace) extends DomFrag {

  def copy(tag      : String            = this.tag,
           modifiers: List[Seq[TagMod]] = this.modifiers,
           namespace: Namespace         = this.namespace): ReactTagOf[N] =
    new ReactTagOf(tag, modifiers, namespace)

  override def render: ReactElement = {
    val b = new Builder()
    build(b)
    b.render(tag)
  }

  /**
   * Walks the [[modifiers]] to apply them to a particular [[Builder]].
   * Super sketchy/procedural for max performance.
   */
  private[this] def build(b: Builder): Unit = {
    var current = modifiers
    val arr = new Array[Seq[TagMod]](modifiers.length)

    var i = 0
    while(current != Nil){
      arr(i) = current.head
      current =  current.tail
      i += 1
    }

    var j = arr.length
    while (j > 0) {
      j -= 1
      val frag = arr(j)
      var i = 0
      while(i < frag.length){
        frag(i).applyTo(b)
        i += 1
      }
    }
  }

  def apply(xs: TagMod*): ReactTagOf[N] =
    this.copy(modifiers = xs :: modifiers)

  override def toString =
    render.toString
}

