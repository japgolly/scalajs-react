package japgolly.scalajs.react.extra

import org.scalajs.dom.raw.EventTarget
import org.scalajs.dom.Event
import scala.scalajs.js
import japgolly.scalajs.react._

object EventListener {

  def apply[E <: Event] = new OfEventType[E](true)

  def defaultTarget[P, S, B]: ScalaComponent.MountedImpure[P, S, B] => EventTarget =
    _.getDOMNode.asMounted().node

  final class OfEventType[E <: Event](private val _unused: Boolean) extends AnyVal {

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
    def install[P, C <: Children, S, B <: OnUnmount, U <: UpdateSnapshot]
        (eventType : String,
         listener  : ScalaComponent.MountedPure[P, S, B] => E => Callback,
         target    : ScalaComponent.MountedImpure[P, S, B] => EventTarget = defaultTarget[P, S, B],
         useCapture: Boolean = false) =
      OnUnmount.install[P, C, S, B, U] andThen (_.componentDidMount { $ =>
        val et = target($.mountedImpure)
        val fe = listener($.mountedPure)
        val f: js.Function1[E, Unit] = (e: E) => fe(e).runNow()
        val add = Callback(et.addEventListener(eventType, f, useCapture))
        val del = Callback(et.removeEventListener(eventType, f, useCapture))
        add >> $.backend.onUnmount(del)
      })

  } //end class

  /** See [[OfEventType.install()]]. */
  def install[P, C <: Children, S, B <: OnUnmount, U <: UpdateSnapshot]
      (eventType : String,
       listener  : ScalaComponent.MountedPure[P, S, B] => Callback,
       target    : ScalaComponent.MountedImpure[P, S, B] => EventTarget = defaultTarget[P, S, B],
       useCapture: Boolean = false) =
    EventListener[Event].install[P, C, S, B, U](
      eventType,
      $ => { val cb = listener($); _ => cb },
      target, useCapture)
}
