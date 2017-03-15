var React = require('react');

const ES3_P = React.createClass({
  render: function render() {
    return React.createElement("div", null, "Hello ", this.props.name, this.props.children);
  }
});

const ES3_S = React.createClass({
  displayName: "Statey",

  getInitialState: function getInitialState() {
      return { num1: 123, num2: 500 };
    },

    inc: function inc() {
      this.setState({ num1: this.state.num1 + 1 });
    },

  render: function render() {
    return React.createElement("div", null, "State = ", this.state.num1, " + ", this.state.num2, this.props.children);
  }
});

module.exports = {ES3_P, ES3_S};
