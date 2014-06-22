package golly

import scala.scalajs.js.JSApp
import org.scalajs.dom._

object Golly extends JSApp {
  override def main(): Unit = {
    console log "Starting..."
    val h2 = document.createElement("h2")
    val t = document.createTextNode("This is text.")
    h2 appendChild t
    document.body appendChild h2
    console log "Done!"
  }
}

