package japgolly.scalajs.react.extras

import scalaz.~>
import scalaz.effect.IO
import japgolly.scalajs.react.{ReactComponentB, ComponentScopeM}
import japgolly.scalajs.react.ScalazReact._

/**
 * NOTE: This may be renamed / relocated / removed in future.
 */
trait Listenable [A] {
  def register(f: A => Unit): () => Unit
}

object Listenable {

  def install[P, S, B <: OnUnmount, A](f: P => Listenable[A], g: ComponentScopeM[P, S, B] => A => Unit) =
    OnUnmount.install compose ((_: ReactComponentB[P, S, B])
      .componentDidMount(s => s.backend onUnmountF f(s.props).register(g(s))))

  def installIO[P, S, B <: OnUnmount, A](f: P => Listenable[A], g: (ComponentScopeM[P, S, B], A) => IO[Unit]) =
    install[P, S, B, A](f, t => a => g(t, a).unsafePerformIO())

  def installS[P, S, B <: OnUnmount, M[_], A](f: P => Listenable[A], g: A => ReactST[M, S, Unit])(implicit M: M ~> IO) =
    installIO[P, S, B, A](f, (t, a) => t.runState(g(a)))

  def installF[P, S, B <: OnUnmount, M[_], A](f: P => Listenable[A], g: A => ReactST[M, S, Unit])(implicit M: M ~> IO, F: ChangeFilter[S]) =
    installIO[P, S, B, A](f, (t, a) => t.runStateF(g(a)))

}

trait Broadcaster[A] extends Listenable[A] {
  private var _listeners = List.empty[A => Unit]

  protected final def listeners = _listeners

  override def register(f: A => Unit) = {
    _listeners ::= f
    () => _listeners = _listeners.filterNot(_ == f)
  }

  protected def broadcast(a: A): Unit =
    listeners foreach (_(a))
}