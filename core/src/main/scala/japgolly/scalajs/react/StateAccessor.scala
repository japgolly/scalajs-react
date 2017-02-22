package japgolly.scalajs.react

import japgolly.scalajs.react.internal.Effect
import ScalaComponent.Lifecycle
import StateAccessor._

/**
  * Type-classes that provide read and/or write access to state.
  */
object StateAccessor extends StateAccessorImplicits {

  trait Read[-I, F[_], S] {
    val state: I => F[S]
  }

  trait Write[-I, F[_], S] {
    val setStateCB: I => (S, Callback) => F[Unit]
    val modStateCB: I => ((S => S), Callback) => F[Unit]

    final def setState(i: I): S => F[Unit] = {
      val f = setStateCB(i)
      f(_, Callback.empty)
    }

    final def modState(i: I): (S => S) => F[Unit] = {
      val f = modStateCB(i)
      f(_, Callback.empty)
    }
  }

  type ReadWrite[-I, R[_], W[_], S] = Read[I, R, S] with Write[I, W, S]

  type ReadId[-I, S] = Read[I, Effect.Id, S]
  type ReadCB[-I, S] = Read[I, CallbackTo, S]

  type WriteId[-I, S] = Write[I, Effect.Id, S]
  type WriteCB[-I, S] = Write[I, CallbackTo, S]

  type ReadIdWriteId[-I, S] = ReadWrite[I, Effect.Id, Effect.Id, S]
  type ReadIdWriteCB[-I, S] = ReadWrite[I, Effect.Id, CallbackTo, S]
  type ReadCBWriteId[-I, S] = ReadWrite[I, CallbackTo, Effect.Id, S]
  type ReadCBWriteCB[-I, S] = ReadWrite[I, CallbackTo, CallbackTo, S]
}

trait StateAccessorImplicits2 {
  protected def castW[I, F[+_], S](w: Write[_, F, _]) = w.asInstanceOf[Write[I, F, S]]
  protected def castR[I, F[+_], S](w: Read[_, F, _]) = w.asInstanceOf[Read[I, F, S]]
  protected def castRW[I, R[+_], W[+_], S](w: ReadWrite[_, R, W, _]) = w.asInstanceOf[ReadWrite[I, R, W, S]]

  implicit def stateAccess[F[+_], S]: ReadWrite[StateAccess[F, S], F, F, S] = {
    type I = StateAccess[F, S]
    new Read[I, F, S] with Write[I, F, S] {
      override val state = (_: I).state
      override val setStateCB = (i: I) => i.setState(_, _)
      override val modStateCB = (i: I) => i.modState(_, _)
    }
  }

  private def newScalaLifecycleStateW[S]: WriteCB[Lifecycle.StateW[_, S, _], S] = {
    type I = Lifecycle.StateW[_, S, _]
    new Write[I, CallbackTo, S] {
      override val setStateCB = (i: I) => i.setState(_, _)
      override val modStateCB = (i: I) => i.modState(_, _)
    }
  }
  private[this] lazy val scalaLifecycleStateWInstance = newScalaLifecycleStateW[Any]
  implicit def scalaLifecycleStateW[S]: WriteCB[Lifecycle.StateW[_, S, _], S] = castW(scalaLifecycleStateWInstance)
}

trait StateAccessorImplicits1 extends StateAccessorImplicits2 {

  private[this] lazy val stateAccessImpureInstance = stateAccess[Effect.Id, Any]
  implicit def stateAccessImpure[S]: ReadIdWriteId[StateAccessImpure[S], S] = castRW(stateAccessImpureInstance)

  // Coercion: Lifecycle ReadIdWriteCB → ReadCBWriteCB
  private def newScalaLifecycleStateRWCB[S]: ReadCBWriteCB[Lifecycle.StateRW[_, S, _], S] = {
    type I = Lifecycle.StateRW[_, S, _]
    new Read[I, CallbackTo, S] with Write[I, CallbackTo, S] {
      override val state = (i: I) => CallbackTo(i.state)
      override val setStateCB = (i: I) => i.setState(_, _)
      override val modStateCB = (i: I) => i.modState(_, _)
    }
  }
  private[this] lazy val scalaLifecycleStateRWCBInstance = newScalaLifecycleStateRWCB[Any]
  implicit def scalaLifecycleStateRWCB[S]: ReadCBWriteCB[Lifecycle.StateRW[_, S, _], S] = castRW(scalaLifecycleStateRWCBInstance)
}

trait StateAccessorImplicits extends StateAccessorImplicits1 {

  private[this] lazy val stateAccessPureInstance = stateAccess[CallbackTo, Any]
  implicit def stateAccessPure[S]: ReadCBWriteCB[StateAccessPure[S], S] = castRW(stateAccessPureInstance)

  private def newScalaLifecycleStateRW[S]: ReadIdWriteCB[Lifecycle.StateRW[_, S, _], S] = {
    type I = Lifecycle.StateRW[_, S, _]
    new Read[I, Effect.Id, S] with Write[I, CallbackTo, S] {
      override val state = (_: I).state
      override val setStateCB = (i: I) => i.setState(_, _)
      override val modStateCB = (i: I) => i.modState(_, _)
    }
  }
  private[this] lazy val scalaLifecycleStateRWInstance = newScalaLifecycleStateRW[Any]
  implicit def scalaLifecycleStateRW[S]: ReadIdWriteCB[Lifecycle.StateRW[_, S, _], S] = castRW(scalaLifecycleStateRWInstance)
}
