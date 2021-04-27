package japgolly.scalajs.react.internal

import japgolly.scalajs.react._

trait MonocleExtComponentLowPriorityImplicits {
  implicit final def MonocleReactExt_StateWritableCB[I, S](i: I)(implicit sa: StateAccessor.WritePure[I, S]): MonocleExtComponent.StateWritableCB[I, S] =
    new MonocleExtComponent.StateWritableCB[I, S](i)(sa)
}

trait MonocleExtComponent extends MonocleExtComponentLowPriorityImplicits {
  implicit final def MonocleReactExt_StateAccess[F[_], S](m: StateAccess[F, S]): MonocleExtComponent.StateAcc[F, S, m.type] =
    new MonocleExtComponent.StateAcc[F, S, m.type](m)
}

object MonocleExtComponent {
  // Keep this import here so that Lens etc take priority over .internal
  import monocle._

  final class StateAcc[F[_], S, M <: StateAccess[F, S]](val self: M) extends AnyVal {
    def zoomStateL[T](l: Lens[S, T]): self.WithMappedState[T] =
      self.zoomState(l.get)(l.replace)

    def modStateL[A, B](L: PSetter[S, S, A, B])(f: A => B, cb: Callback = Callback.empty): F[Unit] =
      self.modState(L.modify(f), cb)

    def modStateOptionL[A, B](l: PTraversal[S, S, A, B])(f: A => Option[B], cb: Callback = Callback.empty): F[Unit] =
      self.modStateOption(l.modifyA(f), cb)

    def setStateL[B](l: PSetter[S, S, _, B])(b: B, cb: Callback = Callback.empty): F[Unit] =
      self.modState(l.replace(b), cb)

    def setStateOptionL[ B](l: PSetter[S, S, _, B])(o: Option[B], cb: Callback = Callback.empty): F[Unit] =
      o match {
        case Some(b) => setStateL(l)(b, cb)
        case None    => self.setStateOption(None, cb)
      }

    def modStateAsyncL[A, B](l: PSetter[S, S, A, B])(f: A => B): AsyncCallback[Unit] =
      self.modStateAsync(l.modify(f))

    def modStateOptionAsyncL[A, B](l: PTraversal[S, S, A, B])(f: A => Option[B]): AsyncCallback[Unit] =
      self.modStateOptionAsync(l.modifyA(f))

    def setStateAsyncL[B](l: PSetter[S, S, _, B])(b: B): AsyncCallback[Unit] =
      self.modStateAsync(l.replace(b))

    def setStateOptionAsyncL[B](l: PSetter[S, S, _, B])(o: Option[B]): AsyncCallback[Unit] =
      o match {
        case Some(b) => setStateAsyncL(l)(b)
        case None    => self.setStateOptionAsync(None)
      }
  }

  final class StateWritableCB[I, S](private val i: I)(implicit sa: StateAccessor.WritePure[I, S]) {
    def modStateL[A, B](l: PLens[S, S, A, B])(f: A => B, cb: Callback = Callback.empty): Callback =
      sa(i).modState(l.modify(f), cb)

    def modStateOptionL[A, B](l: PTraversal[S, S, A, B])(f: A => Option[B], cb: Callback = Callback.empty): Callback =
      sa(i).modStateOption(l.modifyA(f), cb)

    def setStateL[B](l: PSetter[S, S, _, B])(b: B, cb: Callback = Callback.empty): Callback =
      sa(i).modState(l.replace(b), cb)

    def setStateOptionL[B](l: PSetter[S, S, _, B])(o: Option[B], cb: Callback = Callback.empty): Callback =
      o match {
        case Some(b) => setStateL(l)(b, cb)
        case None    => sa(i).setStateOption(None, cb)
      }

    def modStateAsyncL[A, B](l: PSetter[S, S, A, B])(f: A => B): AsyncCallback[Unit] =
      sa(i).modStateAsync(l.modify(f))

    def modStateOptionAsyncL[A, B](l: PTraversal[S, S, A, B])(f: A => Option[B]): AsyncCallback[Unit] =
      sa(i).modStateOptionAsync(l.modifyA(f))

    def setStateAsyncL[B](l: PSetter[S, S, _, B])(b: B): AsyncCallback[Unit] =
      sa(i).modStateAsync(l.replace(b))

    def setStateOptionAsyncL[B](l: PSetter[S, S, _, B])(o: Option[B]): AsyncCallback[Unit] =
      o match {
        case Some(b) => setStateAsyncL(l)(b)
        case None    => sa(i).setStateOptionAsync(None)
      }
  }
}
