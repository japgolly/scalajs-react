package japgolly.scalajs.react.internal

import scala.scalajs.js

trait JsRepr[A] {
  type J <: js.Any
  val toJs: A => J
  val fromJs: J => A

  final def unsafeCastJs(u: js.Any): J =
    u.asInstanceOf[J]

  final def unsafeFromJs(u: js.Any): A =
    fromJs(unsafeCastJs(u))

  final def xmap[B](f: A => B)(g: B => A): JsRepr[B] =
    JsRepr(toJs compose g)(f compose unsafeFromJs)
}

object JsRepr extends JsReprHighPri {
  type Of[A, JJ <: js.Any] = JsRepr[A] { type J = JJ }

  def apply[A, J <: js.Any](toJs: A => J)(fromJs: J => A): Of[A, J] = {
    type _J = J
    val _fromJs = fromJs
    val _toJs = toJs
    new JsRepr[A] {
      override type J = _J
      override val fromJs = _fromJs
      override val toJs = _toJs
    }
  }

  def id[A](implicit f: A => js.Any): JsRepr[A] =
    apply(f)(_.asInstanceOf[A])
}

trait JsReprHighPri extends JsReprMedPri {
  implicit def unit   : JsRepr[Unit   ] = JsRepr.id
  implicit def boolean: JsRepr[Boolean] = JsRepr.id
  implicit def byte   : JsRepr[Byte   ] = JsRepr.id
  implicit def short  : JsRepr[Short  ] = JsRepr.id
  implicit def int    : JsRepr[Int    ] = JsRepr.id
  implicit def long   : JsRepr[Long   ] = double.xmap(_.toLong)(_.toDouble)
  implicit def float  : JsRepr[Float  ] = JsRepr.id
  implicit def double : JsRepr[Double ] = JsRepr.id
  implicit def string : JsRepr[String ] = JsRepr.id
}

trait JsReprMedPri extends JsReprLowPri {
  implicit def jsAny[J <: js.Any]: JsRepr[J] = JsRepr.id
}

trait JsReprLowPri {
  implicit def box[A]: JsRepr[A] = JsRepr(Box.apply[A])(_.unbox)
}
