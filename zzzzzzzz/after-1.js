var _s = $RefreshSig$();

import React, { useState } from 'react'

function App() {
  _s();
  const [count, setCount] = useState(0)
  return React.createElement("button", {
    type: "button",
    onClick: () => setCount(count => count + 1)
  }, "count xx is: ", count);
}

_s(App, "oDgYfYHkD9Wkv4hrAPCkI/ev3YU=");

_c = App;
export default App

var _c;

$RefreshReg$(_c, "App");

