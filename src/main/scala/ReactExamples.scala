package golly

import scala.scalajs.js
import org.scalajs.dom.{Node, document, console}
import react._

object ReactExamples {

  case class HelloProps(name: String, age: Int)

  def sampleRender(renderFn: RenderFn[PropWrapper[HelloProps]]): Unit = {
    val HelloMessage = React.createClass(ComponentSpec(renderFn))
    val pc = HelloMessage(HelloProps("Johnhy", 100))

    val tgt = document.getElementById("target")
    React.renderComponent(pc, tgt)
  }

  def sample1(): Unit = {
    val renderFn = RenderFn.wrapped[HelloProps](p => React.DOM.div(null, "Hello, ", p.name, " of age ", p.age))
    sampleRender(renderFn)
  }

  def sample2(): Unit = {
    import react.scalatags.ReactDom._
    import all._

    val renderFn = RenderFn.wrapped[HelloProps](props =>
      div(backgroundColor := "#fdd", color := "#c00")(
        h1("THIS IS AWESOME"),
        p(textDecoration := "underline")("Hello there, ", "Hello, ", props.name, " of age ", props.age)
      ).render
    )

    sampleRender(renderFn)
  }
}
