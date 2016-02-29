package japgolly.scalajs.react.vdom

import scala.annotation.implicitNotFound
import japgolly.scalajs.react.OptionLike
import japgolly.scalajs.react.vdom.Style.ValueType

trait Style {
  def name: String
  def :=[A](a: A)(implicit t: ValueType[A]): TagMod
}

object Style {

  @inline def Generic(jsName_unused: => String, name: String) =
    new Generic(jsName_unused, name)

  class Generic(jsName_unused: => String, final val name: String) extends Style {

    override final def :=[A](a: A)(implicit t: ValueType[A]): TagMod =
      new NameAndValue(name, a, t)
  }

  implicit val ordering: Ordering[Style] =
    Ordering.by((_: Style).name)

  final class NameAndValue[A](val name: String, val value: A, val valueType: ValueType[A]) extends TagMod {
    override def applyTo(b: Builder): Unit =
      valueType.apply(b.addStyle(name, _), value)
  }

//  // ===================================================================================================================
//
  /**
   * Used to specify how to handle a particular type [[A]] when it is used as
   * the value of a [[Style]]. Only types with a specified [[Style.ValueType]] may
   * be used.
   */
  @implicitNotFound("No StyleValue defined for type ${A}; don't know how to use ${A} as a style." )
  final class ValueType[A](val apply: ValueType.Fn[A]) extends AnyVal

  object ValueType {
    type Fn[A] = (String => Unit, A) => Unit

    @inline def apply[A](fn: Fn[A]): ValueType[A] =
      new ValueType(fn)

    val string: ValueType[String] =
      apply(_(_))

    def stringValue[A]: ValueType[A] =
      apply((b, a) => b(a.toString))

    def optional[T[_], A](ot: OptionLike[T], vt: ValueType[A]): ValueType[T[A]] =
      apply((b, ta) => ot.foreach(ta)(vt.apply(b, _)))
  }
}

