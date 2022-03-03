package japgolly.scalajs.react.test.reactrefresh

import japgolly.scalajs.react.RewritePoC

object RewritePoC1 {

  // val test: scalajs.js.Function1[String, Unit] =
  //   org.scalajs.dom.console.log(_)

  val Component = RewritePoC.start
    .useState(123)
    .render((p, s) => {
      // org.scalajs.dom.console.log("RewritePoC1 render")
      s"Hello p=$p, s=$s !!"
    })

  // TODO: Try fn Ident instead of inline lambda
}


/*
const App = p => {
  _s();

  const [count, setCount] = useState(0);
  return React.createElement("button", {
    type: "button",
    onClick: () => setCount(count => count + 1)
  }, "count is: ", count);
};

_s(App, "oDgYfYHkD9Wkv4hrAPCkI/ev3YU=");

_c = App;
export default App;
*/