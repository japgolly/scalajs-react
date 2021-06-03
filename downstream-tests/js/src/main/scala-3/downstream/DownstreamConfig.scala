package downstream

import japgolly.scalajs.react._

object DownstreamConfig extends ScalaJsReactConfig.Defaults {

  override transparent inline def automaticComponentName(name: String) =
    name + "-YEAH"
}
