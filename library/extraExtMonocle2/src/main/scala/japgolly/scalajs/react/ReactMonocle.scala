package japgolly.scalajs.react

import japgolly.scalajs.react.extra.router.RoutingRule
import japgolly.scalajs.react.extra.router.StaticDsl.RouteCommon
import japgolly.scalajs.react.internal.monocle.{MonocleExtComponent, MonocleExtStateSnapshot}
import monocle._

object ReactMonocle extends MonocleExtComponent with MonocleExtStateSnapshot {

  implicit final class MonocleReactExtModStateFn[F[_], A[_], S](private val self: ModStateFn[F, A, S]) extends AnyVal {
    def xmapStateL[T](l: Iso[S, T]): ModStateFn[F, A, T] =
      self.xmapState(l.get)(l.reverseGet)
  }

  implicit final class MonocleReactExtModStateWithPropsFn[F[_], A[_], P, S](private val self: ModStateWithPropsFn[F, A, P, S]) extends AnyVal {
    def xmapStateL[T](l: Iso[S, T]): ModStateWithPropsFn[F, A, P, T] =
      self.xmapState(l.get)(l.reverseGet)
  }

  implicit final class MonocleReactExtRouteCommon[R[X] <: RouteCommon[R, X], A](private val r: RouteCommon[R, A]) extends AnyVal {
    def pmapL[B](l: Prism[A, B]): R[B] =
      r.pmap(l.getOption)(l.reverseGet)

    def xmapL[B](l: Iso[A, B]): R[B] =
      r.xmap(l.get)(l.reverseGet)
  }

  implicit final class MonocleReactExtRouterRule[Page, Props](private val r: RoutingRule[Page, Props]) extends AnyVal {
    def xmapL[A](l: Iso[Page, A]): RoutingRule[A, Props] =
      r.xmap(l.get)(l.reverseGet)

    def pmapL[W](l: Prism[W, Page]): RoutingRule[W, Props] =
      r.pmapF(l.reverseGet)(l.getOption)
  }

}
