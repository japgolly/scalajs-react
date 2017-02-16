package japgolly.scalajs.react.extra

import japgolly.scalajs.react._

/**
 * External entities can register with this to listen (receive) data of type A.
 *
 * Install in `ReactComponentB` via `.configure(Listenable.install...)`.
 */
trait Listenable[A] {

  /**
   * Register a listener.
   *
   * @param f The listener. A procedure that receives data of type A.
   * @return A procedure to unregister the given listener.
   */
  def register(f: A => Callback): CallbackTo[Callback]
}

object Listenable {

  def install[P, C <: Children, S, B <: OnUnmount, A](f: P => Listenable[A],
                                                      g: ScalaComponent.Lifecycle.ComponentDidMount[P, S, B] => A => Callback): ScalaComponentConfig[P, C, S, B] =
    OnUnmount.install[P, C, S, B] andThen (_.componentDidMount($ =>
      f($.props).register(g($)) >>= $.backend.onUnmount))

  def installU[P, C <: Children, S, B <: OnUnmount](f: P => Listenable[Unit],
                                                    g: ScalaComponent.Lifecycle.ComponentDidMount[P, S, B] => Callback): ScalaComponentConfig[P, C, S, B] =
    install[P, C, S, B, Unit](f, $ => _ => g($))
}
