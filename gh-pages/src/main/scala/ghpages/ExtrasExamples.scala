package ghpages

import scala.concurrent.duration._
import scala.scalajs.js
import japgolly.scalajs.react._, vdom.all._, ScalazReact._
import japgolly.scalajs.react.extra._

object ExtrasExamples {

  /**
   * This is the typical React timer example, modified to use OnUnmount.
   * (Also removed State in favour of just using Long directly.)
   */
  object OnUnmountExample {

    class Backend($: BackendScope[Unit, Long]) extends OnUnmount {     // Extends OnUnmount
                                                                       // Removed `var interval`
      def tick = $.modState(_ + 1)

      def start: Callback =
        for {
          i <- CallbackTo(js.timers.setInterval(1.second)(tick.runNow()))
          c  = Callback(js.timers.clearInterval(i))
          _ <- onUnmount(c)                                            // Use onUnmount here
        } yield ()
    }


    val Timer = ScalaComponent.builder[Unit]("Timer")
      .initialState(0L)
      .backend(new Backend(_))
      .render_S(s => div("Seconds elapsed: ", s))
      .componentDidMount(_.backend.start)
                                                                    // Removed componentWillUnmount() call
      .configure(OnUnmount.install)                                 // Register OnUnmount functionality
      .build
  }

  // ===================================================================================================================

  /**
   * This is the typical React timer example, modified to use TimerSupport.
   * (Also removed State in favour of just using Long directly.)
   */
  object SetIntervalExample {

    class Backend extends TimerSupport

    val Timer = ScalaComponent.builder[Unit]("Timer")
      .initialState(0L)
      .backend(_ => new Backend)
      .render_S(s => div("Seconds elapsed: ", s))
      .componentDidMount(c => c.backend.setInterval(c.modState(_ + 1), 1.second))
      .configure(TimerSupport.install)
      .build
  }

  // ===================================================================================================================

  /**
   * This is an example of using Listenable to receive external events that affect a component's state.
   */
  object ListenableExample {

    class Backend extends OnUnmount                     // Required it can unregister listener on unmount.

    def recv(i: Int) = ReactS.mod[Int](_ + i)           // External event handler.
                                                        // When an int is received, add to component state.
    val C = ScalaComponent.builder[Listenable[Int]]("C")
      .initialState(0)
      .backend(_ => new Backend)
      .render_S(s => div("Total: ", s))
      .configure(Listenable.listenWithStateMonad(identity, recv))   // Listen to events when mounted.
      .build
  }
}
