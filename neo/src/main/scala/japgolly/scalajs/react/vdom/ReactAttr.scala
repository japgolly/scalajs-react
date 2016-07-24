package japgolly.scalajs.react.vdom

import scala.annotation.implicitNotFound
import scala.scalajs.LinkingInfo.developmentMode
import scala.scalajs.js
import japgolly.scalajs.react.internal.OptionLike
import ReactAttr.ValueType

/**
  * @tparam U Underlying type of the value required by this attribute.
  */
trait ReactAttr[-U] {
  override def toString = s"ReactAttr.$name"

  val name: String

  def :=[A](a: A)(implicit t: ValueType[A, U]): TagMod

  final def :=?[O[_], A](oa: O[A])(implicit O: OptionLike[O], t: ValueType[A, U]): TagMod =
    O.fold(oa, TagMod.Empty)(:=(_))
}

object ReactAttr {
  // type Event[-E] = ReactAttr[js.Function1[E, Unit]]

  def apply[U](name: String): ReactAttr[U] =
    Generic(name)

  case class Generic[-U](name: String) extends ReactAttr[U] {
    override def :=[A](a: A)(implicit t: ValueType[A, U]): TagMod =
      t(name, a)
  }

  private[vdom] object Dud extends ReactAttr[Any] {
    override val name = ""
    override def :=[A](a: A)(implicit t: ValueType[A, Any]) = TagMod.Empty
  }

  @inline def devOnly[A](name: => String): ReactAttr[A] =
    if (developmentMode)
      Generic(name)
    else
      Dud

  private[vdom] object ClassName extends ReactAttr[String] {
    override val name = "class"
    override def :=[A](a: A)(implicit t: ValueType[A, String]): TagMod =
      TagMod.fn(b => t.fn(b.addClassName, a))
  }

  private[vdom] object Style extends ReactAttr[js.Object] {
    override val name = "style"
    override def :=[A](a: A)(implicit t: ValueType[A, js.Object]): TagMod =
      TagMod.fn(b => t.fn(b.addStyles, a))
  }

//  case object Ref extends ReactAttr[Any] {
//    override def name = "ref"
//    override def :=[A](a: A)(implicit t: ValueType[A, Any]): TagMod =
//      t(name, a)
//
//    import Implicits._react_attrJsFn
//    def apply[N <: TopNode](f: N => Unit): TagMod =
//      this := ((f: js.Function1[N, Unit]): js.Function)
//  }

//  implicit val ordering: Ordering[ReactAttr[Nothing]] =
//    Ordering.by((_: ReactAttr[Nothing]).name)

  // ===================================================================================================================

  /**
    * Used to specify how to handle a particular type [[A]] when it is used as
    * the value of a [[ReactAttr]] of type [[U]].
    *
    * @tparam A Input type. Type provided by dev.
    * @tparam U Underlying type. Type required by attribute.
    */
  @implicitNotFound("Don't know how to use ${A} with a ${U} attribute.")
  final class ValueType[-A, +U](val fn: ValueType.Fn[A]) extends AnyVal {
    def apply(name: String, value: A): TagMod =
      TagMod.fn(b => fn(b.addAttr(name, _), value))
  }

  object ValueType {
    type Simple[A] = ValueType[A, A]

    type Fn[-A] = (js.Any => Unit, A) => Unit

    @inline def apply[A, U](fn: Fn[A]): ValueType[A, U] =
      new ValueType(fn)

    val direct: ValueType[js.Any, Nothing] =
      apply(_(_))

    val string: ValueType[String, Nothing] =
      apply(_(_))

    def byImplicit[A, U](implicit f: A => js.Any): ValueType[A, U] =
      apply((b, a) => b(f(a)))

//    def array[A](implicit f: A => js.Any): Simple[js.Array[A]] =
//      map(_ map f)
//
//    def optional[T[_], A](ot: OptionLike[T], vt: ValueType[A, Any]): Simple[T[A]] =
//      apply((b, ta) => ot.foreach(ta)(vt.fn(b, _)))
  }
}

