package japgolly.scalajs.react.test.reactrefresh

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

import org.scalajs.dom

object Main {

  def main(): Unit = {

    locally(RewritePoC1.Component)
    locally(RewritePoC2.Component)
    locally(RewritePoC12.Component)

    val app  = Component()
    val cont = dom.document.getElementById("root")
    app.renderIntoDOM(cont)
  }

  private val Component = ScalaFnComponent[Unit] { _ =>
    <.div(
      UseState1.Component(),
      UseState2.Component(),
      UseStateMulti.Component(),
    )
  }
}
