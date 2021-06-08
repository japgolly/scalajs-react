package downstream

import japgolly.scalajs.react._

object DownstreamConfig2 extends ScalaJsReactConfig.Defaults {

  override val reusabilityOverride = new ScalaJsReactConfig.ReusabilityOverride {
    override def apply[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot] = b => {
      Globals.reusabilityLog.push(b.name)
      b
    }
  }
}
