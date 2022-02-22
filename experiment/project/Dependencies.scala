import sbt._
import sbt.Keys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {

  object Ver {

    // Externally observable
    val scala                 = "2.13.8"
    // val scalaJsReact          = "2.0.1"
    val scalaJsReact          = "2.1.0-SNAPSHOT"

    // Internal
    val betterMonadicFor      = "0.3.1"
    val kindProjector         = "0.13.2"
  }

  object Dep {
    val scalaJsReact = Def.setting("com.github.japgolly.scalajs-react" %%% "core" % Ver.scalaJsReact)

    // Compiler plugins
    val betterMonadicFor = compilerPlugin("com.olegpy"     %% "better-monadic-for" % Ver.betterMonadicFor)
    val kindProjector    = compilerPlugin("org.typelevel"  %% "kind-projector"     % Ver.kindProjector cross CrossVersion.full)
  }
}
