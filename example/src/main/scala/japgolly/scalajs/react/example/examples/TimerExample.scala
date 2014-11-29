package japgolly.scalajs.react.example.examples

import japgolly.scalajs.react.vdom.ReactVDom.all._
import japgolly.scalajs.react.{ComponentScopeM, ReactComponentB}
import org.scalajs.dom.window

import scala.scalajs.js

/**
 * Created by chandrasekharkode on 11/18/14.
 */
object TimerExample {

  val timerJsxCode = """
                       | var Timer = React.createClass({displayName: 'Timer',
                       |  getInitialState: function() {
                       |    return {secondsElapsed: 0};
                       |  },
                       |  tick: function() {
                       |    this.setState({secondsElapsed: this.state.secondsElapsed + 1});
                       |  },
                       |  componentDidMount: function() {
                       |    this.interval = setInterval(this.tick, 1000);
                       |  },
                       |  componentWillUnmount: function() {
                       |    clearInterval(this.interval);
                       |  },
                       |  render: function() {
                       |    return (
                       |      React.DOM.div(null, "Seconds Elapsed: ", this.state.secondsElapsed)
                       |    );
                       |  }
                       |});
                       |
                       |React.render(Timer(null ), mountNode);</code></pre>
                       |            """.stripMargin

  val timerScalaCode = """
                         | case class State(secondsElapsed: Long)
                         |
                         |  class Backend {
                         |    var interval: js.UndefOr[Int] = js.undefined
                         |
                         |    def tick(scope: ComponentScopeM[_, State, _]): js.Function =
                         |      () => scope.modState(s => State(s.secondsElapsed + 1))
                         |  }
                         |
                         |  val Timer = ReactComponentB[Unit]("Timer")
                         |    .initialState(State(0))
                         |    .backend(_ => new Backend)
                         |    .render((_, S, _) => div("Seconds elapsed: ", S.secondsElapsed))
                         |    .componentDidMount(scope =>
                         |    scope.backend.interval = window.setInterval(scope.backend.tick(scope), 1000))
                         |    .componentWillUnmount(_.backend.interval foreach window.clearInterval)
                         |    .buildU""".stripMargin


  case class State(secondsElapsed: Long)

  class Backend {
    var interval: js.UndefOr[Int] = js.undefined

    def tick(scope: ComponentScopeM[_, State, _]): js.Function =
      () => scope.modState(s => State(s.secondsElapsed + 1))
  }

  val Timer = ReactComponentB[Unit]("Timer")
    .initialState(State(0))
    .backend(_ => new Backend)
    .render((_, S, _) => div("Seconds elapsed: ", S.secondsElapsed))
    .componentDidMount(scope =>
    scope.backend.interval = window.setInterval(scope.backend.tick(scope), 1000))
    .componentWillUnmount(_.backend.interval foreach window.clearInterval)
    .buildU

}
