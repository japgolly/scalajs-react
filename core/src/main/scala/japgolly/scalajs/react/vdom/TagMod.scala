package japgolly.scalajs.react.vdom

import scala.scalajs.LinkingInfo.developmentMode

/**
 * Represents a value that can be nested within a [[TagOf]]. This can be
 * another [[TagMod]], but can also be a CSS style or HTML attribute binding,
 * which will add itself to the node's attributes but not appear in the final
 * `children` list.
 */
trait TagMod extends TagArg {

  final def when(condition: Boolean): TagMod =
    if (condition) this else TagMod.empty

  final def unless(condition: Boolean): TagMod =
    when(!condition)

  def ~(next: TagArg): TagMod =
    TagMod.Composite(Vector.empty[TagArg] :+ this :+ next)

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
  def fn(f: Builder => Unit): TagMod =
    new TagMod {
      override def applyTo(b: Builder): Unit = f(b)
    }

  def apply(ms: TagMod*): TagMod =
    fromTraversableOnce(ms)

  def one(t: TagArg): TagMod =
    t match {
      case m: TagMod => m
      case _         => Composite(Vector.empty[TagArg] :+ t)
    }

  def fromTraversableOnce(t: TraversableOnce[TagArg]): TagMod = {
    val v = t.toVector
    v.length match {
      case 1 => one(v.head)
      case 0 => empty
      case _ => Composite(v)
    }
  }

  final case class Composite(mods: Vector[TagArg]) extends TagMod {
    override def applyTo(b: Builder): Unit =
      mods.foreach(_ applyTo b)

    override def ~(next: TagArg) =
      Composite(mods :+ next)
  }

  val empty: TagMod =
    new TagMod {
      override def applyTo(b: Builder) = ()
      override def ~(m: TagArg) = one(m)
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

  def intercalate(as: TraversableOnce[TagMod], sep: TagMod): TagMod =
    if (as.isEmpty)
      empty
    else {
      val it = as.toIterator
      val first = it.next()
      if (it.isEmpty)
        first
      else {
        val b = Vector.newBuilder[TagMod]
        b += first
        for (a <- it) {
          b += sep
          b += a
        }
        Composite(b.result())
      }
    }
}
