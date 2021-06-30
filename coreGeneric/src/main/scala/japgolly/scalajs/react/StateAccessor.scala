package japgolly.scalajs.react

import japgolly.scalajs.react.StateAccessor._
import japgolly.scalajs.react.component.builder.Lifecycle
import japgolly.scalajs.react.internal.CoreGeneral._
import japgolly.scalajs.react.util.DefaultEffects.{Async => DA, Sync => DS}
import japgolly.scalajs.react.util.Effect._
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
    def withReadEffect[G[_]](implicit F: UnsafeSync[F], G: UnsafeSync[G]): Read[I, G, S]
  }

  trait Write[-I, F[_], A[_], S] { self =>
    val write: I => StateAccess.Write[F, A, S]
    @inline final def apply(i: I) = write(i)

    def withWriteEffect[G[_]](implicit F: UnsafeSync[F], G: UnsafeSync[G]): Write[I, G, A, S] =
      G.subst[F, ({ type L[E[_]] = Write[I, E, A, S] })#L](this) {
        Write(self.write(_).withEffect[G])
      }

    def withAsyncEffect[G[_]](implicit A: Async[A], G: Async[G]): Write[I, F, G, S] =
      G.subst[A, ({ type L[E[_]] = Write[I, F, E, S] })#L](this) {
        Write(self.write(_).withAsyncEffect[G])
      }
  }

  trait ReadWrite[-I, R[_], W[_], A[_], S] extends Read[I, R, S] with Write[I, W, A, S] { self =>
    override def withReadEffect[G[_]](implicit R: UnsafeSync[R], G: UnsafeSync[G]): ReadWrite[I, G, W, A, S] =
      G.subst[R, ({ type L[E[_]] = ReadWrite[I, E, W, A, S] })#L](this) {
        ReadWrite[I, G, W, A, S](G.transSyncFn1(self.state), self.write)
      }

    override def withWriteEffect[G[_]](implicit W: UnsafeSync[W], G: UnsafeSync[G]): ReadWrite[I, R, G, A, S] =
      G.subst[W, ({ type L[E[_]] = ReadWrite[I, R, E, A, S] })#L](this) {
        ReadWrite(self.state, self.write(_).withEffect[G])
      }

    override def withAsyncEffect[G[_]](implicit A: Async[A], G: Async[G]): ReadWrite[I, R, W, G, S] =
      G.subst[A, ({ type L[E[_]] = ReadWrite[I, R, W, E, S] })#L](this) {
        ReadWrite(self.state, self.write(_).withAsyncEffect[G])
      }
  }

  def Read[I, F[_], S](r: I => F[S]): Read[I, F, S] =
    new Read[I, F, S] {
      override val state = r
      override def withReadEffect[G[_]](implicit F: UnsafeSync[F], G: UnsafeSync[G]) =
        G.subst[F, ({ type L[E[_]] = Read[I, E, S] })#L](this) {
          Read[I, G, S](G.transSyncFn1(r))
        }
    }

  def Write[I, F[_], A[_], S](w: I => StateAccess.Write[F, A, S]): Write[I, F, A, S] =
    new Write[I, F, A, S] {
      override val write = w
    }

  def ReadWrite[I, R[_], W[_], A[_], S](r: I => R[S], w: I => StateAccess.Write[W, A, S]): ReadWrite[I, R, W, A, S] =
    new ReadWrite[I, R, W, A, S] {
      override val state = r
      override val write = w
    }

  type ReadImpure[-I, S] = Read[I, Id, S]
  type ReadPure[-I, S] = Read[I, DS, S]

  type WriteImpure[-I, S] = Write[I, Id, DA, S]
  type WritePure[-I, S] = Write[I, DS, DA, S]

  type ReadWriteImpure[-I, S] = ReadWrite[I, Id, Id, DA, S]
  type ReadWritePure[-I, S] = ReadWrite[I, DS, DS, DA, S]

  type ReadImpureWritePure[-I, S] = ReadWrite[I, Id, DS, DA, S]
  type ReadPureWriteImpure[-I, S] = ReadWrite[I, DS, Id, DA, S]
}

trait StateAccessorImplicits2 {
  protected def castW[I, F[_], A[_], S](w: Write[_, F, A, _]) = w.asInstanceOf[Write[I, F, A, S]]
  protected def castR[I, F[_], S](w: Read[_, F, _]) = w.asInstanceOf[Read[I, F, S]]
  protected def castRW[I, R[_], W[_], A[_], S, X](w: ReadWrite[Nothing, R, W, A, X]) = w.asInstanceOf[ReadWrite[I, R, W, A, S]]

  implicit def stateAccess[F[_], A[_], S]: ReadWrite[StateAccess[F, A, S], F, F, A, S] = {
    type I = StateAccess[F, A, S]
    ReadWrite[I, F, F, A, S](_.state, identityFn)
  }

  private def newScalaLifecycleStateW[S]: WritePure[Lifecycle.StateW[_, S, _], S] = {
    type I = Lifecycle.StateW[_, S, _]
    Write[I, DS, DA, S](identityFn)
  }
  private[this] lazy val scalaLifecycleStateWInstance = newScalaLifecycleStateW[Any]
  implicit def scalaLifecycleStateW[S]: WritePure[Lifecycle.StateW[_, S, _], S] = castW(scalaLifecycleStateWInstance)
}

trait StateAccessorImplicits1 extends StateAccessorImplicits2 {

  private[this] lazy val stateAccessImpureInstance = stateAccess[Id, DA, Any]
  implicit def stateAccessImpure[S]: ReadWriteImpure[StateAccessImpure[S], S] = castRW(stateAccessImpureInstance)

  // Coercion: Lifecycle ReadImpureWritePure → ReadPureWritePure
  private def newScalaLifecycleStateRWCB[S]: ReadWritePure[Lifecycle.StateRW[_, S, _], S] = {
    type I = Lifecycle.StateRW[_, S, _]
    ReadWrite[I, DS, DS, DA, S](i => DS.pure(i.state), identityFn)
  }
  private[this] lazy val scalaLifecycleStateRWCBInstance = newScalaLifecycleStateRWCB[Any]
  implicit def scalaLifecycleStateRWCB[S]: ReadWritePure[Lifecycle.StateRW[_, S, _], S] = castRW(scalaLifecycleStateRWCBInstance)
}

trait StateAccessorImplicits extends StateAccessorImplicits1 {

  private[this] lazy val stateAccessPureInstance = stateAccess[DS, DA, Any]
  implicit def stateAccessPure[S]: ReadWritePure[StateAccessPure[S], S] = castRW(stateAccessPureInstance)

  private def newScalaLifecycleStateRW[S]: ReadImpureWritePure[Lifecycle.StateRW[_, S, _], S] = {
    type I = Lifecycle.StateRW[_, S, _]
    ReadWrite[I, Id, DS, DA, S](_.state, identityFn)
  }
  private[this] lazy val scalaLifecycleStateRWInstance = newScalaLifecycleStateRW[Any]
  implicit def scalaLifecycleStateRW[S]: ReadImpureWritePure[Lifecycle.StateRW[_, S, _], S] = castRW(scalaLifecycleStateRWInstance)
}
