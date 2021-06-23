package japgolly.scalajs.react.internal.monocle

import japgolly.scalajs.react._
import japgolly.scalajs.react.util.DefaultEffects._
import japgolly.scalajs.react.util.Effect.Dispatch

trait MonocleExtComponentLowPriorityImplicits {
  implicit final def MonocleReactExt_StateWritableCB[I, F[_], A[_], S](i: I)(implicit sa: StateAccessor.Write[I, F, A, S]): MonocleExtComponent.StateWritableCB[I, F, A, S] =
    new MonocleExtComponent.StateWritableCB(i)(sa)
}

trait MonocleExtComponent extends MonocleExtComponentLowPriorityImplicits {
  implicit final def MonocleReactExt_StateAccess[F[_], A[_], S](m: StateAccess[F, A, S]): MonocleExtComponent.StateAcc[F, A, S, m.type] =
    new MonocleExtComponent.StateAcc[F, A, S, m.type](m)
}

object MonocleExtComponent {
  // Keep this import here so that Lens etc take priority over .internal
  import monocle._

  final class OptionalDispatchDsl1[A, L, B](private val f: (L, A, Sync[Unit]) => B) extends AnyVal {
    def apply(a: A)(implicit l: L): B =
      f(l, a, Sync.empty)

    def apply[G[_]](a: A, callback: => G[Unit])(implicit l: L, G: Dispatch[G]): B =
      f(l, a, Sync.transDispatch(callback))
  }

  final class StateAcc[F[_], FA[_], S, M <: StateAccess[F, FA, S]](val self: M) extends AnyVal {
    def zoomStateL[T](l: Lens[S, T]): self.WithMappedState[T] =
      self.zoomState(l.get)(l.set)

    def modStateL[L[_, _, _, _], A, B](l: L[S, S, A, B]): OptionalDispatchDsl1[A => B, MonocleModifier[L], F[Unit]] =
      new OptionalDispatchDsl1((L, f, cb) => self.modState(L.modify(l)(f), cb))

    def modStateOptionL[L[_, _, _, _], A, B](l: L[S, S, A, B]): OptionalDispatchDsl1[A => Option[B], MonocleOptionalModifier[L], F[Unit]] =
      new OptionalDispatchDsl1((L, f, cb) => self.modStateOption(L.modifyOption(l)(f), cb))

    def setStateL[L[_, _, _, _], A, B](l: L[S, S, A, B]): OptionalDispatchDsl1[B, MonocleSetter[L], F[Unit]] =
      new OptionalDispatchDsl1((L, b, cb) => self.modState(L.set(l)(b), cb))

    def setStateOptionL[L[_, _, _, _], A, B](l: L[S, S, A, B]): OptionalDispatchDsl1[Option[B], MonocleSetter[L], F[Unit]] =
      new OptionalDispatchDsl1((L, o, cb) =>
        o match {
          case Some(b) => setStateL(l)(b, cb)(L, Sync)
          case None    => self.setStateOption(None, cb)
        }
      )

    def modStateAsyncL[L[_, _, _, _], A, B](l: L[S, S, A, B])(f: A => B)(implicit L: MonocleModifier[L]): FA[Unit] =
      self.modStateAsync(L.modify(l)(f))

    def modStateOptionAsyncL[L[_, _, _, _], A, B](l: L[S, S, A, B])(f: A => Option[B])(implicit L: MonocleOptionalModifier[L]): FA[Unit] =
      self.modStateOptionAsync(L.modifyOption(l)(f))

    def setStateAsyncL[L[_, _, _, _], A, B](l: L[S, S, A, B])(b: B)(implicit L: MonocleSetter[L]): FA[Unit] =
      self.modStateAsync(L.set(l)(b))

    def setStateOptionAsyncL[L[_, _, _, _], A, B](l: L[S, S, A, B])(o: Option[B])(implicit L: MonocleSetter[L]): FA[Unit] =
      o match {
        case Some(b) => setStateAsyncL(l)(b)
        case None    => self.setStateOptionAsync(None)
      }
  }

  final class StateWritableCB[I, F[_], FA[_], S](private val i: I)(implicit sa: StateAccessor.Write[I, F, FA, S]) {
    def modStateL[L[_, _, _, _], A, B](l: L[S, S, A, B]): OptionalDispatchDsl1[A => B, MonocleModifier[L], F[Unit]] =
      new OptionalDispatchDsl1((L, f, cb) => sa(i).modState(L.modify(l)(f), cb))

    def modStateOptionL[L[_, _, _, _], A, B](l: L[S, S, A, B]): OptionalDispatchDsl1[A => Option[B], MonocleOptionalModifier[L], F[Unit]] =
      new OptionalDispatchDsl1((L, f, cb) => sa(i).modStateOption(L.modifyOption(l)(f), cb))

    def setStateL[L[_, _, _, _], A, B](l: L[S, S, A, B]): OptionalDispatchDsl1[B, MonocleSetter[L], F[Unit]] =
      new OptionalDispatchDsl1((L, b, cb) => sa(i).modState(L.set(l)(b), cb))

    def setStateOptionL[L[_, _, _, _], A, B](l: L[S, S, A, B]): OptionalDispatchDsl1[Option[B], MonocleSetter[L], F[Unit]] =
      new OptionalDispatchDsl1((L, o, cb) =>
        o match {
          case Some(b) => setStateL(l)(b, cb)(L, Sync)
          case None    => sa(i).setStateOption(None, cb)
        }
      )

    def modStateAsyncL[L[_, _, _, _], A, B](l: L[S, S, A, B])(f: A => B)(implicit L: MonocleModifier[L]): FA[Unit] =
      sa(i).modStateAsync(L.modify(l)(f))

    def modStateOptionAsyncL[L[_, _, _, _], A, B](l: L[S, S, A, B])(f: A => Option[B])(implicit L: MonocleOptionalModifier[L]): FA[Unit] =
      sa(i).modStateOptionAsync(L.modifyOption(l)(f))

    def setStateAsyncL[L[_, _, _, _], A, B](l: L[S, S, A, B])(b: B)(implicit L: MonocleSetter[L]): FA[Unit] =
      sa(i).modStateAsync(L.set(l)(b))

    def setStateOptionAsyncL[L[_, _, _, _], A, B](l: L[S, S, A, B])(o: Option[B])(implicit L: MonocleSetter[L]): FA[Unit] =
      o match {
        case Some(b) => setStateAsyncL(l)(b)
        case None    => sa(i).setStateOptionAsync(None)
      }
  }
}
