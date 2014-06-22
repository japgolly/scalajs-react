package golly

import scala.scalajs.js.JSApp
import org.scalajs.dom.{document, console}

object Golly extends JSApp {
  override def main(): Unit = {
    console log "Starting..."
    eg1()
    eg2()

    ReactExamples.sample1()

    console log "Done!"
  }

  def eg1(): Unit = {
    console log "Example #1"
    val h2 = document.createElement("h2")
    val t = document.createTextNode("This is text.")
    h2 appendChild t
    document.body appendChild h2
  }

  def eg2(): Unit = {
    console log "Example #2"
    console log s"Appending: ${makeDom(scalatags.Text).toString}"
    console log s"Appending: ${makeDom(scalatags.JsDom).toString}"
    document.body appendChild makeDom(scalatags.JsDom).render
  }

  def makeDom[Builder, Output <: FragT, FragT](bundle: scalatags.generic.Bundle[Builder, Output, FragT]) = {
    import bundle._
    import bundle.all._
    import bundle.tags2._

    section(backgroundColor := "#dfe")(
      h2("This is example #2"),
      p("<hr/> more text.")
    )
  }
}
