package japgolly.scalajs.react.extra

import japgolly.scalajs.react._

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
  def register(listener: A => Callback): CallbackTo[Callback]
}

object Listenable {

  def listen[P, C <: Children, S, B <: OnUnmount, U <: UpdateSnapshot, A](
      listenable: P => Listenable[A],
      makeListener: ScalaComponent.Lifecycle.ComponentDidMount[P, S, B] => A => Callback): ScalaComponent.Config[P, C, S, B, U, U] =
    OnUnmount.install[P, C, S, B, U] andThen (
      _.componentDidMount($ => listenable($.props).register(makeListener($)) >>= $.backend.onUnmount))

  def listenToUnit[P, C <: Children, S, B <: OnUnmount, U <: UpdateSnapshot](
      listenable: P => Listenable[Unit],
      makeListener: ScalaComponent.Lifecycle.ComponentDidMount[P, S, B] => Callback): ScalaComponent.Config[P, C, S, B, U, U] =
    listen[P, C, S, B, U, Unit](listenable, $ => _ => makeListener($))
}
