package demo

import org.scalajs.dom

object Main {
  def main(): Unit = {
    dom.console.info("Main loading!")

    val app  = MainApp.Component()
    val cont = dom.document.getElementById("root")
    app.renderIntoDOM(cont)
  }
}
