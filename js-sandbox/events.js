'use strict';

const trace = React.__SECRET_INTERNALS_DO_NOT_USE_OR_YOU_WILL_BE_FIRED.SchedulerTracing.unstable_trace;

const Root = document.getElementById('root');

let i = null

class Comp extends React.Component {
  constructor(props) {
    super(props);
    this.state = { value: 'ah' }
  }

  onChange(e) {
    e.persist()
    trace("hxahaha2", performance.now(), () => {
      console.log("Event: ", e)
      this.setState({ value: e.target.value })
    })
  }

  onMouseEvent(e) {
    console.log(`${e.type} : ${e.detail}`)
  }

  profilerCallback(
    id, // the "id" prop of the Profiler tree that has just committed
    phase, // either "mount" (if the tree just mounted) or "update" (if it re-rendered)
    actualDuration, // time spent rendering the committed update
    baseDuration, // estimated time to render the entire subtree without memoization
    startTime, // when React began rendering this update
    commitTime, // when React committed this update
    interactions // the Set of interactions belonging to this update
  ) {
    const all = {
      id, // the "id" prop of the Profiler tree that has just committed
      phase, // either "mount" (if the tree just mounted) or "update" (if it re-rendered)
      actualDuration, // time spent rendering the committed update
      baseDuration, // estimated time to render the entire subtree without memoization
      startTime, // when React began rendering this update
      commitTime, // when React committed this update
      interactions // the Set of interactions belonging to this update
    }
    i = interactions;
    console.log(`profilerCallback: ${JSON.stringify(all)} | ${interactions.size}`)
  }

  render() {
    return trace("hahaasfha", performance.now(), () => {
      const input = React.createElement("input", {
        value: this.state.value,
        onChange: this.onChange.bind(this),
        onMouseDown: this.onMouseEvent.bind(this),
        onMouseUp: this.onMouseEvent.bind(this),
      })
      const prof = React.createElement(React.Profiler, {id: "boop", onRender: this.profilerCallback}, input)
      return prof;
      })
  }
}

trace("hahaha", performance.now(), () => {
ReactDOM.render(React.createElement(Comp, null), Root)
})