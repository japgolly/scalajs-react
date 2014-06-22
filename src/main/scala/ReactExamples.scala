package golly

import scala.scalajs.js
import org.scalajs.dom.{Node, document, console}

object ReactExamples {
  import scala.scalajs.js

//  object ReactGlobal extends js.GlobalScope {
//    val react: React = ???
//  }
//  import ReactGlobal.{react => React}

  trait React extends js.Object {
    def createClass(c: js.Dynamic): ProxyFn = ???
    def renderComponent(c: ProxyConstructor, n: Node): js.Dynamic = ???
    val DOM: DOM = ???
  }

  trait DOM extends js.Object {
    def div(props: js.Object, children: js.Any*): js.Object = ???
  }

  trait ProxyConstructor extends js.Object

  trait ProxyFn extends js.Object { //extends js.Function2[js.Object, js.Any, ProxyConstructor] {
    def apply(): ProxyConstructor = ???
    def apply(props: js.Object, children: js.Any*): ProxyConstructor = ???
  }


  def React = js.Dynamic.global.React.asInstanceOf[React]

  def sample1(): Unit = {
    console.log("React = ", React)
    val renderFn: js.ThisFunction0[js.Dynamic, js.Object] =
      (t: js.Dynamic) => React.DOM.div(null, "Hello, ", t.props.name)
    val cc = js.Dynamic.literal("render" -> renderFn) //.asInstanceOf[js.Object]
    console.log("RenderFn = ", renderFn)
    console.log("CC = ", cc)
    val HelloMessage = React.createClass(cc)
    console.log("HelloMessage = ", HelloMessage)
    val propobj = js.Dynamic.literal("name" -> "John").asInstanceOf[js.Object]
    console.log("propobj = ", propobj)
    val pc = HelloMessage(propobj)
    console.log("PC = ", pc)
    val tgt = document.getElementById("target")
    console.log("TGT = ", tgt)
    React.renderComponent(pc, tgt)
  }

}
