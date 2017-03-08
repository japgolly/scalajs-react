package japgolly.scalajs.react.extra

import japgolly.scalajs.react._

package object router {

  type Router [P] = ScalaComponent              [Unit, Resolution[P], OnUnmount.Backend, CtorType.Nullary]
  type RouterU[P] = ScalaComponent.Unmounted    [Unit, Resolution[P], OnUnmount.Backend]
  type RouterM[P] = ScalaComponent.MountedImpure[Unit, Resolution[P], OnUnmount.Backend]

  private[router] implicit class OptionFnExt[A, B](private val f: A => Option[B]) extends AnyVal {
    def ||(g: A => Option[B]): A => Option[B] = a => f(a) orElse g(a)
    def | (g: A => B)        : A => B         = a => f(a) getOrElse g(a)
  }

  private[router] implicit class OptionFn2Ext[A, B, C](private val f: (A, B) => Option[C]) extends AnyVal {
    def ||(g: (A, B) => Option[C]): (A, B) => Option[C] = (a, b) => f(a, b) orElse g(a, b)
    def | (g: (A, B) => C)        : (A, B) => C         = (a, b) => f(a, b) getOrElse g(a, b)
  }

  private[router] implicit class SaneEitherMethods[A, B](private val e: Either[A, B]) extends AnyVal {
    def map[C](f: B => C): Either[A, C] =
      e match {
        case Right(b) => Right(f(b))
        case l: Left[A, B] => l.asInstanceOf[Left[A, Nothing]]
      }

    def bimap[C, D](f: A => C, g: B => D): Either[C, D] =
      e match {
        case Right(b) => Right(g(b))
        case Left(a)  => Left(f(a))
      }
  }
}
