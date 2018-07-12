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
}

trait JsReprLowPri {
  implicit def box[A]: JsRepr.Of[A, Box[A]] =
    JsRepr(Box.apply[A])(_.unbox)
}

object JsRepr extends JsReprLowPri {
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

  def identity[A](implicit f: A => js.Any): JsRepr.Of[A, js.Any with A] =
    JsRepr((_: A).asInstanceOf[js.Any with A])(identityFn)
//  def identity[A](implicit f: A => js.Any): JsRepr.Of[A, js.Any] =
//    JsRepr(f)(_.asInstanceOf[A])

  implicit def boolean = //: JsRepr.Of[Int, Int] =
    identity[Boolean]

  implicit def int = //: JsRepr.Of[Int, Int] =
    identity[Int]
}