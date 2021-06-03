package downstream

import japgolly.scalajs.react._

object DownstreamConfig extends ScalaJsReactConfig.Defaults {

  later override def automaticComponentName(name: String) =
    name + "-YEAH"
}
