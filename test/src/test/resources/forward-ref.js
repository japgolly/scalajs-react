var FancyButton = React.forwardRef(function (props, ref) {
  return (
    React.createElement("div", null,
      React.createElement("button", { ref: ref, className: "FancyButton" },
        props.children)));
});