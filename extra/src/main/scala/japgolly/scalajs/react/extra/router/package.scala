package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement

package object router {

  type RouterP[P, Props] = ScalaComponent              [Props, ResolutionP[P, Props], OnUnmount.Backend, CtorType.Props]
  type RouterU[P, Props] = ScalaComponent.Unmounted    [Props, ResolutionP[P, Props], OnUnmount.Backend]
  type RouterM[P, Props] = ScalaComponent.MountedImpure[Props, ResolutionP[P, Props], OnUnmount.Backend]

  // START: Compatibility with contextless Router API
  type Router [P]        = ScalaComponent              [Unit, ResolutionP[P, Unit], OnUnmount.Backend, CtorType.Nullary]

  type RouterConfig[P] = RouterConfigP[P, Unit]
  type Resolution[P]   = ResolutionP[P, Unit]

  implicit class ResolutionOps[P](resolution: Resolution[P]) {
    def render(): VdomElement = resolution.renderP(())
  }
  // END

  private[router] implicit class OptionFnExt[A, B](private val f: A => Option[B]) extends AnyVal {
    def ||(g: A => Option[B]): A => Option[B] = a => f(a) orElse g(a)
    def | (g: A => B)        : A => B         = a => f(a) getOrElse g(a)
  }

  private[router] implicit class CallbackToOptionFnExt[A, B](private val f: A => CallbackTo[Option[B]]) extends AnyVal {
    def ||(g: A => CallbackTo[Option[B]]): A => CallbackTo[Option[B]] =
      a => f(a).flatMap {
        case s@ Some(_) => CallbackTo.pure(s)
        case None       => g(a)
      }
  }

  private[router] implicit class OptionFn2Ext[A, B, C](private val f: (A, B) => Option[C]) extends AnyVal {
    def ||(g: (A, B) => Option[C]): (A, B) => Option[C] = (a, b) => f(a, b) orElse g(a, b)
    def | (g: (A, B) => C)        : (A, B) => C         = (a, b) => f(a, b) getOrElse g(a, b)
  }

  private[router] implicit class CallbackToOptionFn2Ext[A, B, C](private val f: (A, B) => CallbackTo[Option[C]]) extends AnyVal {
    def ||(g: (A, B) => CallbackTo[Option[C]]): (A, B) => CallbackTo[Option[C]] =
      (a, b) => f(a, b).flatMap {
        case s@ Some(_) => CallbackTo.pure(s)
        case None       => g(a, b)
      }
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
