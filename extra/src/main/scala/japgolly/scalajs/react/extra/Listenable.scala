package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import CompScope.DuringCallbackM

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

  def install[P, S, B <: OnUnmount, N <: TopNode, A](f: P => Listenable[A], g: DuringCallbackM[P, S, B, N] => A => Callback) =
    OnUnmount.install[P, S, B, N] andThen (_.componentDidMount($ =>
      f($.props).register(g($)) >>= $.backend.onUnmount))

  def installU[P, S, B <: OnUnmount, N <: TopNode](f: P => Listenable[Unit], g: DuringCallbackM[P, S, B, N] => Callback) =
    install[P, S, B, N, Unit](f, $ => _ => g($))
}
