package japgolly.scalajs.react.internal

import monocle._
import japgolly.scalajs.react._

trait MonocleExtComponent {
  implicit final def MonocleReactExt_StateAccess[F[+_], S, M <: StateAccess[F, S]](m: M) = new MonocleExtComponent.StateAcc[F, S, m.type](m)
  implicit final def MonocleReactExt_StateWritableCB[I, S](i: I)(implicit sa: StateAccessor.WriteCB[I, S]) = new MonocleExtComponent.StateWritableCB[I, S](i)(sa)
}

object MonocleExtComponent {

  final class StateAcc[F[+_], S, M <: StateAccess[F, S]](val self: M) extends AnyVal {
    def zoomStateL[T](l: Lens[S, T]): self.WithMappedState[T] =
      self.zoomState(l.get)(l.set)

    def modStateL[A, B](l: PLens[S, S, A, B])(f: A => B, cb: Callback = Callback.empty): F[Unit] =
      self.modState(l.modify(f), cb)

    def setStateL[L[_, _, _, _], B](l: L[S, S, _, B])(b: B, cb: Callback = Callback.empty)(implicit L: MonocleSetter[L]): F[Unit] =
      self.modState(L.set(l)(b), cb)

    def setStateFnL[L[_, _, _, _], B](l: L[S, S, _, B], cb: Callback = Callback.empty)(implicit L: MonocleSetter[L]): B => F[Unit] =
      setStateL(l)(_, cb)
  }

  // TODO what about the conflicts between these? test

  final class StateWritableCB[I, S](private val i: I)(implicit sa: StateAccessor.WriteCB[I, S]) {
    def modStateL[A, B](l: PLens[S, S, A, B])(f: A => B, cb: Callback = Callback.empty): Callback =
      sa.modStateCB(i)(l.modify(f), cb)

    def setStateL[L[_, _, _, _], B](l: L[S, S, _, B])(b: B, cb: Callback = Callback.empty)(implicit L: MonocleSetter[L]): Callback =
      sa.modStateCB(i)(L.set(l)(b), cb)

    def setStateFnL[L[_, _, _, _], B](l: L[S, S, _, B], cb: Callback = Callback.empty)(implicit L: MonocleSetter[L]): B => Callback =
      setStateL(l)(_, cb)
  }

}
