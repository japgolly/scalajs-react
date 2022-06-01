package japgolly.scalajs.react.test.emissions.util

object Props {

  def rootPkg          = "japgolly.scalajs.react.test.emissions"
  def moduleName       = "testEmissions"
  def resSubdirJsRR    = "rr-js"
  def resSubdirScala   = "sjr"
  def resSubdirScalaRR = "rr-sjr"

  val CI          = Prop.get("CI").contains("1")
  val jsOutputDir = Prop.need("jsOutputDir")
  val tempDir     = Prop.need("tempDir")
  val testResDir  = Prop.need("testResDir")
  val testRootDir = Prop.need("testRootDir")

  // -------------------------------------------------------------------------------------------------------------------

  private object Prop {
    import scala.Console._

    def get(property: String): Option[String] = {
      val o = Option(System.getProperty(property))
      println(s"$CYAN[$moduleName] $property$RESET = $YELLOW${o.getOrElse("")}$RESET")
      o
    }

    def get(property: String, default: String): String =
      get(property).getOrElse(default)

    def need(property: String): String =
      get(property).getOrElse(throw new RuntimeException("Property not defined: " + property))
  }
}
