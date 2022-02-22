package demo

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object MainApp {

  // var omg = false

  val Component = ScalaFnComponent[Unit] { _ =>
    <.div(
      <.h1("HELLO!", ^.color.red),
      // <.div(App.comp()).when(omg),
      // <.div(Counter.Component()),
      // <.div(Counter.Component()),
      // <.div(CounterV.Component()),
      <.p(CounterV.Component()),
    )
  }
}