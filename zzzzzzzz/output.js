import {createHotContext as __vite__createHotContext} from "/@vite/client";
import.meta.hot = __vite__createHotContext("/src/App.js");
import RefreshRuntime from "/@react-refresh";
let prevRefreshReg;
let prevRefreshSig;
if (import.meta.hot) {
  if (!window.__vite_plugin_react_preamble_installed__) {
    throw new Error("@vitejs/plugin-react can't detect preamble. Something is wrong. " + "See https://github.com/vitejs/vite-plugin-react/pull/11#discussion_r430879201");
  }
  prevRefreshReg = window.$RefreshReg$;
  prevRefreshSig = window.$RefreshSig$;
  window.$RefreshReg$ = (type,id)=>{
    RefreshRuntime.register(type, "/home/golly/projects/public/scalajs-react/zzzzzzzz/src/App.js" + " " + id)
  }
  ;
  window.$RefreshSig$ = RefreshRuntime.createSignatureFunctionForTransform;
}
var _s = $RefreshSig$();

import __vite__cjsImport2_react from "/node_modules/.vite/react.js?v=ba1de405";
const React = __vite__cjsImport2_react.__esModule ? __vite__cjsImport2_react.default : __vite__cjsImport2_react;
const useState = __vite__cjsImport2_react["useState"];

function App() {
  _s();

  const [count,setCount] = useState(0);
  return React.createElement("button", {
    type: "button",
    onClick: ()=>setCount(count=>count + 1)
  }, "count xxxxx is: ", count);
}

_s(App, "oDgYfYHkD9Wkv4hrAPCkI/ev3YU=");

_c = App;
export default App;
// const omg = 123;
// export { omg, App }

var _c;

$RefreshReg$(_c, "App");
if (import.meta.hot) {
  window.$RefreshReg$ = prevRefreshReg;
  window.$RefreshSig$ = prevRefreshSig;

  import.meta.hot.accept();
  if (!window.__vite_plugin_react_timeout) {
    window.__vite_plugin_react_timeout = setTimeout(()=>{
      window.__vite_plugin_react_timeout = 0;
      RefreshRuntime.performReactRefresh();
    }
    , 30);
  }
}
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi9ob21lL2dvbGx5L3Byb2plY3RzL3B1YmxpYy9zY2FsYWpzLXJlYWN0L3p6enp6enp6L3NyYy9BcHAuanMiXSwibmFtZXMiOlsiUmVhY3QiLCJ1c2VTdGF0ZSIsIkFwcCIsImNvdW50Iiwic2V0Q291bnQiLCJjcmVhdGVFbGVtZW50IiwidHlwZSIsIm9uQ2xpY2siXSwibWFwcGluZ3MiOiI7O0FBQUEsT0FBT0EsS0FBUCxJQUFnQkMsUUFBaEIsUUFBZ0MsT0FBaEM7O0FBRUEsU0FBU0MsR0FBVCxHQUFlO0FBQUE7O0FBQ2IsUUFBTSxDQUFDQyxLQUFELEVBQVFDLFFBQVIsSUFBb0JILFFBQVEsQ0FBQyxDQUFELENBQWxDO0FBRUEsU0FBT0QsS0FBSyxDQUFDSyxhQUFOLENBQW9CLFFBQXBCLEVBQThCO0FBQ25DQyxJQUFBQSxJQUFJLEVBQUUsUUFENkI7QUFFbkNDLElBQUFBLE9BQU8sRUFBRSxNQUFNSCxRQUFRLENBQUNELEtBQUssSUFBSUEsS0FBSyxHQUFHLENBQWxCO0FBRlksR0FBOUIsRUFHSixrQkFISSxFQUdnQkEsS0FIaEIsQ0FBUDtBQUtEOztHQVJRRCxHOztLQUFBQSxHO0FBVVQsZUFBZUEsR0FBZixDLENBRUE7QUFDQSIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCwgeyB1c2VTdGF0ZSB9IGZyb20gJ3JlYWN0J1xuXG5mdW5jdGlvbiBBcHAoKSB7XG4gIGNvbnN0IFtjb3VudCwgc2V0Q291bnRdID0gdXNlU3RhdGUoMClcblxuICByZXR1cm4gUmVhY3QuY3JlYXRlRWxlbWVudChcImJ1dHRvblwiLCB7XG4gICAgdHlwZTogXCJidXR0b25cIixcbiAgICBvbkNsaWNrOiAoKSA9PiBzZXRDb3VudChjb3VudCA9PiBjb3VudCArIDEpXG4gIH0sIFwiY291bnQgeHh4eHggaXM6IFwiLCBjb3VudCk7XG5cbn1cblxuZXhwb3J0IGRlZmF1bHQgQXBwXG5cbi8vIGNvbnN0IG9tZyA9IDEyMztcbi8vIGV4cG9ydCB7IG9tZywgQXBwIH0iXX0=

