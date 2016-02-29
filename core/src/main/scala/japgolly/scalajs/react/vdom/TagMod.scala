package japgolly.scalajs.react.vdom

/**
 * Represents a value that can be nested within a [[ReactTagOf]]. This can be
 * another [[TagMod]], but can also be a CSS style or HTML attribute binding,
 * which will add itself to the node's attributes but not appear in the final
 * `children` list.
 */
trait TagMod {
  /**
   * Applies this modifier to the specified [[Builder]], such that when
   * rendering is complete the effect of adding this modifier can be seen.
   */
  def applyTo(t: Builder): Unit

  final def +(that: TagMod): TagMod = this compose that

  final def compose(that: TagMod): TagMod = this match {
    case l if EmptyTag eq l     => that
    case _ if EmptyTag eq that  => this
    case TagMod.Composition(ms) => TagMod.Composition(ms :+ that)
    case _                      => TagMod.Composition(Vector.empty[TagMod] :+ this :+ that)
  }
}

object TagMod {
  @inline def apply(ms: TagMod*): TagMod =
    Composition(ms.toVector)

  case class Composition(ms: Vector[TagMod]) extends TagMod {
    override def applyTo(b: Builder): Unit =
      ms.foreach(_ applyTo b)
  }
}

