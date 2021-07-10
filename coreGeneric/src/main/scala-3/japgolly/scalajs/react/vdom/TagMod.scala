package japgolly.scalajs.react.vdom

import scala.scalajs.LinkingInfo.developmentMode

/**
 * Represents a value that can be nested within a [[TagOf]]. This can be
 * another [[TagMod]], but can also be a CSS style or HTML attribute binding,
 * which will add itself to the node's attributes but not appear in the final
 * `children` list.
 */
trait TagMod {

  /** Applies this modifier to the specified [[VdomBuilder]], such that when
    * rendering is complete the effect of adding this modifier can be seen.
    */
  def applyTo(b: VdomBuilder): Unit

  final def when(condition: Boolean): TagMod =
    if (condition) this else TagMod.empty

  final inline def unless(inline condition: Boolean): TagMod =
    when(!condition)

  /**
    * Converts this VDOM and all its potential children into raw JS values.
    *
    * Meant for very advanced usage.
    *
    * Do not use this unless you know what you're doing (and you're doing something very funky)!
    */
  final def toJs: VdomBuilder.ToJs = {
    val t = new VdomBuilder.ToJs {}
    applyTo(t)
    t
  }
}

object TagMod {

  val empty: TagMod =
    new TagMod {
      override def applyTo(b: VdomBuilder) = ()
    }

  def fn(f: VdomBuilder => Unit): TagMod =
    new TagMod {
      override def applyTo(b: VdomBuilder): Unit = f(b)
    }

  inline def apply(ms: TagMod*): TagMod =
    fromTraversableOnce(ms)

  def fromTraversableOnce(t: IterableOnce[TagMod]): TagMod = {
    val a = IArray.from(t)
    a.length match {
      case 1 => a.head
      case 0 => empty
      case _ => Composite(a)
    }
  }

  final case class Composite(mods: IArray[TagMod]) extends TagMod {
    override def applyTo(b: VdomBuilder): Unit =
      mods.foreach(_ applyTo b)
  }

  inline def devOnly(inline m: TagMod): TagMod =
    if (developmentMode)
      m
    else
      empty

  def when(cond: Boolean)(t: TagMod): TagMod =
    if (cond) t else empty

  inline def unless(inline cond: Boolean)(inline t: TagMod): TagMod =
    when(!cond)(t)
}
