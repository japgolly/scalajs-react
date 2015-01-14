package ghpages

import scala.scalajs.js
import japgolly.scalajs.react._, vdom.all._, ScalazReact._
import japgolly.scalajs.react.extra._

object ExtrasExamples {

  /**
   * This is the typical React timer example, modified to use OnUnmount.
   * (Also removed State in favour of just using Long directly.)
   */
  object OnUnmountExample {

    class Backend($: BackendScope[_, Long]) extends OnUnmount {     // Extends OnUnmount
                                                                    // Removed `var interval`
      def tick() = $.modState(_ + 1)

      def start() = {
        val i = js.timers.setInterval(1000)(tick())
        onUnmount(js.timers.clearInterval(i))                       // Use onUnmount here
      }
    }


    val Timer = ReactComponentB[Unit]("Timer")
      .initialState(0L)
      .backend(new Backend(_))
      .render((_,s,_) => div("Seconds elapsed: ", s))
      .componentDidMount(_.backend.start())
                                                                    // Removed componentWillUnmount() call
      .configure(OnUnmount.install)                                 // Register OnUnmount functionality
      .buildU
  }

  // ===================================================================================================================

  /**
   * This is the typical React timer example, modified to use SetInterval.
   * (Also removed State in favour of just using Long directly.)
   */
  object SetIntervalExample {

    class Backend extends SetInterval

    val Timer = ReactComponentB[Unit]("Timer")
      .initialState(0L)
      .backend(_ => new Backend)
      .render((_,s,_) => div("Seconds elapsed: ", s))
      .componentDidMount(c => c.backend.setInterval(c.modState(_ + 1), 1000))
      .configure(SetInterval.install)
      .buildU
  }
  
  // ===================================================================================================================

  /**
   * This is an example of using Listenable to receive external events that affect a component's state.
   */
  object ListenableExample {

    class Backend extends OnUnmount                     // Required it can unregister listener on unmount.

    def recv(i: Int) = ReactS.mod[Int](_ + i)           // External event handler.
                                                        // When an int is received, add to component state.
    val C = ReactComponentB[Listenable[Int]]("C")
      .initialState(0)
      .backend(_ => new Backend)
      .render((_,s,_) => div("Total: ", s))
      .configure(Listenable.installS(identity, recv))   // Listen to events when mounted.
      .build
  }
}
