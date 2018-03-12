package japgolly.scalajs.react

import monocle._
import cats.Monad
import japgolly.scalajs.react.internal.{MonocleExtComponent, MonocleExtStateSnapshot}
import japgolly.scalajs.react.extra.router.StaticDsl.{RouteCommon, Rule}
import CatsReact._

object MonocleReact extends MonocleExtComponent with MonocleExtStateSnapshot {

  implicit final class MonocleReactExt_ReactST[M[_], S, A](private val s: ReactST[M, S, A]) extends AnyVal {
    def zoomL[T](l: Lens[T, S])(implicit M: Monad[M]): ReactST[M, T, A] =
      ReactS.zoom[M, S, T, A](s, l.get, (a, b) => l.set(b)(a))
  }

  implicit final class MonocleReactExt_ModStateFn[F[_], S](private val self: ModStateFn[F, S]) extends AnyVal {
    def xmapStateL[T](l: Iso[S, T]): ModStateFn[F, T] =
      self.xmapState(l.get)(l.reverseGet)
  }

  implicit final class MonocleReactExt_ModStateWithPropsFn[F[_], P, S](private val self: ModStateWithPropsFn[F, P, S]) extends AnyVal {
    def xmapStateL[T](l: Iso[S, T]): ModStateWithPropsFn[F, P, T] =
      self.xmapState(l.get)(l.reverseGet)
  }

  implicit final class MonocleReactExt_RouteCommon[R[X] <: RouteCommon[R, X], A](private val r: RouteCommon[R, A]) extends AnyVal {
    def pmapL[B](l: Prism[A, B]): R[B] =
      r.pmap(l.getOption)(l.reverseGet)

    def xmapL[B](l: Iso[A, B]): R[B] =
      r.xmap(l.get)(l.reverseGet)
  }

  implicit final class MonocleReactExt_RouterRule[Page](private val r: Rule[Page]) extends AnyVal {
    def xmapL[A](l: Iso[Page, A]): Rule[A] =
      r.xmap(l.get)(l.reverseGet)

    def pmapL[W](l: Prism[W, Page]): Rule[W] =
      r.pmapF(l.reverseGet)(l.getOption)
  }

}
