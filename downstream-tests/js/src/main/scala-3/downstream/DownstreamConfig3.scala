package downstream

import japgolly.scalajs.react._
import ScalaJsReactConfig._

object DownstreamConfig3 extends Defaults {

  def reusabilityOverride(i: Int): Int = 1

  override def reusabilityOverride: ReusabilityOverride = new ReusabilityOverride {
    override def apply[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot] = b => {
      Globals.reusabilityLog.push(b.name)
      b
    }
  }
}
