package downstream

import scala.scalajs.js

object Globals {

  private var _componentInitStarted = false

  def componentInitStarted(): Boolean =
    _componentInitStarted

  def onComponentInit(): Unit =
    _componentInitStarted ||= true

  def clear(): Unit = {
    carrotMountsA  = 0
    carrotMountsB  = 0
    carrotRenders  = 0
    pumpkinRenders = 0
    // Don't clear reusabilityLog as it's only written to on component construction.
  }

  var carrotMountsA  = 0
  var carrotMountsB  = 0
  var carrotRenders  = 0
  var pumpkinRenders = 0
  val reusabilityLog = new js.Array[String]

  clear()
}
