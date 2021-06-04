package downstream

import scala.scalajs.js

object Globals {

  def clear(): Unit = {
    pumpkinRenders = 0
    // Don't clear reusabilityLog as it's only written to on component construction.
  }

  var pumpkinRenders = 0
  val reusabilityLog = new js.Array[String]

  clear()
}
