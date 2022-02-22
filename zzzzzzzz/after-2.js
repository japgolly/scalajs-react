import RefreshRuntime from "/@react-refresh";let prevRefreshReg;let prevRefreshSig;if (import.meta.hot) {  if (!window.__vite_plugin_react_preamble_installed__) {    throw new Error(      "@vitejs/plugin-react can't detect preamble. Something is wrong. " +      "See https://github.com/vitejs/vite-plugin-react/pull/11#discussion_r430879201"    );  }  prevRefreshReg = window.$RefreshReg$;  prevRefreshSig = window.$RefreshSig$;  window.$RefreshReg$ = (type, id) => {    RefreshRuntime.register(type, "/home/golly/projects/public/scalajs-react/zzzzzzzz/src/App.js" + " " + id)  };  window.$RefreshSig$ = RefreshRuntime.createSignatureFunctionForTransform;}

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

if (import.meta.hot) {
  window.$RefreshReg$ = prevRefreshReg;
  window.$RefreshSig$ = prevRefreshSig;

  import.meta.hot.accept();
  if (!window.__vite_plugin_react_timeout) {
    window.__vite_plugin_react_timeout = setTimeout(() => {
      window.__vite_plugin_react_timeout = 0;
      RefreshRuntime.performReactRefresh();
    }, 30);
  }
}

