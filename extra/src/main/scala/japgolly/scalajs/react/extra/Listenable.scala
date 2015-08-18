package japgolly.scalajs.react.extra

import scalaz.~>
import japgolly.scalajs.react._
import japgolly.scalajs.react.ScalazReact._

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

  def install[P, S, B <: OnUnmount, N <: TopNode, A](f: P => Listenable[A], g: ComponentScopeM[P, S, B, N] => A => Callback) =
    OnUnmount.install[P, S, B, N] andThen (_.componentDidMount($ =>
      f($.props).register(g($)) >>= $.backend.onUnmount))

  def installU[P, S, B <: OnUnmount, N <: TopNode](f: P => Listenable[Unit], g: ComponentScopeM[P, S, B, N] => Callback) =
    install[P, S, B, N, Unit](f, $ => _ => g($))

  // TODO Remove Scalaz from extra, move these ↓ to scalaz module

//  def installIO[P, S, B <: OnUnmount, N <: TopNode, A](f: P => Listenable[A], g: (ComponentScopeM[P, S, B, N], A) => IO[Unit]) =
//    install[P, S, B, N, A](f, t => a => g(t, a).unsafePerformIO())

  def installS[P, S, B <: OnUnmount, N <: TopNode, M[_], A](f: P => Listenable[A], g: A => ReactST[M, S, Unit])(implicit M: M ~> CallbackTo) =
    install[P, S, B, N, A](f, $ => a => $.runState(g(a)))

  def installSF[P, S, B <: OnUnmount, N <: TopNode, M[_], A](f: P => Listenable[A], g: A => ReactST[M, S, Unit])(implicit M: M ~> CallbackTo, F: ChangeFilter[S]) =
    install[P, S, B, N, A](f, $ => a => $.runStateF(g(a)))
}
