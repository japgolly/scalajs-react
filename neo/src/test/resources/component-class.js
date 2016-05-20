var ComponentClass = React.createClass({
  displayName: "HelloMessage",

  render: function render() {
    return React.createElement( "div", null, "Hello ", this.props.name);
  }
});

