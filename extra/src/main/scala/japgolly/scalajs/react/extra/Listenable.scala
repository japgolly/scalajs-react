package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.util.DefaultEffects._
import japgolly.scalajs.react.util.Effect

/**
 * External entities can register with this to listen (receive) data of type A.
 *
 * Install in `ScalaComponent.build` via `.configure(Listenable.listen)`.
 */
trait Listenable[A] {

  /**
   * Register a listener.
   *
   * @param listener The listener/consumer. A procedure that receives data of type A.
   * @return A procedure to unregister the given listener.
   */
  def register(listener: A => Sync[Unit]): Sync[Sync[Unit]]
}

object Listenable {

  def listen[F[_], P, C <: Children, S, B <: OnUnmount, U <: UpdateSnapshot, A](
      listenable: P => Listenable[A],
      makeListener: ScalaComponent.Lifecycle.ComponentDidMount[Sync, Async, P, S, B] => A => F[Unit])
     (implicit F: Effect.Sync[F]): ScalaComponent.Config[P, C, S, B, U, U] =
    OnUnmount.install[P, C, S, B, U].andThen(
      _.componentDidMount { $ =>
        val f = listenable($.props).register(a => Sync.transSync(makeListener($)(a)))
        Sync.flatMap(f)(x => Sync.transSync($.backend.onUnmount(x)))
      }
    )

  def listenToUnit[F[_]: Effect.Sync, P, C <: Children, S, B <: OnUnmount, U <: UpdateSnapshot](
      listenable: P => Listenable[Unit],
      makeListener: ScalaComponent.Lifecycle.ComponentDidMount[Sync, Async, P, S, B] => F[Unit]): ScalaComponent.Config[P, C, S, B, U, U] =
    listen[F, P, C, S, B, U, Unit](listenable, $ => _ => makeListener($))
}
