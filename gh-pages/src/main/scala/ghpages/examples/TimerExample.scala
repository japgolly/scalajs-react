package ghpages.examples

import japgolly.scalajs.react._, vdom.all._
import org.scalajs.dom.window
import scala.scalajs.js
import ghpages.examples.util.SideBySide

/** Scala version of "A Stateful Component" on http://facebook.github.io/react/ */
object TimerExample {

  def content = SideBySide.Content(jsSource, source, Timer())

  val jsSource =
    """
      |var Timer = React.createClass({displayName: 'Timer',
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
      |      React.createElement("div", null, "Seconds Elapsed: ", this.state.secondsElapsed)
      |    );
      |  }
      |});
      |
      |React.render(React.createElement(Timer, null), mountNode);
      |""".stripMargin

  val source =
    """
      |val Timer = ReactComponentB[Unit]("Timer")
      | .initialState(State(0))
      | .backend(_ => new Backend)
      | .render((_, s, _) => div("Seconds elapsed: ", s.secondsElapsed))
      | .componentDidMount(scope =>
      |   scope.backend.interval = window.setInterval(scope.backend.tick(scope), 1000))
      | .componentWillUnmount(_.backend.interval foreach window.clearInterval)
      | .buildU
      |""".stripMargin


  case class State(secondsElapsed: Long)

  class Backend {
    var interval: js.UndefOr[Int] = js.undefined
    def tick(scope: ComponentScopeM[_, State, _]): js.Function =
      () => scope.modState(s => State(s.secondsElapsed + 1))
  }

  val Timer = ReactComponentB[Unit]("Timer")
    .initialState(State(0))
    .backend(_ => new Backend)
    .render((_, s, _) => div("Seconds elapsed: ", s.secondsElapsed))
    .componentDidMount(scope =>
      scope.backend.interval = window.setInterval(scope.backend.tick(scope), 1000))
    .componentWillUnmount(_.backend.interval foreach window.clearInterval)
    .buildU
}
