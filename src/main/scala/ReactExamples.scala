package golly

import scala.scalajs.js
import org.scalajs.dom.{Node, document, console, window}
import react._

object ReactExamples {

//  object Sample1 {
//
//    case class HelloProps(name: String, age: Int)
//
//    def apply(): Unit = {
//      import react.scalatags.ReactDom._
//      import all._
//
//      val renderFn = RenderFn.wrapped[HelloProps](props =>
//        div(backgroundColor := "#fdd", color := "#c00")(
//          h1("THIS IS AWESOME"),
//          p(textDecoration := "underline")("Hello there, ", "Hello, ", props.name, " of age ", props.age)
//        ).render
//      )
//
//      val HelloMessage = React.createClass(ComponentSpec(renderFn))
//      val pc = HelloMessage(HelloProps("Johnhy", 100))
//
//      val tgt = document.getElementById("target")
//      React.renderComponent(pc, tgt)
//    }
//  }
  
  // ===================================================================================================================

  object Sample2 {

    case class MyState(secondsElapsed: Long) {
      def inc = MyState(secondsElapsed + 1)
    }

    def apply(): Unit = {
      import react.scalatags.ReactDom._
      import all._

      val renderFn = RenderFn[UnitObject, WrapObj[MyState]](ctx =>
        div(backgroundColor := "#fdd", color := "#c00")(
          h1("THIS IS AWESOME"),
          p(textDecoration := "underline")("Seconds elapsed: ", ctx.state.secondsElapsed)
        ).render
      )

      // TODO Needs a hidden state type, or a backend type in which i can put a var and auto-initialise from scope
      var interval: js.UndefOr[Int] = js.undefined

      val spec = ComponentSpecBuilder(renderFn)
        .initialState(MyState(0).wrap)
        .componentDidMount(cs => {
          val tick: js.Function = (_:js.Any) => cs.setState(cs.state.inc.wrap)
          console log "Installing timer..."
          interval = window.setInterval(tick, 1000)
        })
        .componentWillUnmount(_ => interval.foreach(window.clearInterval))
        .build
      val component = React.createClass(spec)
      val c = component(())
      React.renderComponent(c, document getElementById "target")
    }
  }
}
