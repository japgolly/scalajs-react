package golly

import scala.scalajs.js
import org.scalajs.dom.{Node, document, console}

import scala.scalajs.js.ThisFunction

object ReactExamples {

  type Props = js.Object

  trait ComponentScope[P <: Props] extends js.Object {
    def props: P = ???
  }

  type RenderFn[P <: Props] = js.ThisFunction0[ComponentScope[P], js.Object]
  def RenderFn[P <: Props](f: ComponentScope[P] => js.Object): RenderFn[P] = f
  def RenderFnP[P <: Props](f: P => js.Object): RenderFn[P] = RenderFn(f.compose(_.props))

  trait CreateClassInput[P <: Props] extends js.Object {
    var render: RenderFn[P] = ???
  }
  object CreateClassInput {
    def apply[P <: Props](render: RenderFn[P]) =
      js.Dynamic.literal("render" -> render).asInstanceOf[CreateClassInput[P]]
  }

  trait React extends js.Object {
    def createClass[P <: Props](c: CreateClassInput[P]): ProxyFn[P] = ???
    def renderComponent(c: ProxyConstructor, n: Node): js.Dynamic = ???
    val DOM: DOM = ???
  }

  trait DOM extends js.Object {
    def div(props: js.Object, children: js.Any*): js.Object = ???
  }

  trait ProxyConstructor extends js.Object

  trait ProxyFn[P <: Props] extends js.Object {
//    def apply(): ProxyConstructor = ???
    def apply(props: P, children: js.Any*): ProxyConstructor = ???
  }

  def React = js.Dynamic.global.React.asInstanceOf[React]

  // ------------------------------------------------------------------------

  //  class HelloProps(val name: String) extends js.Object
  trait HelloProps extends js.Object {
    val name: String
  }
  object HelloProps {
    def apply(name: String) =
      js.Dynamic.literal("name" -> name).asInstanceOf[HelloProps]
  }

  def sample1(): Unit = {
    val renderFn = RenderFnP[HelloProps](p => React.DOM.div(null, "Hello, ", p.name))
    val HelloMessage = React.createClass(CreateClassInput(renderFn))
    val pc = HelloMessage(HelloProps("Johnhy"))

    val tgt = document.getElementById("target")
    React.renderComponent(pc, tgt)
  }
}
