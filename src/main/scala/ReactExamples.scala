package golly

import scala.scalajs.js
import org.scalajs.dom.{Node, document, console}

object ReactExamples {

  trait ComponentScope extends js.Object {
    type P <: js.Object
    def props: P = ???
  }

  //  class Test(x: js.Any)// extends js.Object

  type RenderFn[S <: ComponentScope] = js.ThisFunction0[S, js.Object]
  //  class CreateClassInput[S <: ComponentScope](val render: RenderFn[S]) extends js.Object
  trait CreateClassInput[S <: ComponentScope] extends js.Object {
    var render: RenderFn[S] = ???
  }
  object CreateClassInput {
    def apply[S <: ComponentScope](render: RenderFn[S]) =
      js.Dynamic.literal("render" -> render).asInstanceOf[CreateClassInput[S]]
  }

  trait React extends js.Object {
    def createClass[S <: ComponentScope](c: CreateClassInput[S]): ProxyFn[S#P] = ???
    def renderComponent(c: ProxyConstructor, n: Node): js.Dynamic = ???
    val DOM: DOM = ???
  }

  trait DOM extends js.Object {
    def div(props: js.Object, children: js.Any*): js.Object = ???
  }

  trait ProxyConstructor extends js.Object

  trait ProxyFn[Props <: js.Object] extends js.Object {
    def apply(): ProxyConstructor = ???
    def apply(props: Props, children: js.Any*): ProxyConstructor = ???
  }

  def React = js.Dynamic.global.React.asInstanceOf[React]

  // ------------------------------------------------------------------------

  trait HelloScope extends ComponentScope {
    override type P = HelloProps
  }
  //  class HelloProps(val name: String) extends js.Object
  trait HelloProps extends js.Object {
    val name: String
  }
  object HelloProps {
    def apply(name: String) =
      js.Dynamic.literal("name" -> name).asInstanceOf[HelloProps]
  }


  def sample1(): Unit = {
    //    console.log(new Test("hehe"))

    val renderFn: RenderFn[HelloScope] = (t:HelloScope) => React.DOM.div(null, "Hello, ", t.props.name)
    val cc = CreateClassInput[HelloScope](renderFn)
    val HelloMessage = React.createClass(cc)
    val p = HelloProps("Johnhy")
    val pc = HelloMessage(p)
    val tgt = document.getElementById("target")
    React.renderComponent(pc, tgt)
  }
}
