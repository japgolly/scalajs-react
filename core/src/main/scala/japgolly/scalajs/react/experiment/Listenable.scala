package japgolly.scalajs.react.experiment

import japgolly.scalajs.react.{ReactComponentB, ComponentScopeM}

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