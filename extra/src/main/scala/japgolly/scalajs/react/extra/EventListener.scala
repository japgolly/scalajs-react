package japgolly.scalajs.react.extra

import org.scalajs.dom.raw.EventTarget
import org.scalajs.dom.Event
import scala.scalajs.js
import scalaz.effect.IO
import japgolly.scalajs.react._

object EventListener {

  def apply[E <: Event] = new OfEventType[E](true)

  def defaultTarget[P, S, B, N <: TopNode]: ComponentScopeM[P,S,B,N] => EventTarget =
    _.getDOMNode()

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
    def install[P, S, B <: OnUnmount, N <: TopNode](eventType : String,
                                                    listener  : ComponentScopeM[P,S,B,N] => E => Unit,
                                                    target    : ComponentScopeM[P,S,B,N] => EventTarget = defaultTarget[P,S,B,N],
                                                    useCapture: Boolean = false) =
      OnUnmount.install[P,S,B,N] andThen (_.componentDidMount { $ =>
        val et = target($)
        val fe = listener($)
        val f: js.Function1[E, Unit] = (e: E) => fe(e)
        et.addEventListener(eventType, f, useCapture)
        $.backend.onUnmount(et.removeEventListener(eventType, f, useCapture))
      })

    /** See [[install()]]. */
    def installIO[P, S, B <: OnUnmount, N <: TopNode](eventType : String,
                                                      listener  : ComponentScopeM[P,S,B,N] => E => IO[Unit],
                                                      target    : ComponentScopeM[P,S,B,N] => EventTarget = defaultTarget[P,S,B,N],
                                                      useCapture: Boolean = false) =
      install[P,S,B,N](
        eventType,
        $ => { val f = listener($); e => f(e).unsafePerformIO() },
        target, useCapture)
  }

  /** See [[OfEventType.install()]]. */
  def install[P, S, B <: OnUnmount, N <: TopNode](eventType : String,
                                                  listener  : ComponentScopeM[P,S,B,N] => () => Unit,
                                                  target    : ComponentScopeM[P,S,B,N] => EventTarget = defaultTarget[P,S,B,N],
                                                  useCapture: Boolean = false) =
    EventListener[Event].install[P,S,B,N](
      eventType,
      $ => { val f = listener($); _ => f() },
      target, useCapture)

  /** See [[OfEventType.install()]]. */
  def installIO[P, S, B <: OnUnmount, N <: TopNode](eventType : String,
                                                    listener  : ComponentScopeM[P,S,B,N] => IO[Unit],
                                                    target    : ComponentScopeM[P,S,B,N] => EventTarget = defaultTarget[P,S,B,N],
                                                    useCapture: Boolean = false) =
    EventListener[Event].installIO[P,S,B,N](
      eventType,
      Function const listener(_),
      target, useCapture)

}
