import sbt._
import sbt.Keys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import org.scalajs.jsdependencies.sbtplugin.JSDependenciesPlugin
import org.scalajs.jsdependencies.sbtplugin.JSDependenciesPlugin.autoImport._

object Dependencies {

  object Ver {

    // Externally observable
    val cats                  = "2.12.0"
    val catsEffect            = "3.5.4"
    val microlibs             = "4.1.0"
    val monocle2              = "2.1.0"
    val monocle3              = "3.2.0"
    val scala2                = "2.13.15"
    val scala3                = "3.3.0"
    val scalaJsDom            = "2.8.0"
    val sourcecode            = "0.4.2"

    // Internal
    val betterMonadicFor      = "0.3.1"
    val catsTestkitScalaTest  = "2.1.5"
    val disciplineScalaTest   = "2.1.5"
    val fastTextEncoding      = "1.0.6"
    val kindProjector         = "0.13.3"
    val macrotaskExecutor     = "1.1.1"
    val nyaya                 = "1.0.0"
    val reactJs               = "18.3.1"
    val scalaJsJavaTime       = "1.0.0"
    val scalaJsSecureRandom   = "1.0.0"
    val scalaTest             = "3.2.11"
    val sizzleJs              = "2.3.0"
    val univEq                = "2.0.0"
    val utest                 = "0.8.5"
  }

  object Dep {
    val cats                 = Def.setting("org.typelevel"                 %%% "cats-core"                               % Ver.cats)
    val catsEffect           = Def.setting("org.typelevel"                 %%% "cats-effect"                             % Ver.catsEffect)
    val catsEffectLaws       = Def.setting("org.typelevel"                 %%% "cats-effect-laws"                        % Ver.catsEffect)
    val catsEffectTestkit    = Def.setting("org.typelevel"                 %%% "cats-effect-testkit"                     % Ver.catsEffect)
    val catsTestkit          = Def.setting("org.typelevel"                 %%% "cats-testkit"                            % Ver.cats)
    val catsTestkitScalaTest = Def.setting("org.typelevel"                 %%% "cats-testkit-scalatest"                  % Ver.catsTestkitScalaTest)
    val disciplineScalaTest  = Def.setting("org.typelevel"                 %%% "discipline-scalatest"                    % Ver.disciplineScalaTest)
    val macrotaskExecutor    = Def.setting("org.scala-js"                  %%% "scala-js-macrotask-executor"             % Ver.macrotaskExecutor)
    val microlibsCompileTime = Def.setting("com.github.japgolly.microlibs" %%% "compile-time"                            % Ver.microlibs)
    val microlibsTestUtil    = Def.setting("com.github.japgolly.microlibs" %%% "test-util"                               % Ver.microlibs)
    val microlibsTypes       = Def.setting("com.github.japgolly.microlibs" %%% "types"                                   % Ver.microlibs)
    val monocle2             = Def.setting("com.github.julien-truffaut"    %%% "monocle-core"                            % Ver.monocle2 cross CrossVersion.for3Use2_13 excludeAll(ExclusionRule(organization = "org.typelevel")))
    val monocle3             = Def.setting("dev.optics"                    %%% "monocle-core"                            % Ver.monocle3)
    val nyayaGen             = Def.setting("com.github.japgolly.nyaya"     %%% "nyaya-gen"                               % Ver.nyaya)
    val nyayaProp            = Def.setting("com.github.japgolly.nyaya"     %%% "nyaya-prop"                              % Ver.nyaya)
    val nyayaTest            = Def.setting("com.github.japgolly.nyaya"     %%% "nyaya-test"                              % Ver.nyaya)
    val scalaCompiler        = Def.setting("org.scala-lang"                  % "scala-compiler"                          % scalaVersion.value)
    val scalaJsDom           = Def.setting("org.scala-js"                  %%% "scalajs-dom"                             % Ver.scalaJsDom)
    val scalaJsJavaTime      = Def.setting("org.scala-js"                  %%% "scalajs-java-time"                       % Ver.scalaJsJavaTime cross CrossVersion.for3Use2_13)
    val scalaJsSecureRandom  = Def.setting("org.scala-js"                  %%% "scalajs-fake-insecure-java-securerandom" % Ver.scalaJsSecureRandom cross CrossVersion.for3Use2_13)
    val scalaReflect         = Def.setting("org.scala-lang"                  % "scala-reflect"                           % scalaVersion.value)
    val scalaTest            = Def.setting("org.scalatest"                 %%% "scalatest"                               % Ver.scalaTest)
    val sourcecode           = Def.setting("com.lihaoyi"                   %%% "sourcecode"                              % Ver.sourcecode)
    val univEq               = Def.setting("com.github.japgolly.univeq"    %%% "univeq"                                  % Ver.univEq)
    val univEqCats           = Def.setting("com.github.japgolly.univeq"    %%% "univeq-cats"                             % Ver.univEq)
    val utest                = Def.setting("com.lihaoyi"                   %%% "utest"                                   % Ver.utest)

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

  def fastTextEncodingJs = "text.min.js" // 1.0.6 webjar only contains minified version

  def globalDependencyOverrides = Def.setting(Seq(
    Dep.scalaJsDom.value,
    Dep.univEq.value,
    Dep.univEqCats.value,
    "org.webjars.npm" % "scheduler" % "0.22.0", // Required for React 18.3.1
  ))

  final case class ReactArtifact(filename: String) {
    val dev = s"umd/$filename.development.js"
    val prod = s"umd/$filename.production.min.js"
  }

  def addReactJsDependencies(scope: Configuration): Project => Project =
    _.enablePlugins(JSDependenciesPlugin)
      .settings(
        jsDependencies ++= Seq(

          /** For testing React 18 */
          "org.webjars.npm" % "fast-text-encoding" % Ver.fastTextEncoding % scope
          / fastTextEncodingJs
          minified "text.min.js",

          "org.webjars.npm" % "react" % Ver.reactJs % scope
            /         "umd/react.development.js"
            minified  "umd/react.production.min.js"
            dependsOn fastTextEncodingJs
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
            /         "umd/react-dom-server-legacy.browser.development.js"
            minified  "umd/react-dom-server-legacy.browser.production.min.js"
            dependsOn "umd/react-dom.development.js"
            commonJSName "ReactDOMServer"),

        packageJSDependencies / skip := false)
}
