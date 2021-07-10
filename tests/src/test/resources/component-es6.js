class ES6_P extends React.Component {
  render() {
    return React.createElement("div", null, "Hello ", this.props.name, this.props.children);
  }
}


class ES6_S extends React.Component {
//  static displayName = "Statey";

  constructor(props) {
    super(props);
    this.state = { num1: 123, num2: 500 };
  }

  inc() {
    this.setState({ num1: this.state.num1 + 1 });
  }

  render() {
    return React.createElement("div", null, "State = ", this.state.num1, " + ", this.state.num2, this.props.children);
  }
}
ES6_S.displayName = "Statey";
