package japgolly.scalajs.react.extra

import japgolly.scalajs.react._

package object router {

  type Router [P] = ReactComponentC.ConstProps[Unit, Resolution[P], Any, TopNode]
  type RouterU[P] = ReactComponentU           [Unit, Resolution[P], Any, TopNode]
  type RouterM[P] = ReactComponentM           [Unit, Resolution[P], Any, TopNode]

  private[router] implicit class OptionFnExt[A, B](private val f: A => Option[B]) extends AnyVal {
    def ||(g: A => Option[B]): A => Option[B] = a => f(a) orElse g(a)
    def | (g: A => B)        : A => B         = a => f(a) getOrElse g(a)
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
