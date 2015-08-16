package japgolly.scalajs.react.extra

/**
 * Implementation of `Listener`.
 * Subclasses can broadcast data of type A via the `broadcast` method.
 */
trait Broadcaster[A] extends Listenable[A] {
  private var _listeners = List.empty[A => Unit]

  protected final def listeners = _listeners

  override def register(f: A => Unit) = {
    _listeners ::= f
    () => _listeners = _listeners.filter(_ ne f)
  }

  protected def broadcast(a: A): Unit =
    listeners foreach (_(a))
}
