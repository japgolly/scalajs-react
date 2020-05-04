'use strict';

const Root = document.getElementById('root');

class Comp extends React.Component {
  constructor(props) {
    super(props);
  }

  onChange(e) {
    e.persist()
    console.log("Event: ", e)
  }

  render() {
    return React.createElement("input", {
      value: "ah",
      onChange: this.onChange
    })
  }

  componentDidCatch(e, i) {
    err = e
    info = i
    console.log("Error: ", err)
    console.log("Error msg: ", e.message)
    console.log("Info: ", info)
    this.setState({error: e.message, hasError: true})
  }
}

ReactDOM.render(React.createElement(Comp, null), Root)
