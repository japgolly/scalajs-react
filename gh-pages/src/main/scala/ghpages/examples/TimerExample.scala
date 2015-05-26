package ghpages.examples

import japgolly.scalajs.react._, vdom.prefix_<^._
import scala.scalajs.js
import ghpages.examples.util.SideBySide

/** Scala version of "A Stateful Component" on https://facebook.github.io/react/ */
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
      |case class State(secondsElapsed: Long)
      |
      |class Backend($: BackendScope[_, State]) {
      |  var interval: js.UndefOr[js.timers.SetIntervalHandle] =
      |    js.undefined
      |
      |  def tick() =
      |    $.modState(s => State(s.secondsElapsed + 1))
      |
      |  def start() =
      |    interval = js.timers.setInterval(1000)(tick())
      |}
      |
      |val Timer = ReactComponentB[Unit]("Timer")
      |  .initialState(State(0))
      |  .backend(new Backend(_))
      |  .render($ => <.div("Seconds elapsed: ", $.state.secondsElapsed))
      |  .componentDidMount(_.backend.start())
      |  .componentWillUnmount(_.backend.interval foreach js.timers.clearInterval)
      |  .buildU
      |""".stripMargin


  case class State(secondsElapsed: Long)

  class Backend($: BackendScope[_, State]) {
    var interval: js.UndefOr[js.timers.SetIntervalHandle] =
      js.undefined

    def tick() =
      $.modState(s => State(s.secondsElapsed + 1))

    def start() =
      interval = js.timers.setInterval(1000)(tick())
  }

  val Timer = ReactComponentB[Unit]("Timer")
    .initialState(State(0))
    .backend(new Backend(_))
    .render($ => <.div("Seconds elapsed: ", $.state.secondsElapsed))
    .componentDidMount(_.backend.start())
    .componentWillUnmount(_.backend.interval foreach js.timers.clearInterval)
    .buildU
}
