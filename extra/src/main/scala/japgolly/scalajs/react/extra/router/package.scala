package japgolly.scalajs.react.extra

import japgolly.scalajs.react.internal.CoreGeneral._
import japgolly.scalajs.react.util.DefaultEffects.Sync
import japgolly.scalajs.react.{CtorType, ScalaComponent}

package object router {

  type RouterWithProps [P, Props] = ScalaComponent              [Props, ResolutionWithProps[P, Props], OnUnmount.Backend, CtorType.Props]
  type RouterWithPropsU[P, Props] = ScalaComponent.Unmounted    [Props, ResolutionWithProps[P, Props], OnUnmount.Backend]
  type RouterWithPropsM[P, Props] = ScalaComponent.MountedImpure[Props, ResolutionWithProps[P, Props], OnUnmount.Backend]

  // START: Compatibility with no-props Router API
  type RouterConfig[P] = RouterWithPropsConfig[P, Unit]
  type Resolution[P]   = ResolutionWithProps[P, Unit]

  type Router [P]      = ScalaComponent              [Unit, Resolution[P], OnUnmount.Backend, CtorType.Nullary]
  type RouterU[P]      = ScalaComponent.Unmounted    [Unit, Resolution[P], OnUnmount.Backend]
  type RouterM[P]      = ScalaComponent.MountedImpure[Unit, Resolution[P], OnUnmount.Backend]
  // END

  private[router] implicit class OptionFnExt[A, B](private val f: A => Option[B]) extends AnyVal {
    def ||(g: A => Option[B]): A => Option[B] = a => f(a) orElse g(a)
    def | (g: A => B)        : A => B         = a => f(a) getOrElse g(a)
  }

  private[router] implicit class CallbackToOptionFnExt[A, B](private val f: A => Sync[Option[B]]) extends AnyVal {
    def ||(g: A => Sync[Option[B]]): A => Sync[Option[B]] =
      a => Sync.flatMap(f(a)) {
        case s@ Some(_) => Sync.pure(s)
        case None       => g(a)
      }
  }

  private[router] implicit class OptionFn2Ext[A, B, C](private val f: (A, B) => Option[C]) extends AnyVal {
    def ||(g: (A, B) => Option[C]): (A, B) => Option[C] = (a, b) => f(a, b) orElse g(a, b)
    def | (g: (A, B) => C)        : (A, B) => C         = (a, b) => f(a, b) getOrElse g(a, b)
  }

  private[router] implicit class CallbackToOptionFn2Ext[A, B, C](private val f: (A, B) => Sync[Option[C]]) extends AnyVal {
    def ||(g: (A, B) => Sync[Option[C]]): (A, B) => Sync[Option[C]] =
      (a, b) => Sync.flatMap(f(a, b)) {
        case s@ Some(_) => Sync.pure(s)
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
