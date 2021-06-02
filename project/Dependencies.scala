import sbt._
import sbt.Keys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import org.scalajs.jsdependencies.sbtplugin.JSDependenciesPlugin
import org.scalajs.jsdependencies.sbtplugin.JSDependenciesPlugin.autoImport._

object Dependencies {

  object Ver {

    // Externally observable
    val cats                  = "2.6.0"
    val catsEffect            = "2.5.0"
    val microlibs             = "2.6-RC6"
    val monocleCats           = "2.1.0"
    val monocle3              = "3.0.0-M5"
    val monocleScalaz         = "1.6.3"
    val scala212              = "2.12.13"
    val scala213              = "2.13.6"
    val scala3                = "3.0.0-RC3"
    val scalaCollCompat       = "2.4.4"
    val scalaJsDom            = "1.1.0"
    val scalaz72              = "7.2.31"
    val sourcecode            = "0.2.6"

    // Internal
    val betterMonadicFor      = "0.3.1"
    val catsTestkitScalaTest  = "2.1.4"
    val disciplineScalaTest   = "2.1.4"
    val kindProjector         = "0.13.0"
    val macroParadise         = "2.1.1"
    val nyaya                 = "0.10.0-RC2"
    val reactJs               = "17.0.2"
    val scalaJsJavaTime       = "1.0.0"
    val scalaTest             = "3.2.8"
    val sizzleJs              = "2.3.0"
    val univEq                = "1.4.0-RC4"
    val utest                 = "0.7.9"
  }

  object Dep {
    val cats                 = Def.setting("org.typelevel"                 %%% "cats-core"               % Ver.cats)
    val catsEffect           = Def.setting("org.typelevel"                 %%% "cats-effect"             % Ver.catsEffect)
    val catsEffectLaws       = Def.setting("org.typelevel"                 %%% "cats-effect-laws"        % Ver.catsEffect)
    val catsTestkit          = Def.setting("org.typelevel"                 %%% "cats-testkit"            % Ver.cats)
    val catsTestkitScalaTest = Def.setting("org.typelevel"                 %%% "cats-testkit-scalatest"  % Ver.catsTestkitScalaTest)
    val disciplineScalaTest  = Def.setting("org.typelevel"                 %%% "discipline-scalatest"    % Ver.disciplineScalaTest)
    val microlibsCompileTime = Def.setting("com.github.japgolly.microlibs" %%% "compile-time"            % Ver.microlibs)
    val microlibsTestUtil    = Def.setting("com.github.japgolly.microlibs" %%% "test-util"               % Ver.microlibs)
    val monocleCats          = Def.setting("com.github.julien-truffaut"    %%% "monocle-core"            % Ver.monocleCats cross CrossVersion.for3Use2_13)
    val monocle3             = Def.setting("com.github.julien-truffaut"    %%% "monocle-core"            % Ver.monocle3)
    val monocleScalaz        = Def.setting("com.github.julien-truffaut"    %%% "monocle-core"            % Ver.monocleScalaz cross CrossVersion.for3Use2_13)
    val nyayaGen             = Def.setting("com.github.japgolly.nyaya"     %%% "nyaya-gen"               % Ver.nyaya)
    val nyayaProp            = Def.setting("com.github.japgolly.nyaya"     %%% "nyaya-prop"              % Ver.nyaya)
    val nyayaTest            = Def.setting("com.github.japgolly.nyaya"     %%% "nyaya-test"              % Ver.nyaya)
    val scalaCollCompat      = Def.setting("org.scala-lang.modules"        %%% "scala-collection-compat" % Ver.scalaCollCompat)
    val scalaCompiler        = Def.setting("org.scala-lang"                  % "scala-compiler"          % scalaVersion.value)
    val scalaJsDom           = Def.setting("org.scala-js"                  %%% "scalajs-dom"             % Ver.scalaJsDom cross CrossVersion.for3Use2_13)
    val scalaJsJavaTime      = Def.setting("org.scala-js"                  %%% "scalajs-java-time"       % Ver.scalaJsJavaTime cross CrossVersion.for3Use2_13)
    val scalaReflect         = Def.setting("org.scala-lang"                  % "scala-reflect"           % scalaVersion.value)
    val scalaTest            = Def.setting("org.scalatest"                 %%% "scalatest"               % Ver.scalaTest)
    val scalazEffect72       = Def.setting("org.scalaz"                    %%% "scalaz-effect"           % Ver.scalaz72 cross CrossVersion.for3Use2_13)
    val sourcecode           = Def.setting("com.lihaoyi"                   %%% "sourcecode"              % Ver.sourcecode)
    val univEq               = Def.setting("com.github.japgolly.univeq"    %%% "univeq"                  % Ver.univEq)
    val univEqScalaz         = Def.setting("com.github.japgolly.univeq"    %%% "univeq-scalaz"           % Ver.univEq)
    val utest                = Def.setting("com.lihaoyi"                   %%% "utest"                   % Ver.utest)

    // Compiler plugins
    val betterMonadicFor = compilerPlugin("com.olegpy"     %% "better-monadic-for" % Ver.betterMonadicFor)
    val kindProjector    = compilerPlugin("org.typelevel"  %% "kind-projector"     % Ver.kindProjector cross CrossVersion.full)

    def sizzleJs(scope: Configuration) =
      Def.setting("org.webjars.bower" % "sizzle" % Ver.sizzleJs % scope / "sizzle.min.js" commonJSName "Sizzle")

    val react             = ReactArtifact("react")
    val reactDom          = ReactArtifact("react-dom")
    val reactDomServer    = ReactArtifact("react-dom-server.browser")
    val reactDoutestUtils = ReactArtifact("react-dom-test-utils")
  }

  final case class ReactArtifact(filename: String) {
    val dev = s"umd/$filename.development.js"
    val prod = s"umd/$filename.production.min.js"
  }

  def globalDependencyOverrides = Def.setting(Seq(
    Dep.univEq.value,
    Dep.univEqScalaz.value,
  ))

  def addReactJsDependencies(scope: Configuration): Project => Project =
    _.enablePlugins(JSDependenciesPlugin)
      .settings(
        jsDependencies ++= Seq(

          "org.webjars.npm" % "react" % Ver.reactJs % scope
            /        "umd/react.development.js"
            minified "umd/react.production.min.js"
            commonJSName "React",

          "org.webjars.npm" % "react-dom" % Ver.reactJs % scope
            /         "umd/react-dom.development.js"
            minified  "umd/react-dom.production.min.js"
            dependsOn "umd/react.development.js"
            commonJSName "ReactDOM",

          "org.webjars.npm" % "react-dom" % Ver.reactJs % scope
            /         "umd/react-dom-test-utils.development.js"
            minified  "umd/react-dom-test-utils.production.min.js"
            dependsOn "umd/react-dom.development.js"
            commonJSName "ReactTestUtils",

          "org.webjars.npm" % "react-dom" % Ver.reactJs % scope
            /         "umd/react-dom-server.browser.development.js"
            minified  "umd/react-dom-server.browser.production.min.js"
            dependsOn "umd/react-dom.development.js"
            commonJSName "ReactDOMServer"),

        packageJSDependencies / skip := false)
}
