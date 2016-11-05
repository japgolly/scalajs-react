import sbt._
import sbt.Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.scalajs.sbtplugin.cross.CrossProject
import org.scalajs.sbtplugin.ScalaJSPluginInternal._
import org.scalajs.jsenv.selenium._

object InBrowserTesting {

  lazy val testAll = TaskKey[Unit]("test-all", "Run tests in all test platforms.")

  val ConfigFirefox = config("firefox")
  val ConfigChrome  = config("chrome")

  private def browserConfig(cfg: Configuration, env: SeleniumJSEnv): Project => Project =
    _.settings(
      inConfig(cfg)(
        Defaults.testSettings ++
        scalaJSTestSettings ++
        Seq(

          // Scala.JS public settings
          checkScalaJSSemantics         := (checkScalaJSSemantics         in Test).value,
          emitSourceMaps                := (emitSourceMaps                in Test).value,
          fastOptJS                     := (fastOptJS                     in Test).value,
          fullOptJS                     := (fullOptJS                     in Test).value,
          jsDependencies                := (jsDependencies                in Test).value,
          jsDependencyFilter            := (jsDependencyFilter            in Test).value,
          jsDependencyManifest          := (jsDependencyManifest          in Test).value,
          jsDependencyManifests         := (jsDependencyManifests         in Test).value,
          jsManifestFilter              := (jsManifestFilter              in Test).value,
       // loadedJSEnv                   := (loadedJSEnv                   in Test).value,
          packageJSDependencies         := (packageJSDependencies         in Test).value,
          packageMinifiedJSDependencies := (packageMinifiedJSDependencies in Test).value,
          packageScalaJSLauncher        := (packageScalaJSLauncher        in Test).value,
          persistLauncher               := (persistLauncher               in Test).value,
          relativeSourceMaps            := (relativeSourceMaps            in Test).value,
          resolvedJSDependencies        := (resolvedJSDependencies        in Test).value,
       // resolvedJSEnv                 := (resolvedJSEnv                 in Test).value,
       // scalaJSConsole                := (scalaJSConsole                in Test).value,
          scalaJSIR                     := (scalaJSIR                     in Test).value,
          scalaJSLauncher               := (scalaJSLauncher               in Test).value,
          scalaJSLinkedFile             := (scalaJSLinkedFile             in Test).value,
          scalaJSNativeLibraries        := (scalaJSNativeLibraries        in Test).value,
          scalaJSOptimizerOptions       := (scalaJSOptimizerOptions       in Test).value,
          scalaJSOutputMode             := (scalaJSOutputMode             in Test).value,
          scalaJSOutputWrapper          := (scalaJSOutputWrapper          in Test).value,
          scalajsp                      := (scalajsp                      in Test).inputTaskValue,
          scalaJSSemantics              := (scalaJSSemantics              in Test).value,
          scalaJSStage                  := (scalaJSStage                  in Test).value,

          // Scala.JS internal settings
          scalaJSClearCacheStats := (scalaJSClearCacheStats in Test).value,
          scalaJSEnsureUnforked  := (scalaJSEnsureUnforked  in Test).value,
          scalaJSIRCacheHolder   := (scalaJSIRCacheHolder   in Test).value,
          scalaJSIRCache         := (scalaJSIRCache         in Test).value,
          scalaJSLinker          := (scalaJSLinker          in Test).value,
          scalaJSRequestsDOM     := (scalaJSRequestsDOM     in Test).value,
          sjsirFilesOnClasspath  := (sjsirFilesOnClasspath  in Test).value,
          usesScalaJSLinkerTag   := (usesScalaJSLinkerTag   in Test).value,

          // SBT test settings
          definedTestNames     := (definedTestNames     in Test).value,
          definedTests         := (definedTests         in Test).value,
       // executeTests         := (executeTests         in Test).value,
       // loadedTestFrameworks := (loadedTestFrameworks in Test).value,
       // testExecution        := (testExecution        in Test).value,
       // testFilter           := (testFilter           in Test).value,
          testForkedParallel   := (testForkedParallel   in Test).value,
       // testFrameworks       := (testFrameworks       in Test).value,
          testGrouping         := (testGrouping         in Test).value,
       // testListeners        := (testListeners        in Test).value,
       // testLoader           := (testLoader           in Test).value,
       // testOnly             := (testOnly             in Test).value,
          testOptions          := (testOptions          in Test).value,
       // testQuick            := (testQuick            in Test).value,
          testResultLogger     := (testResultLogger     in Test).value,
       // test                 := (test                 in Test).value,

          // In-browser settings
          jsEnv           := env,
          requiresDOM     := true)))

  def js: Project => Project =
    _.configure(
      browserConfig(ConfigFirefox, new SeleniumJSEnv(Firefox)),
      browserConfig(ConfigChrome, new SeleniumJSEnv(Chrome)))
    .settings(
      testAll := {
        (test in Test         ).value
        (test in ConfigFirefox).value
        (test in ConfigChrome ).value
      })

  def jvm: Project => Project =
    _.settings(
      testAll := (test in Test).value)

  def cross: CrossProject => CrossProject =
    _.jvmConfigure(jvm).jsConfigure(js)
}
