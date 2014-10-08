package japgolly.scalajs.react.example

import scala.scalajs.js
import org.scalajs.dom.window
import japgolly.scalajs.react._, vdom.ReactVDom._, all._, ScalazReact._
import japgolly.scalajs.react.experiment._

object ExperimentExamples {

  /**
   * This is the typical React timer example, modified to use OnUnmount.
   */
  object OnUnmountExample {

    case class State(secondsElapsed: Long)

    class Backend extends OnUnmount {                               // Extends OnUnmount
                                                                    // Removed `var interval`
      def tick(scope: ComponentScopeM[_, State, _]): js.Function =
        () => scope.modState(s => State(s.secondsElapsed + 1))
    }

    val Timer = ReactComponentB[Unit]("Timer")
      .initialState(State(0))
      .backend(_ => new Backend)
      .render((_,S,_) => div("Seconds elapsed: ", S.secondsElapsed))
      .componentDidMount(scope => {
        val i = window.setInterval(scope.backend.tick(scope), 1000)
        scope.backend onUnmount window.clearInterval(i)             // Use onUnmount here
      })
                                                                    // Removed componentWillUnmount() call
      .configure(OnUnmount.install)                                 // Register OnUnmount functionality
      .buildU
  }

  // ===================================================================================================================

  /**
   * This is the typical React timer example, modified to use SetInterval.
   * Also removed State in favour of just using Long directly.
   */
  object SetIntervalExample {

    class Backend extends SetInterval

    val Timer = ReactComponentB[Unit]("Timer")
      .initialState(0)
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
