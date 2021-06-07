package downstream

import java.io.File
import scala.Console._
import scala.io.Source

object Props {

  private object Prop {
    def get(property: String): Option[String] = {
      val o = Option(System.getProperty(property))
      println(s"$CYAN$property$RESET = $YELLOW${o.getOrElse("")}$RESET")
      o
    }

    def get(property: String, default: String): String =
      get(property).getOrElse(default)

    def need(property: String): String =
      get(property).getOrElse(throw new RuntimeException("Property not defined: " + property))
  }

  val jsFilename = Prop.need("js_file")

  val fastOptJS = jsFilename.contains("fast")

  val content: String = {
    val s = Source.fromFile(new File(jsFilename))
    try s.mkString finally s.close()
  }

  val compnameAll  = Prop.get("japgolly.scalajs.react.component.names.all", "allow")
  val compnameAuto = Prop.get("japgolly.scalajs.react.component.names.implicit", "full")
  val configClass  = Prop.get("japgolly.scalajs.react.config.class")

  val dsCfg1 = configClass.contains("downstream.DownstreamConfig1")
  val dsCfg2 = configClass.contains("downstream.DownstreamConfig2")
  val dsCfg3 = configClass.contains("downstream.DownstreamConfig3")

  val reusabilityDev = Prop.get("downstream_tests.reusability.dev")

}
