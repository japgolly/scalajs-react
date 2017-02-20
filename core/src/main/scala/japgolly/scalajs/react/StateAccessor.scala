package japgolly.scalajs.react

import japgolly.scalajs.react.internal.Effect

/**
  * Type-classes for abstracting over things that have state.
  */
object StateAccessor extends StateAccessorImplicits {

  sealed class WriteCB[-I, S](
      final val setStateCB: I => (S, Callback) => Callback,
      final val modStateCB: I => ((S => S), Callback) => Callback) {

    final val setState: I => S => Callback = i => {
      val f = setStateCB(i)
      f(_, Callback.empty)
    }

    final val modState: I => (S => S) => Callback = i => {
      val f = modStateCB(i)
      f(_, Callback.empty)
    }
  }

  final class ReadFWriteCB[F[+_], -I, S](
      val state: I => F[S],
      setStateCB: I => (S, Callback) => Callback,
      modStateCB: I => ((S => S), Callback) => Callback)
      extends WriteCB[I, S](setStateCB, modStateCB) {

    def toStateAccess(i: I)(implicit t: Effect.Trans[CallbackTo, F]): StateAccess[F, S] =
      if (t.to eq Effect.callbackInstance) {
        val fakeEv1 = =:=.tpEquals[Any].asInstanceOf[this.type =:= ReadCBWriteCB[I, S]]
        val fakeEv2 = fakeEv1.asInstanceOf[StateAccessPure[S] =:= StateAccess[F, S]]
        fakeEv2(toStateAccessPure(i)(fakeEv1))
      } else {
        val ss = setStateCB(i)
        val ms = modStateCB(i)
        StateAccess(state(i))((s, c) => t(ss(s, c)), (f, c) => t(ms(f, c)))(t.to)
      }

    def toStateAccessPure[II <: I](i: II)(implicit ev: this.type =:= ReadCBWriteCB[II, S]): StateAccessPure[S] = {
      val x = ev(this)
      val get = x.state(i) // force by-value
      StateAccess(get)(setStateCB(i), modStateCB(i))
    }
  }

  type ReadIdWriteCB[-I, S] = ReadFWriteCB[Effect.Id, I, S]
  type ReadCBWriteCB[-I, S] = ReadFWriteCB[CallbackTo, I, S]
}

// =====================================================================================================================
import StateAccessor.{WriteCB, ReadFWriteCB, ReadIdWriteCB, ReadCBWriteCB}

sealed trait StateAccessorImplicits1 {

  protected sealed trait X
  private def castW[I, S](w: WriteCB[_, _]) = w.asInstanceOf[WriteCB[I, S]]

  // WriteCB -- ScalaComponent.Lifecycle.StateW
  private[this] val _scalaLifecycleW = new WriteCB[ScalaComponent.Lifecycle.StateW[_, X, _], X](
    i => i.setState(_, _), i => i.modState(_, _))
  implicit def scalaLifecycleW[S]: WriteCB[ScalaComponent.Lifecycle.StateW[_, S, _], S] =
    castW(_scalaLifecycleW)
}

sealed trait StateAccessorImplicits2 extends StateAccessorImplicits1 {

  protected def castRW[F[+_], I, S](w: ReadFWriteCB[F, _, _]) = w.asInstanceOf[ReadFWriteCB[F, I, S]]

  // ReadCBWriteCB -- StateAccessPure
  private[this] val _mountedCB = new ReadFWriteCB[CallbackTo, StateAccessPure[X], X](
    _.state, i => i.setState(_, _), i => i.modState(_, _))
  implicit def mountedCB[S]: ReadCBWriteCB[StateAccessPure[S], S] =
    castRW(_mountedCB)
}

sealed trait StateAccessorImplicits3 extends StateAccessorImplicits2 {

  // ReadCBWriteCB -- StateAccessImpure
  private[this] lazy val _mountedIdCB = new ReadFWriteCB[CallbackTo, StateAccessImpure[X], X](
    i => CallbackTo(i.state),
    i => (s, cb) => Callback(i.setState(s, cb)),
    i => (f, cb) => Callback(i.modState(f, cb)))
  implicit def mountedIdCB[S]: ReadCBWriteCB[StateAccessImpure[S], S] =
    castRW(_mountedIdCB)
}

sealed trait StateAccessorImplicits extends StateAccessorImplicits3 {

  // ReadIdWriteCB -- StateAccessImpure
  private[this] val _mountedId = new ReadFWriteCB[Effect.Id, StateAccessImpure[X], X](
    _.state,
    i => (s, cb) => Callback(i.setState(s, cb)),
    i => (f, cb) => Callback(i.modState(f, cb)))
  implicit def mountedId[S]: ReadIdWriteCB[StateAccessImpure[S], S] =
    castRW(_mountedId)

  // ReadIdWriteCB -- ScalaComponent.Lifecycle.StateRW
  private[this] val _scalaLifecycleRW = new ReadFWriteCB[Effect.Id, ScalaComponent.Lifecycle.StateRW[_, X, _], X](
    _.state, i => i.setState(_, _), i => i.modState(_, _))
  implicit def scalaLifecycleRW[S]: ReadIdWriteCB[ScalaComponent.Lifecycle.StateRW[_, S, _], S] =
    castRW(_scalaLifecycleRW)
}
