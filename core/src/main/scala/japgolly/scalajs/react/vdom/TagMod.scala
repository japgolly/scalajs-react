package japgolly.scalajs.react.vdom

import scala.collection.compat._
import scala.scalajs.LinkingInfo.developmentMode

/**
 * Represents a value that can be nested within a [[TagOf]]. This can be
 * another [[TagMod]], but can also be a CSS style or HTML attribute binding,
 * which will add itself to the node's attributes but not appear in the final
 * `children` list.
 */
trait TagMod {

  /** Applies this modifier to the specified [[Builder]], such that when
    * rendering is complete the effect of adding this modifier can be seen.
    */
  def applyTo(b: Builder): Unit

  final def when(condition: Boolean): TagMod =
    if (condition) this else TagMod.empty

  final def unless(condition: Boolean): TagMod =
    when(!condition)

  /**
    * Converts this VDOM and all its potential children into raw JS values.
    *
    * Meant for very advanced usage.
    *
    * Do not use this unless you know what you're doing (and you're doing something very funky)!
    */
  final def toJs: Builder.ToJs = {
    val t = new Builder.ToJs {}
    applyTo(t)
    t
  }
}

object TagMod {

  val empty: TagMod =
    new TagMod {
      override def applyTo(b: Builder) = ()
    }

  def fn(f: Builder => Unit): TagMod =
    new TagMod {
      override def applyTo(b: Builder): Unit = f(b)
    }

  def apply(ms: TagMod*): TagMod =
    fromTraversableOnce(ms)

  def fromTraversableOnce(t: IterableOnce[TagMod]): TagMod = {
    val v = t.iterator.to(Vector)
    v.length match {
      case 1 => v.head
      case 0 => empty
      case _ => Composite(v)
    }
  }

  final case class Composite(mods: Vector[TagMod]) extends TagMod {
    override def applyTo(b: Builder): Unit =
      mods.foreach(_ applyTo b)
  }

  def devOnly(m: => TagMod): TagMod =
    if (developmentMode)
      m
    else
      empty

  def when(cond: Boolean)(t: => TagMod): TagMod =
    if (cond) t else empty

  @inline def unless(cond: Boolean)(t: => TagMod): TagMod =
    when(!cond)(t)
}
