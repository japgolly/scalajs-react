package golly

import scala.scalajs.js
import org.scalajs.dom.{Node, document, console, window}
import react.scalatags.ReactDom._
import react.scalatags.ReactDom.all._
import react._

object ReactExamples {

  object Sample1 {

    case class HelloProps(name: String, age: Int)

    val component = ComponentBuilder[HelloProps, Unit]
      .render(t =>
        div(backgroundColor := "#fdd", color := "#c00")(
          h1("THIS IS COOL."),
          p(textDecoration := "underline")("Hello there, ", "Hello, ", t.props.name, " of age ", t.props.age)
        ).render
      ).build

    def apply(): Unit = {
      React.renderComponent(component.create(HelloProps("Johnhy", 100)), document getElementById "target")
    }
  }
  
  // ===================================================================================================================

  object Sample2 {

    case class MyProps(title: String, startTime: Long)

    case class MyState(secondsElapsed: Long) {
      def inc = MyState(secondsElapsed + 1)
    }

    class MyBackend {
      var interval: js.UndefOr[Int] = js.undefined
      def start(tick: js.Function): Unit = interval = window.setInterval(tick, 1000)
      def stop(): Unit = interval foreach window.clearInterval
    }

    val component = ComponentBuilder[MyProps, MyState]
      .backend(new MyBackend)
      .render(ctx =>
        div(backgroundColor := "#fdd", color := "#c00")(
          h1("THIS IS AWESOME (", ctx.props.title, ")"),
          p(textDecoration := "underline")("Seconds elapsed: ", ctx.state.secondsElapsed)
        ).render
      )
      .getInitialState(ctx => MyState(ctx.props.startTime))
      .componentDidMount(ctx => {
        val tick: js.Function = (_: js.Any) => ctx.modState(_.inc)
        console log "Installing timer..."
        ctx.backend.start(tick)
      })
      .componentWillUnmount(_.backend.stop)
      .build

    def apply(): Unit = {
      React.renderComponent(component.create(MyProps("Great", 0)), document getElementById "target")
      React.renderComponent(component.create(MyProps("Again", 1000)), document getElementById "target2")
    }
  }
}
