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
      self.zoomState(l.get)(l.set)

    def modStateL[L[_, _, _, _], A, B](l: L[S, S, A, B])(f: A => B, cb: Callback = Callback.empty)(implicit L: MonocleModifier[L]): F[Unit] =
      self.modState(L.modify(l)(f), cb)

    def modStateOptionL[L[_, _, _, _], A, B](l: L[S, S, A, B])(f: A => Option[B], cb: Callback = Callback.empty)(implicit L: MonocleOptionalModifier[L]): F[Unit] =
      self.modStateOption(L.modifyOption(l)(f), cb)

    def setStateL[L[_, _, _, _], B](l: L[S, S, _, B])(b: B, cb: Callback = Callback.empty)(implicit L: MonocleSetter[L]): F[Unit] =
      self.modState(L.set(l)(b), cb)

    def setStateOptionL[L[_, _, _, _], B](l: L[S, S, _, B])(o: Option[B], cb: Callback = Callback.empty)(implicit L: MonocleSetter[L]): F[Unit] =
      o match {
        case Some(b) => setStateL(l)(b, cb)
        case None    => self.setStateOption(None, cb)
      }

    def modStateAsyncL[L[_, _, _, _], A, B](l: L[S, S, A, B])(f: A => B)(implicit L: MonocleModifier[L]): AsyncCallback[Unit] =
      self.modStateAsync(L.modify(l)(f))

    def modStateOptionAsyncL[L[_, _, _, _], A, B](l: L[S, S, A, B])(f: A => Option[B])(implicit L: MonocleOptionalModifier[L]): AsyncCallback[Unit] =
      self.modStateOptionAsync(L.modifyOption(l)(f))

    def setStateAsyncL[L[_, _, _, _], B](l: L[S, S, _, B])(b: B)(implicit L: MonocleSetter[L]): AsyncCallback[Unit] =
      self.modStateAsync(L.set(l)(b))

    def setStateOptionAsyncL[L[_, _, _, _], B](l: L[S, S, _, B])(o: Option[B])(implicit L: MonocleSetter[L]): AsyncCallback[Unit] =
      o match {
        case Some(b) => setStateAsyncL(l)(b)
        case None    => self.setStateOptionAsync(None)
      }
  }

  final class StateWritableCB[I, S](private val i: I)(implicit sa: StateAccessor.WritePure[I, S]) {
    def modStateL[L[_, _, _, _], A, B](l: L[S, S, A, B])(f: A => B, cb: Callback = Callback.empty)(implicit L: MonocleModifier[L]): Callback =
      sa(i).modState(L.modify(l)(f), cb)

    def modStateOptionL[L[_, _, _, _], A, B](l: L[S, S, A, B])(f: A => Option[B], cb: Callback = Callback.empty)(implicit L: MonocleOptionalModifier[L]): Callback =
      sa(i).modStateOption(L.modifyOption(l)(f), cb)

    def setStateL[L[_, _, _, _], B](l: L[S, S, _, B])(b: B, cb: Callback = Callback.empty)(implicit L: MonocleSetter[L]): Callback =
      sa(i).modState(L.set(l)(b), cb)

    def setStateOptionL[L[_, _, _, _], B](l: L[S, S, _, B])(o: Option[B], cb: Callback = Callback.empty)(implicit L: MonocleSetter[L]): Callback =
      o match {
        case Some(b) => setStateL(l)(b, cb)
        case None    => sa(i).setStateOption(None, cb)
      }

    def modStateAsyncL[L[_, _, _, _], A, B](l: L[S, S, A, B])(f: A => B)(implicit L: MonocleModifier[L]): AsyncCallback[Unit] =
      sa(i).modStateAsync(L.modify(l)(f))

    def modStateOptionAsyncL[L[_, _, _, _], A, B](l: L[S, S, A, B])(f: A => Option[B])(implicit L: MonocleOptionalModifier[L]): AsyncCallback[Unit] =
      sa(i).modStateOptionAsync(L.modifyOption(l)(f))

    def setStateAsyncL[L[_, _, _, _], B](l: L[S, S, _, B])(b: B)(implicit L: MonocleSetter[L]): AsyncCallback[Unit] =
      sa(i).modStateAsync(L.set(l)(b))

    def setStateOptionAsyncL[L[_, _, _, _], B](l: L[S, S, _, B])(o: Option[B])(implicit L: MonocleSetter[L]): AsyncCallback[Unit] =
      o match {
        case Some(b) => setStateAsyncL(l)(b)
        case None    => sa(i).setStateOptionAsync(None)
      }
  }
}
