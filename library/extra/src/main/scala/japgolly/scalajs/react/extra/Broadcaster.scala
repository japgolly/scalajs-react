package japgolly.scalajs.react.extra

import japgolly.scalajs.react.util.DefaultEffects

/** Implementation of `Listener`.
  * Subclasses can broadcast data of type A via the `broadcast` method.
  */
trait BroadcasterF[F[_], A] extends ListenableF[F, A] { self =>
  import self.{listenableEffect => F}

  /** (A => F[Unit], Unit) instead of (A => F[Unit]) because unregister is tied to a specific registration, rather
    * than the listener fn itself. This allows users to register the same listener multiple times and each
    * registration is guaranteed to only correspond to the single registration.
    */
  private var _listeners = List.empty[(A => F[Unit], Unit)]

  protected final def listenerIterator: Iterator[A => F[Unit]] =
    _listeners.iterator.map(_._1)

  override def register(listener: A => F[Unit]) = F.delay {
    val record = (listener, ())
    _listeners ::= record
    F.delay { _listeners = _listeners.filter(_ ne record) }
  }

  protected def broadcast(a: A): F[Unit] =
    F.traverse_(_listeners)(_._1(a))
}

// =====================================================================================================================

trait Broadcaster[A] extends BroadcasterF[DefaultEffects.Sync, A] with Listenable[A]
