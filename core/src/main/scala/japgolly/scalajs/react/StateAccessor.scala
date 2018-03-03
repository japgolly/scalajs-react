package japgolly.scalajs.react

import japgolly.scalajs.react.internal.{Effect, identityFn}
import ScalaComponent.Lifecycle
import StateAccessor._

/**
  * Type-classes that provide read and/or write access to state.
  *
  * The syntax is a little wonky for technical reasons.
  * For read access, it's `typeclass.state(i): F[S]`.
  * For write access, it's `typeclass(i).setState(...): F[Unit]`.
  */
object StateAccessor extends StateAccessorImplicits {

  trait Read[-I, F[_], S] {
    val state: I => F[S]
  }

  trait Write[-I, F[_], S] {
    val write: I => StateAccess.Write[F, S]
    @inline final def apply(i: I) = write(i)
  }

  type ReadWrite[-I, R[_], W[_], S] = Read[I, R, S] with Write[I, W, S]

  type ReadImpure[-I, S] = Read[I, Effect.Id, S]
  type ReadPure[-I, S] = Read[I, CallbackTo, S]

  type WriteImpure[-I, S] = Write[I, Effect.Id, S]
  type WritePure[-I, S] = Write[I, CallbackTo, S]

  type ReadWriteImpure[-I, S] = ReadWrite[I, Effect.Id, Effect.Id, S]
  type ReadWritePure[-I, S] = ReadWrite[I, CallbackTo, CallbackTo, S]

  type ReadImpureWritePure[-I, S] = ReadWrite[I, Effect.Id, CallbackTo, S]
  type ReadPureWriteImpure[-I, S] = ReadWrite[I, CallbackTo, Effect.Id, S]
}

trait StateAccessorImplicits2 {
  protected def castW[I, F[_], S](w: Write[_, F, _]) = w.asInstanceOf[Write[I, F, S]]
  protected def castR[I, F[_], S](w: Read[_, F, _]) = w.asInstanceOf[Read[I, F, S]]
  protected def castRW[I, R[_], W[_], S](w: ReadWrite[_, R, W, _]) = w.asInstanceOf[ReadWrite[I, R, W, S]]

  implicit def stateAccess[F[_], S]: ReadWrite[StateAccess[F, S], F, F, S] = {
    type I = StateAccess[F, S]
    new Read[I, F, S] with Write[I, F, S] {
      override val state = (_: I).state
      override val write = identityFn[I]
    }
  }

  private def newScalaLifecycleStateW[S]: WritePure[Lifecycle.StateW[_, S, _], S] = {
    type I = Lifecycle.StateW[_, S, _]
    new Write[I, CallbackTo, S] {
      override val write = identityFn[I]
    }
  }
  private[this] lazy val scalaLifecycleStateWInstance = newScalaLifecycleStateW[Any]
  implicit def scalaLifecycleStateW[S]: WritePure[Lifecycle.StateW[_, S, _], S] = castW(scalaLifecycleStateWInstance)
}

trait StateAccessorImplicits1 extends StateAccessorImplicits2 {

  private[this] lazy val stateAccessImpureInstance = stateAccess[Effect.Id, Any]
  implicit def stateAccessImpure[S]: ReadWriteImpure[StateAccessImpure[S], S] = castRW(stateAccessImpureInstance)

  // Coercion: Lifecycle ReadImpureWritePure → ReadPureWritePure
  private def newScalaLifecycleStateRWCB[S]: ReadWritePure[Lifecycle.StateRW[_, S, _], S] = {
    type I = Lifecycle.StateRW[_, S, _]
    new Read[I, CallbackTo, S] with Write[I, CallbackTo, S] {
      override val state = (i: I) => CallbackTo(i.state)
      override val write = identityFn[I]
    }
  }
  private[this] lazy val scalaLifecycleStateRWCBInstance = newScalaLifecycleStateRWCB[Any]
  implicit def scalaLifecycleStateRWCB[S]: ReadWritePure[Lifecycle.StateRW[_, S, _], S] = castRW(scalaLifecycleStateRWCBInstance)
}

trait StateAccessorImplicits extends StateAccessorImplicits1 {

  private[this] lazy val stateAccessPureInstance = stateAccess[CallbackTo, Any]
  implicit def stateAccessPure[S]: ReadWritePure[StateAccessPure[S], S] = castRW(stateAccessPureInstance)

  private def newScalaLifecycleStateRW[S]: ReadImpureWritePure[Lifecycle.StateRW[_, S, _], S] = {
    type I = Lifecycle.StateRW[_, S, _]
    new Read[I, Effect.Id, S] with Write[I, CallbackTo, S] {
      override val state = (_: I).state
      override val write = identityFn[I]
    }
  }
  private[this] lazy val scalaLifecycleStateRWInstance = newScalaLifecycleStateRW[Any]
  implicit def scalaLifecycleStateRW[S]: ReadImpureWritePure[Lifecycle.StateRW[_, S, _], S] = castRW(scalaLifecycleStateRWInstance)
}
