package japgolly.scalajs.react.vdom

import scala.scalajs.LinkingInfo.developmentMode

/**
 * Represents a value that can be nested within a [[TagOf]]. This can be
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

  final def when(condition: Boolean): TagMod =
    if (condition) this else TagMod.Empty

  final def unless(condition: Boolean): TagMod =
    when(!condition)

  def apply(ms: TagMod*): TagMod =
    if (ms.isEmpty) this else TagMod.Composite((Iterator.single(this) ++ ms).toVector)
}

object TagMod {
  def fn(f: Builder => Unit): TagMod =
    new TagMod {
      override def applyTo(b: Builder): Unit =
        f(b)
    }

  def apply(ms: TagMod*): TagMod = {
    val v = ms.toVector
    v.length match {
      case 0 => Empty
      case 1 => v.head
      case _ => Composite(v)
    }
  }

  final case class Composite(mods: Vector[TagMod]) extends TagMod {
    override def applyTo(b: Builder): Unit =
      mods.foreach(_ applyTo b)

    override def apply(ms: TagMod*) =
      if (ms.isEmpty) this else Composite(mods ++ ms)
  }

  private[vdom] val Empty: TagMod =
    new TagMod {
      override def toString = "EmptyTag"
      override def applyTo(b: Builder) = ()
      override def apply(ms: TagMod*) = TagMod(ms: _*)
    }

  def devOnly(m: => TagMod): TagMod =
    if (developmentMode)
      m
    else
      Empty
}
