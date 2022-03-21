import React, { useState, useEffect } from 'react'

/*
// eval("console.log('_s = ', _s)")

function App() {
  // const [count, setCount] = useState(0)

  const temp = useState(0)
  const [count, setCount] = temp

  useEffect(() => {
    document.title = `You clicked ${count} times`;
  });

  return React.createElement("button", {
    type: "button",
    onClick: () => setCount(count => count + 1)
  }, "count is: ", count);

}
*/

/*
// DOESN'T WORK
// =============
const App = (this$3 => p => {
  const [count, setCount] = useState(0)

  return React.createElement("button", {
    type: "button",
    onClick: () => setCount(count => count + 1)
  }, "count is: ", count);

})(this);
*/

const App = (x => p => {
  const [count, setCount] = useState(0)

  return React.createElement("button", {
    type: "button",
    onClick: () => setCount(count => count + 1)
  }, "count!! is: ", count);

})(123);

export default App

// const omg = 123;
// export { omg, App }
