package japgolly.scalajs.react.extra

import japgolly.scalajs.react.{CallbackTo, Callback}

/**
 * Implementation of `Listener`.
 * Subclasses can broadcast data of type A via the `broadcast` method.
 */
trait Broadcaster[A] extends Listenable[A] {
  private var _listeners = List.empty[(A => Callback, Unit)]

  @deprecated("Use listenerIterator", "1.2.0")
  protected final def listeners: List[A => Callback] =
    _listeners.map(_._1)

  protected final def listenerIterator: Iterator[A => Callback] =
    _listeners.iterator.map(_._1)

  override def register(listener: A => Callback) = CallbackTo {
    val record = (listener, ())
    _listeners ::= record
    Callback(_listeners = _listeners.filter(_ eq record))
  }

  protected def broadcast(a: A): Callback =
    Callback.traverse(_listeners)(_._1(a))
}
