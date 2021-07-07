package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.util.Effect.Dispatch
import org.scalajs.dom.Event
import org.scalajs.dom.raw.EventTarget
import scala.scalajs.js

class EventListenerF[F[_]] {
  import EventListenerF.{defaultTarget, OfEventType}

  def apply[E <: Event] = new OfEventType[F, E](true)

  /** See [[OfEventType.install()]]. */
  def install[G[_], P, C <: Children, S, B <: OnUnmountF[F], U <: UpdateSnapshot]
      (eventType : String,
       listener  : ScalaComponent.MountedPure[P, S, B] => G[Unit],
       target    : ScalaComponent.MountedImpure[P, S, B] => EventTarget = defaultTarget[P, S, B],
       useCapture: Boolean = false)
      (implicit F: Dispatch[F], G: Dispatch[G]): ScalaComponent.Config[P, C, S, B, U, U] =
    apply[Event].install[G, P, C, S, B, U](
      eventType,
      $ => { val cb = listener($); _ => cb },
      target,
      useCapture)

  /** See [[OfEventType.install()]]. */
  def install_[G[_], P, C <: Children, S, B <: OnUnmountF[F], U <: UpdateSnapshot]
      (eventType : String,
       listener  : G[Unit],
       target    : => EventTarget,
       useCapture: Boolean = false)
      (implicit F: Dispatch[F], G: Dispatch[G]): ScalaComponent.Config[P, C, S, B, U, U] =
    install[G, P, C, S, B, U](
      eventType,
      _ => listener,
      _ => target,
      useCapture)
}

object EventListenerF {

  def defaultTarget[P, S, B]: ScalaComponent.MountedImpure[P, S, B] => EventTarget =
    _.getDOMNode.asMounted().node

  def apply[F[_]] = new EventListenerF[F]

  final class OfEventType[F[_], E <: Event](private val _unused: Boolean) extends AnyVal {

    /**
     * Install an event listener when a component is mounted.
     * Automatically uninstalls the event listener when the component is unmounted.
     *
     * @param eventType A string representing the
     *                  <a href="https://developer.mozilla.org/en-US/docs/DOM/event.type">event type</a> to listen for.
     * @param useCapture If true, useCapture indicates that the user wishes to initiate capture.
     *                   After initiating capture, all events of the specified type will be dispatched to the registered
     *                   listener before being dispatched to any EventTarget beneath it in the DOM tree.
     *                   Events which are bubbling upward through the tree will not trigger a listener designated to use
     *                   capture.
     */
    def install[G[_], P, C <: Children, S, B <: OnUnmountF[F], U <: UpdateSnapshot]
        (eventType : String,
         listener  : ScalaComponent.MountedPure[P, S, B] => E => G[Unit],
         target    : ScalaComponent.MountedImpure[P, S, B] => EventTarget = defaultTarget[P, S, B],
         useCapture: Boolean = false)
        (implicit F: Dispatch[F], G: Dispatch[G]): ScalaComponent.Config[P, C, S, B, U, U] =
      OnUnmountF.install[F, P, C, S, B, U].andThen(_.componentDidMount { $ =>
        val et = target($.mountedImpure)
        val ge = listener($.mountedPure)
        val f: js.Function1[E, Unit] = e => G.dispatch(ge(e))
        val add = F.delay(et.addEventListener(eventType, f, useCapture))
        val del = F.delay(et.removeEventListener(eventType, f, useCapture))
        F.chain(add, $.backend.onUnmount(del))
      })
  }
}
