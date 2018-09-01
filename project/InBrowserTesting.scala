import sbt._
import sbt.Keys._
import org.openqa.selenium.remote.DesiredCapabilities
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.scalajs.jsenv.selenium._

object InBrowserTesting {

  val ConfigFirefox = config("firefox")
  val ConfigChrome  = config("chrome")

  private def browserConfig(cfg: Configuration, env: SeleniumJSEnv): Project => Project =
    _.settings(jsEnv in cfg := env)

  def js: Project => Project =
    _.configure(
      browserConfig(ConfigFirefox, new SeleniumJSEnv(DesiredCapabilities.firefox())),
      browserConfig(ConfigChrome, new SeleniumJSEnv(DesiredCapabilities.chrome())))
}
