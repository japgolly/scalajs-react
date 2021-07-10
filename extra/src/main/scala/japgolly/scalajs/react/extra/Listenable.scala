package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.util.DefaultEffects
import japgolly.scalajs.react.util.Effect.{Dispatch, Sync}

/** External entities can register with this to listen (receive) data of type A.
  *
  * Install in `ScalaComponent.build` via `.configure(Listenable.listen)`.
  */
trait ListenableF[F[_], A] { self =>
  protected def listenableEffect: Sync[F]
  import self.{listenableEffect => F}

  /** Register a listener.
    *
    * @param listener The listener/consumer. A procedure that receives data of type A.
    * @return A procedure to unregister the given listener.
    */
  def register(listener: A => F[Unit]): F[F[Unit]]

  final def registerF[G[_]](listener: A => G[Unit])(implicit G: Dispatch[G]): F[F[Unit]] =
    register(F.transDispatchFn1(listener))
}

object ListenableF {

  def listen[F[_], G[_], P, C <: Children, S, B <: OnUnmountF[F], U <: UpdateSnapshot, A](
      listenable: P => ListenableF[F, A],
      makeListener: ScalaComponent.Lifecycle.ComponentDidMount[P, S, B] => A => G[Unit])
     (implicit F: Dispatch[F], G: Dispatch[G]): ScalaComponent.Config[P, C, S, B, U, U] =
    OnUnmountF.install[F, P, C, S, B, U].andThen(
      _.componentDidMount { $ =>
        val f = listenable($.props).registerF(a => makeListener($)(a))
        F.flatMap(f)($.backend.onUnmount(_))
      }
    )

  def listenToUnit[F[_]: Dispatch, G[_]: Dispatch, P, C <: Children, S, B <: OnUnmountF[F], U <: UpdateSnapshot](
      listenable: P => ListenableF[F, Unit],
      makeListener: ScalaComponent.Lifecycle.ComponentDidMount[P, S, B] => G[Unit]): ScalaComponent.Config[P, C, S, B, U, U] =
    listen[F, G, P, C, S, B, U, Unit](listenable, $ => _ => makeListener($))
}

// =====================================================================================================================

trait Listenable[A] extends ListenableF[DefaultEffects.Sync, A] {
  override protected def listenableEffect: Sync[DefaultEffects.Sync] =
    DefaultEffects.Sync
}

object Listenable {
  import DefaultEffects.{Sync => s} // for the implicit

  def listen[F[_]: Dispatch, P, C <: Children, S, B <: OnUnmount, U <: UpdateSnapshot, A](
      listenable: P => Listenable[A],
      makeListener: ScalaComponent.Lifecycle.ComponentDidMount[P, S, B] => A => F[Unit])
     : ScalaComponent.Config[P, C, S, B, U, U] =
    ListenableF.listen(listenable, makeListener)

  def listenToUnit[F[_]: Dispatch, P, C <: Children, S, B <: OnUnmount, U <: UpdateSnapshot](
      listenable: P => Listenable[Unit],
      makeListener: ScalaComponent.Lifecycle.ComponentDidMount[P, S, B] => F[Unit]): ScalaComponent.Config[P, C, S, B, U, U] =
    listen[F, P, C, S, B, U, Unit](listenable, $ => _ => makeListener($))
}
