package downstream

import japgolly.scalajs.react._

object DownstreamConfig1 extends ScalaJsReactConfig {

  override transparent inline def automaticComponentName(name: String) =
    name + "-AUTO"

  override transparent inline def modifyComponentName(name: String) =
    name + "-MOD"

  override object reusabilityOverride extends ScalaJsReactConfig.ReusabilityOverride {
    override def apply[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot] = b => {
      Globals.reusabilityLog.push(b.name)
      if (b.name contains "CarRot")
        ScalaJsReactConfig.ReusabilityOverride.default[P, C, S, B, U].apply(b)
      else
        b
    }
  }
}
