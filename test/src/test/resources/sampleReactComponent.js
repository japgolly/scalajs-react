var SampleReactComponent = React.createClass({
  getInitialState: function() {
    return {num:0};
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