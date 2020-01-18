package japgolly.scalajs.react.extra

import japgolly.scalajs.react.{CallbackTo, Callback}

/**
 * Implementation of `Listener`.
 * Subclasses can broadcast data of type A via the `broadcast` method.
 */
trait Broadcaster[A] extends Listenable[A] {

  /** (A => Callback, Unit) instead of (A => Callback) because unregister is tied to a specific registration, rather
    * than the listener fn itself. This allows users to register the same listener multiple times and each
    * registration is guaranteed to only correspond to the single registration.
    */
  private var _listeners = List.empty[(A => Callback, Unit)]

  @deprecated("Use listenerIterator", "1.2.0")
  protected final def listeners: List[A => Callback] =
    _listeners.map(_._1)

  protected final def listenerIterator: Iterator[A => Callback] =
    _listeners.iterator.map(_._1)

  override def register(listener: A => Callback) = CallbackTo {
    val record = (listener, ())
    _listeners ::= record
    Callback({_listeners = _listeners.filter(_ ne record)})
  }

  protected def broadcast(a: A): Callback =
    Callback.traverse(_listeners)(_._1(a))
}
