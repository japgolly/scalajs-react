package japgolly.scalajs.react

import scala.scalajs.js
import js.Dynamic.{global => $}
import vdom.ReactVDom._
import all._
import utest._

object Test extends TestSuite {

  val tests = TestSuite {
    if (js.isUndefined($.React) && !js.isUndefined($.module))
      js.eval("React = module.exports")

    'props {
      val Comp = ReactComponentB[String]("C").render(name => div("Hi ", name)).create
      val c = Comp("Mate")
      val m = React.renderComponentToStaticMarkup(c)
      assert(m == "<div>Hi Mate</div>")
    }
  }
}