package japgolly.scalajs.react.vdom

import scala.scalajs.LinkingInfo.developmentMode

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
  def applyTo(b: Builder): Unit

  final def +(that: TagMod): TagMod =
    this compose that

  final def compose(that: TagMod): TagMod =
    this match {
      case l if EmptyTag eq l    => that
      case _ if EmptyTag eq that => this
      case TagMod.Composite(ms)  => TagMod.Composite(ms :+ that)
      case _                     => TagMod.Composite(Vector.empty[TagMod] :+ this :+ that)
    }
}

object TagMod {
  @deprecated("Use EmptyTag.", "-")
  def empty: TagMod =
    EmptyTag

  @inline def apply(ms: TagMod*): TagMod =
    Composite(ms.toVector)

  def fn(f: Builder => Unit): TagMod =
    new TagMod {
      override def applyTo(b: Builder): Unit =
        f(b)
    }

  final case class Composite(ms: Vector[TagMod]) extends TagMod {
    override def applyTo(b: Builder): Unit =
      ms.foreach(_ applyTo b)
  }

  @inline def devOnly(m: => TagMod): TagMod =
    if (developmentMode)
      apply(m)
    else
      EmptyTag
}

