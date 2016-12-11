var React = require('react');

module.exports = React.createClass({
  getInitialState: function() {
    return {num:0,num2:0};
  },
  render: function() {
    return React.createElement("div", null, this.props.propOne);
  },
  getNum:function() {
    return this.state.num;
  },
  setNum:function(n) {
    this.setState({num:n});
  }
});