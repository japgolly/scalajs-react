package japgolly.scalajs.react.extra

import japgolly.scalajs.react.util.DefaultEffects.Sync

/**
 * Implementation of `Listener`.
 * Subclasses can broadcast data of type A via the `broadcast` method.
 */
trait Broadcaster[A] extends Listenable[A] {

  /** (A => Sync[Unit], Unit) instead of (A => Sync[Unit]) because unregister is tied to a specific registration, rather
    * than the listener fn itself. This allows users to register the same listener multiple times and each
    * registration is guaranteed to only correspond to the single registration.
    */
  private var _listeners = List.empty[(A => Sync[Unit], Unit)]

  protected final def listenerIterator: Iterator[A => Sync[Unit]] =
    _listeners.iterator.map(_._1)

  override def register(listener: A => Sync[Unit]) = Sync.delay {
    val record = (listener, ())
    _listeners ::= record
    Sync.delay { _listeners = _listeners.filter(_ ne record) }
  }

  protected def broadcast(a: A): Sync[Unit] =
    Sync.traverse_(_listeners)(_._1(a))
}
