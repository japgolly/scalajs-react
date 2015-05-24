package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import scalaz.Free
import scalaz.effect.IO
import scalaz.syntax.bind.ToBindOps

package object router2 {

  type Router [P] = ReactComponentC.ConstProps[Unit, Resolution[P], Any, TopNode]
  type RouterU[P] = ReactComponentU           [Unit, Resolution[P], Any, TopNode]
  type RouterM[P] = ReactComponentM           [Unit, Resolution[P], Any, TopNode]

  /** Free monad & free functor over route commands. */
  type RouteProg[A] = Free.FreeC[RouteCmd, A]

  implicit def reactAutoLiftRouteProg[A](c: RouteCmd[A]): RouteProg[A] =
    Free.liftFC[RouteCmd, A](c)

  implicit final class ReactRouteProgExt[P, A](val _a: RouteProg[A]) extends AnyVal {
    def >>[B](b: RouteProg[B]): RouteProg[B] = _a flatMap (_ => b)
  }

  implicit final class ReactRouteCmdExt[P, A](val _a: RouteCmd[A]) extends AnyVal {
    def >>[B](b: RouteProg[B]): RouteProg[B] = new ReactRouteProgExt(_a) >> b
  }

  implicit final class ReactRouteIOExt[A](val _io: IO[A]) extends AnyVal {
    @inline def <<(prepend: IO[Unit]): IO[A] = prepend >> _io
  }

  private[router2] implicit class OptionFnExt[A, B](val f: A => Option[B]) extends AnyVal {
    def ||(g: A => Option[B]): A => Option[B] = a => f(a) orElse g(a)
    def | (g: A => B)        : A => B         = a => f(a) getOrElse g(a)
  }
}