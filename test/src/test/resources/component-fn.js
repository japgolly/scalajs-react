var React = require('react');

function FnComp(props) {
  return React.createElement(
    "div",
    null,
    "Hello ",
    props.name
  );
}

module.exports = {FnComp};
