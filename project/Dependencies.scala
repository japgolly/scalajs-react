import sbt._
import sbt.Keys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import org.scalajs.jsdependencies.sbtplugin.JSDependenciesPlugin
import org.scalajs.jsdependencies.sbtplugin.JSDependenciesPlugin.autoImport._

object Dependencies {

  object Ver {

    // Externally observable
    val Cats                  = "2.4.2"
    val CatsEffect            = "2.3.3"
    val MonocleCats           = "2.1.0"
    val Monocle3              = "3.0.0-M3"
    val MonocleScalaz         = "1.6.3"
    val Scala212              = "2.12.13"
    val Scala213              = "2.13.5"
    val ScalaCollCompat       = "2.4.2"
    val ScalaJsDom            = "1.1.0"
    val Scalaz72              = "7.2.31"
    val Sourcecode            = "0.2.4"

    // Internal
    val BetterMonadicFor      = "0.3.1"
    val CatsTestkitScalaTest  = "2.1.2"
    val DisciplineScalaTest   = "2.1.2"
    val KindProjector         = "0.11.3"
    val MacroParadise         = "2.1.1"
    val Microlibs             = "2.5"
    val MTest                 = "0.7.7"
    val Nyaya                 = "0.10.0-RC1"
    val ReactJs               = "16.14.0"
    val ScalaJsJavaTime       = "1.0.0"
    val ScalaTest             = "3.2.6"
    val SizzleJs              = "2.3.0"
  }

  object Dep {
    val Cats                 = Def.setting("org.typelevel"                 %%% "cats-core"               % Ver.Cats)
    val CatsEffect           = Def.setting("org.typelevel"                 %%% "cats-effect"             % Ver.CatsEffect)
    val CatsEffectLaws       = Def.setting("org.typelevel"                 %%% "cats-effect-laws"        % Ver.CatsEffect)
    val CatsTestkit          = Def.setting("org.typelevel"                 %%% "cats-testkit"            % Ver.Cats)
    val CatsTestkitScalaTest = Def.setting("org.typelevel"                 %%% "cats-testkit-scalatest"  % Ver.CatsTestkitScalaTest)
    val DisciplineScalaTest  = Def.setting("org.typelevel"                 %%% "discipline-scalatest"    % Ver.DisciplineScalaTest)
    val MicrolibsTestUtil    = Def.setting("com.github.japgolly.microlibs" %%% "test-util"               % Ver.Microlibs)
    val MonocleCats          = Def.setting("com.github.julien-truffaut"    %%% "monocle-core"            % Ver.MonocleCats)
    val Monocle3             = Def.setting("com.github.julien-truffaut"    %%% "monocle-core"            % Ver.Monocle3)
    val MonocleScalaz        = Def.setting("com.github.julien-truffaut"    %%% "monocle-core"            % Ver.MonocleScalaz)
    val MonocleScalazMacro   = Def.setting("com.github.julien-truffaut"    %%% "monocle-macro"           % Ver.MonocleScalaz)
    val MTest                = Def.setting("com.lihaoyi"                   %%% "utest"                   % Ver.MTest)
    val NyayaGen             = Def.setting("com.github.japgolly.nyaya"     %%% "nyaya-gen"               % Ver.Nyaya)
    val NyayaProp            = Def.setting("com.github.japgolly.nyaya"     %%% "nyaya-prop"              % Ver.Nyaya)
    val NyayaTest            = Def.setting("com.github.japgolly.nyaya"     %%% "nyaya-test"              % Ver.Nyaya)
    val ScalaCollCompat      = Def.setting("org.scala-lang.modules"        %%% "scala-collection-compat" % Ver.ScalaCollCompat)
    val ScalaCompiler        = Def.setting("org.scala-lang"                  % "scala-compiler"          % scalaVersion.value)
    val ScalaJsDom           = Def.setting("org.scala-js"                  %%% "scalajs-dom"             % Ver.ScalaJsDom)
    val ScalaJsJavaTime      = Def.setting("org.scala-js"                  %%% "scalajs-java-time"       % Ver.ScalaJsJavaTime)
    val ScalaReflect         = Def.setting("org.scala-lang"                  % "scala-reflect"           % scalaVersion.value)
    val ScalaTest            = Def.setting("org.scalatest"                 %%% "scalatest"               % Ver.ScalaTest)
    val ScalazEffect72       = Def.setting("org.scalaz"                    %%% "scalaz-effect"           % Ver.Scalaz72)
    val Sourcecode           = Def.setting("com.lihaoyi"                   %%% "sourcecode"              % Ver.Sourcecode)

    // Compiler plugins
    val BetterMonadicFor = compilerPlugin("com.olegpy"     %% "better-monadic-for" % Ver.BetterMonadicFor)
    val KindProjector    = compilerPlugin("org.typelevel"  %% "kind-projector"     % Ver.KindProjector cross CrossVersion.full)
    val MacroParadise    = compilerPlugin("org.scalamacros" % "paradise"           % Ver.MacroParadise cross CrossVersion.patch)

    def SizzleJs(scope: Configuration) =
      Def.setting("org.webjars.bower" % "sizzle" % Ver.SizzleJs % scope / "sizzle.min.js" commonJSName "Sizzle")

    val React             = ReactArtifact("react")
    val ReactDom          = ReactArtifact("react-dom")
    val ReactDomServer    = ReactArtifact("react-dom-server.browser")
    val ReactDomTestUtils = ReactArtifact("react-dom-test-utils")
  }

  final case class ReactArtifact(filename: String) {
    val dev = s"umd/$filename.development.js"
    val prod = s"umd/$filename.production.min.js"
  }

  def addReactJsDependencies(scope: Configuration): Project => Project =
    _.enablePlugins(JSDependenciesPlugin)
      .settings(
        dependencyOverrides += "org.webjars.npm" % "js-tokens" % "3.0.2", // https://github.com/webjars/webjars/issues/1789
        dependencyOverrides += "org.webjars.npm" % "scheduler" % "0.12.0-alpha.3",
        jsDependencies ++= Seq(

          "org.webjars.npm" % "react" % Ver.ReactJs % scope
            /        "umd/react.development.js"
            minified "umd/react.production.min.js"
            commonJSName "React",

          "org.webjars.npm" % "react-dom" % Ver.ReactJs % scope
            /         "umd/react-dom.development.js"
            minified  "umd/react-dom.production.min.js"
            dependsOn "umd/react.development.js"
            commonJSName "ReactDOM",

          "org.webjars.npm" % "react-dom" % Ver.ReactJs % scope
            /         "umd/react-dom-test-utils.development.js"
            minified  "umd/react-dom-test-utils.production.min.js"
            dependsOn "umd/react-dom.development.js"
            commonJSName "ReactTestUtils",

          "org.webjars.npm" % "react-dom" % Ver.ReactJs % scope
            /         "umd/react-dom-server.browser.development.js"
            minified  "umd/react-dom-server.browser.production.min.js"
            dependsOn "umd/react-dom.development.js"
            commonJSName "ReactDOMServer"),

        skip in packageJSDependencies := false)

  def addMacroParadise: Project => Project =
    _.settings(libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, v)) if v <= 12 =>
          Dep.MacroParadise :: Nil
        case _ =>
          // if scala 2.13.0-M4 or later, macro annotations merged into scala-reflect
          // https://github.com/scala/scala/pull/6606
          Nil
      }
    })

}
