package demo

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object MainApp {

  val Component = ScalaFnComponent[Unit] { _ =>
    <.div(
      <.h1("HELLO!", ^.color.red),
      <.div(Counter.Component()),
      <.div(Counter.Component()),
    )
  }
}