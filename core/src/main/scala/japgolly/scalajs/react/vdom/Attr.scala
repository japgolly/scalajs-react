package japgolly.scalajs.react.vdom

import scala.annotation.implicitNotFound
import scala.scalajs.js
import japgolly.scalajs.react.{TopNode, OptionLike}
import Attr.ValueType

trait Attr {
  def name: String
  def :=[A](a: A)(implicit t: ValueType[A]): TagMod
}

object Attr {

  final case class Generic(name: String) extends Attr {
    Escaping.assertValidAttrName(name)

    override def :=[A](a: A)(implicit t: ValueType[A]): TagMod =
      new NameAndValue(name, a, t)
  }

  case object ClassName extends Attr {
    override def name = "class"
    override def :=[A](a: A)(implicit t: ValueType[A]): TagMod =
      TagMod.fn(b => t.apply(b.addClassName, a))
  }

  case object Ref extends Attr {
    override def name = "ref"
    override def :=[A](a: A)(implicit t: ValueType[A]): TagMod =
      new Attr.NameAndValue(name, a, t)

    import Implicits._react_attrJsFn
    def apply[N <: TopNode](f: N => Unit): TagMod =
      this := ((f: js.Function1[N, Unit]): js.Function)
  }

  implicit val ordering: Ordering[Attr] =
    Ordering.by((_: Attr).name)

  final class NameAndValue[A](val name: String, val value: A, val valueType: ValueType[A]) extends TagMod {
    override def applyTo(b: Builder): Unit =
      valueType.apply(b.addAttr(name, _), value)
  }

  // ===================================================================================================================

  /**
    * Used to specify how to handle a particular type [[A]] when it is used as
    * the value of a [[Attr]]. Only types with a specified [[Attr.ValueType]] may
    * be used.
    */
  @implicitNotFound("No Attr.ValueType defined for type ${A}; don't know how to use ${A} as an attribute.")
  final class ValueType[A](val apply: ValueType.Fn[A]) extends AnyVal

  object ValueType {
    type Fn[A] = (js.Any => Unit, A) => Unit

    @inline def apply[A](fn: Fn[A]): ValueType[A] =
      new ValueType(fn)

    val string: ValueType[String] =
      apply(_(_))

    def map[A](implicit f: A => js.Any): ValueType[A] =
      apply((b, a) => b(f(a)))

    def array[A](implicit f: A => js.Any): ValueType[js.Array[A]] =
      map(_ map f)

    def optional[T[_], A](ot: OptionLike[T], vt: ValueType[A]): ValueType[T[A]] =
      apply((b, ta) => ot.foreach(ta)(vt.apply(b, _)))
  }
}

