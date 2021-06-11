package japgolly.scalajs.react

import japgolly.scalajs.react.StateAccessor._
import japgolly.scalajs.react.component.builder.Lifecycle
import japgolly.scalajs.react.util.DefaultEffects._
import japgolly.scalajs.react.util.Effect.Id
import japgolly.scalajs.react.util.Util.identityFn

/** Type-classes that provide read and/or write access to state.
  *
  * The syntax is a little wonky for technical reasons.
  * For read access, it's `typeclass.state(i): F[S]`.
  * For write access, it's `typeclass(i).setState(...): F[Unit]`.
  */
object StateAccessor extends StateAccessorImplicits {

  trait Read[-I, F[_], S] {
    val state: I => F[S]
  }

  trait Write[-I, F[_], A[_], S] {
    val write: I => StateAccess.Write[F, A, S]
    @inline final def apply(i: I) = write(i)
  }

  type ReadWrite[-I, R[_], W[_], A[_], S] = Read[I, R, S] with Write[I, W, A, S]

  type ReadImpure[-I, S] = Read[I, Id, S]
  type ReadPure[-I, S] = Read[I, Sync, S]

  type WriteImpure[-I, S] = Write[I, Id, Async, S]
  type WritePure[-I, S] = Write[I, Sync, Async, S]

  type ReadWriteImpure[-I, S] = ReadWrite[I, Id, Id, Async, S]
  type ReadWritePure[-I, S] = ReadWrite[I, Sync, Sync, Async, S]

  type ReadImpureWritePure[-I, S] = ReadWrite[I, Id, Sync, Async, S]
  type ReadPureWriteImpure[-I, S] = ReadWrite[I, Sync, Id, Async, S]
}

trait StateAccessorImplicits2 {
  protected def castW[I, F[_], A[_], S](w: Write[_, F, A, _]) = w.asInstanceOf[Write[I, F, A, S]]
  protected def castR[I, F[_], S](w: Read[_, F, _]) = w.asInstanceOf[Read[I, F, S]]
  protected def castRW[I, R[_], W[_], A[_], S, X](w: ReadWrite[Nothing, R, W, A, X]) = w.asInstanceOf[ReadWrite[I, R, W, A, S]]

  implicit def stateAccess[F[_], A[_], S]: ReadWrite[StateAccess[F, A, S], F, F, A, S] = {
    type I = StateAccess[F, A, S]
    new Read[I, F, S] with Write[I, F, A, S] {
      override val state = (_: I).state
      override val write = identityFn[I]
    }
  }

  private def newScalaLifecycleStateW[S]: WritePure[Lifecycle.StateW[Sync, Async, _, S, _], S] = {
    type I = Lifecycle.StateW[Sync, Async, _, S, _]
    new Write[I, Sync, Async, S] {
      override val write = identityFn[I]
    }
  }
  private[this] lazy val scalaLifecycleStateWInstance = newScalaLifecycleStateW[Any]
  implicit def scalaLifecycleStateW[S]: WritePure[Lifecycle.StateW[Sync, Async, _, S, _], S] = castW(scalaLifecycleStateWInstance)
}

trait StateAccessorImplicits1 extends StateAccessorImplicits2 {

  private[this] lazy val stateAccessImpureInstance = stateAccess[Id, Async, Any]
  implicit def stateAccessImpure[S]: ReadWriteImpure[StateAccessImpure[S], S] = castRW(stateAccessImpureInstance)

  // Coercion: Lifecycle ReadImpureWritePure → ReadPureWritePure
  private def newScalaLifecycleStateRWCB[S]: ReadWritePure[Lifecycle.StateRW[Sync, Async, _, S, _], S] = {
    type I = Lifecycle.StateRW[Sync, Async, _, S, _]
    new Read[I, Sync, S] with Write[I, Sync, Async, S] {
      override val state = (i: I) => sync.pure(i.state)
      override val write = identityFn[I]
    }
  }
  private[this] lazy val scalaLifecycleStateRWCBInstance = newScalaLifecycleStateRWCB[Any]
  implicit def scalaLifecycleStateRWCB[S]: ReadWritePure[Lifecycle.StateRW[Sync, Async, _, S, _], S] = castRW(scalaLifecycleStateRWCBInstance)
}

trait StateAccessorImplicits extends StateAccessorImplicits1 {

  private[this] lazy val stateAccessPureInstance = stateAccess[Sync, Async, Any]
  implicit def stateAccessPure[S]: ReadWritePure[StateAccessPure[S], S] = castRW(stateAccessPureInstance)

  private def newScalaLifecycleStateRW[S]: ReadImpureWritePure[Lifecycle.StateRW[Sync, Async, _, S, _], S] = {
    type I = Lifecycle.StateRW[Sync, Async, _, S, _]
    new Read[I, Id, S] with Write[I, Sync, Async, S] {
      override val state = (_: I).state
      override val write = identityFn[I]
    }
  }
  private[this] lazy val scalaLifecycleStateRWInstance = newScalaLifecycleStateRW[Any]
  implicit def scalaLifecycleStateRW[S]: ReadImpureWritePure[Lifecycle.StateRW[Sync, Async, _, S, _], S] = castRW(scalaLifecycleStateRWInstance)
}
