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

  def listen[P, C <: Children, S, B <: OnUnmount, A](
      listenable: P => Listenable[A],
      makeListener: ScalaComponent.Lifecycle.ComponentDidMount[P, S, B] => A => Callback): ScalaComponent.Config[P, C, S, B] =
    OnUnmount.install[P, C, S, B] andThen (_.componentDidMount($ =>
      listenable($.props).register(makeListener($)) >>= $.backend.onUnmount))

  def listenToUnit[P, C <: Children, S, B <: OnUnmount](
      listenable: P => Listenable[Unit],
      makeListener: ScalaComponent.Lifecycle.ComponentDidMount[P, S, B] => Callback): ScalaComponent.Config[P, C, S, B] =
    listen[P, C, S, B, Unit](listenable, $ => _ => makeListener($))
}
