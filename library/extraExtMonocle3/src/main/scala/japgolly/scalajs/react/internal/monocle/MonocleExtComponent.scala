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

  final class OptionalDispatchDsl1[A, B](private val f: (A, Sync[Unit]) => B) extends AnyVal {
    def apply(a: A): B =
      f(a, Sync.empty)

    def apply[G[_]](a: A, callback: => G[Unit])(implicit G: Dispatch[G]): B =
      f(a, Sync.transDispatch(callback))
  }

  final class StateAcc[F[_], FA[_], S, M <: StateAccess[F, FA, S]](val self: M) extends AnyVal {
    def zoomStateL[T](l: Lens[S, T]): self.WithMappedState[T] =
      self.zoomState(l.get)(l.replace)

    def modStateL[A, B](l: PSetter[S, S, A, B]): OptionalDispatchDsl1[A => B, F[Unit]] =
      new OptionalDispatchDsl1((f, cb) => self.modState(l.modify(f), cb))

    def modStateOptionL[A, B](l: PTraversal[S, S, A, B]): OptionalDispatchDsl1[A => Option[B], F[Unit]] =
      new OptionalDispatchDsl1((f, cb) => self.modStateOption(l.modifyA(f), cb))

    def setStateL[A, B](l: PSetter[S, S, A, B]): OptionalDispatchDsl1[B, F[Unit]] =
      new OptionalDispatchDsl1((b, cb) => self.modState(l.replace(b), cb))

    def setStateOptionL[A, B](l: PSetter[S, S, A, B]): OptionalDispatchDsl1[Option[B], F[Unit]] =
      new OptionalDispatchDsl1((o, cb) =>
        o match {
          case Some(b) => setStateL(l)(b, cb)
          case None    => self.setStateOption(None, cb)
        }
      )

    def modStateAsyncL[A, B](l: PSetter[S, S, A, B])(f: A => B): FA[Unit] =
      self.modStateAsync(l.modify(f))

    def modStateOptionAsyncL[A, B](l: PTraversal[S, S, A, B])(f: A => Option[B]): FA[Unit] =
      self.modStateOptionAsync(l.modifyA(f))

    def setStateAsyncL[A, B](l: PSetter[S, S, A, B])(b: B): FA[Unit] =
      self.modStateAsync(l.replace(b))

    def setStateOptionAsyncL[A, B](l: PSetter[S, S, A, B])(o: Option[B]): FA[Unit] =
      o match {
        case Some(b) => setStateAsyncL(l)(b)
        case None    => self.setStateOptionAsync(None)
      }
  }

  final class StateWritableCB[I, F[_], FA[_], S](private val i: I)(implicit sa: StateAccessor.Write[I, F, FA, S]) {
    def modStateL[A, B](l: PSetter[S, S, A, B]): OptionalDispatchDsl1[A => B, F[Unit]] =
      new OptionalDispatchDsl1((f, cb) => sa(i).modState(l.modify(f), cb))

    def modStateOptionL[A, B](l: PTraversal[S, S, A, B]): OptionalDispatchDsl1[A => Option[B], F[Unit]] =
      new OptionalDispatchDsl1((f, cb) => sa(i).modStateOption(l.modifyA(f), cb))

    def setStateL[A, B](l: PSetter[S, S, A, B]): OptionalDispatchDsl1[B, F[Unit]] =
      new OptionalDispatchDsl1((b, cb) => sa(i).modState(l.replace(b), cb))

    def setStateOptionL[A, B](l: PSetter[S, S, A, B]): OptionalDispatchDsl1[Option[B], F[Unit]] =
      new OptionalDispatchDsl1((o, cb) =>
        o match {
          case Some(b) => setStateL(l)(b, cb)
          case None    => sa(i).setStateOption(None, cb)
        }
      )

    def modStateAsyncL[A, B](l: PSetter[S, S, A, B])(f: A => B): FA[Unit] =
      sa(i).modStateAsync(l.modify(f))

    def modStateOptionAsyncL[A, B](l: PTraversal[S, S, A, B])(f: A => Option[B]): FA[Unit] =
      sa(i).modStateOptionAsync(l.modifyA(f))

    def setStateAsyncL[A, B](l: PSetter[S, S, A, B])(b: B): FA[Unit] =
      sa(i).modStateAsync(l.replace(b))

    def setStateOptionAsyncL[A, B](l: PSetter[S, S, A, B])(o: Option[B]): FA[Unit] =
      o match {
        case Some(b) => setStateAsyncL(l)(b)
        case None    => sa(i).setStateOptionAsync(None)
      }
  }
}
