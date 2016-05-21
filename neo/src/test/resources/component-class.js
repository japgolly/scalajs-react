const ComponentClassP = React.createClass({
  displayName: "HelloMessage",

  render: function render() {
    return React.createElement("div", null, "Hello ", this.props.name);
  }
});

const ComponentClassS = React.createClass({
  displayName: "Statey",

  getInitialState: function getInitialState() {
      return { num: 123 };
    },

    inc: function inc() {
      this.setState({ num: this.state.num + 1 });
    },

  render: function render() {
    return React.createElement("div", null, "State = ", this.state.num);
  }
});

