package japgolly.scalajs.react.extra

import scalaz.~>
import scalaz.effect.IO
import japgolly.scalajs.react.{TopNode, ReactComponentB, ComponentScopeM}
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
  def register(f: A => Unit): () => Unit
}

object Listenable {

  def install[P, S, B <: OnUnmount, N <: TopNode, A](f: P => Listenable[A], g: ComponentScopeM[P, S, B, N] => A => Unit) =
    OnUnmount.install compose ((_: ReactComponentB[P, S, B, N])
      .componentDidMount(s => s.backend onUnmountF f(s.props).register(g(s))))

  def installIO[P, S, B <: OnUnmount, N <: TopNode, A](f: P => Listenable[A], g: (ComponentScopeM[P, S, B, N], A) => IO[Unit]) =
    install[P, S, B, N, A](f, t => a => g(t, a).unsafePerformIO())

  def installS[P, S, B <: OnUnmount, N <: TopNode, M[_], A](f: P => Listenable[A], g: A => ReactST[M, S, Unit])(implicit M: M ~> IO) =
    installIO[P, S, B, N, A](f, (t, a) => t.runState(g(a)))

  def installSF[P, S, B <: OnUnmount, N <: TopNode, M[_], A](f: P => Listenable[A], g: A => ReactST[M, S, Unit])(implicit M: M ~> IO, F: ChangeFilter[S]) =
    installIO[P, S, B, N, A](f, (t, a) => t.runStateF(g(a)))

}