var _s = $RefreshSig$();

import React, { useState, useEffect } from 'react';

function App() {
  _s();

  const [count, setCount] = useState(0);
  useEffect(() => {
    document.title = `You clicked ${count} times`;
  });
  return React.createElement("button", {
    type: "button",
    onClick: () => setCount(count => count + 1)
  }, "count is: ", count);
}

_s(App, "/xL7qdScToREtqzbt5GZ1kHtYjQ=");

_c = App;
export default App;

var _c;

$RefreshReg$(_c, "App");
