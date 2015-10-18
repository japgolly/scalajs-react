package japgolly.scalajs.react.extra

import japgolly.scalajs.react.{CallbackTo, Callback}

/**
 * Implementation of `Listener`.
 * Subclasses can broadcast data of type A via the `broadcast` method.
 */
trait Broadcaster[A] extends Listenable[A] {
  private var _listeners = List.empty[A => Callback]

  protected final def listeners = _listeners

  override def register(f: A => Callback) = CallbackTo {
    _listeners ::= f
    Callback(_listeners = _listeners.filter(_ ne f))
  }

  protected def broadcast(a: A): Callback =
    Callback(listeners foreach (_(a).runNow()))
}
