import sbt._
import sbt.Keys._
import org.scalajs.jsdependencies.sbtplugin.JSDependenciesPlugin.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import scalafix.sbt.BuildInfo.{scalafixVersion => ScalafixVer}
import scalafix.sbt.ScalafixPlugin
import scalafix.sbt.ScalafixPlugin.autoImport._

object ScalaJsReact {
  import Dependencies._
  import Lib._

  lazy val root = Project("root", file("."))
    .settings(
      name := "scalajs-react",
      crossScalaVersions := Nil,
    )
    .configure(commonSettings, preventPublication, hasNoTests)
    .aggregate(
      callback,
      callbackExtCats,
      callbackExtCatsEffect,
      coreDefCallback,
      coreDefCatsEffect,
      coreExtCats,
      coreExtCatsEffect,
      coreGeneric,
      extra,
      extraExtMonocle2,
      extraExtMonocle3,
      facadeMain,
      facadeTest,
      ghpages,
      ghpagesMacros,
      scalafixRules,
      tests,
      testUtil,
      util,
      utilCatsEffect,
      utilDummyDefaults,
      utilFallbacks,
    )

  def commonSettings: PE =
    _.enablePlugins(ScalaJSPlugin, ScalafixPlugin)
      .dependsOn(scalafixRules % ScalafixConfig)
      .configure(commonSettingsWithoutPlugins)

  def shimDummyDefaults: PE =
    _.dependsOn(utilDummyDefaults % Provided)

  def prohibitDefaultEffects: PE =
    _.settings(scalafixOnCompile := scalaVersion.value.startsWith("2")) // for ProhibitDefaultEffects

  def effectGenericModule: PE =
    _.configure(shimDummyDefaults, prohibitDefaultEffects)

  lazy val genHooks = TaskKey[Unit]("genHooks")

  // ==============================================================================================

  lazy val scalafixRules = project
    .disablePlugins(ScalafixPlugin)
    .configure(commonSettingsWithoutPlugins)
    .settings(
      libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % ScalafixVer,
      disable := scalaVersion.value.startsWith("3"),
    )
    .configure(conditionallyDisable) // keep this last

  lazy val callback = project
    .dependsOn(util, utilFallbacks % Provided)
    .configure(commonSettings, publicationSettings, utestSettings, prohibitDefaultEffects)
    .settings(
      libraryDependencies += Dep.cats.value % Test,
    )

  lazy val callbackExtCats = project
    .dependsOn(callback)
    .configure(commonSettings, publicationSettings, hasNoTests, prohibitDefaultEffects)
    .settings(
      moduleName := "callback-ext-cats",
      libraryDependencies += Dep.cats.value,
    )

  lazy val callbackExtCatsEffect = project
    .dependsOn(callbackExtCats, utilCatsEffect)
    .configure(commonSettings, publicationSettings, prohibitDefaultEffects)
    .settings(
      moduleName := "callback-ext-cats-effect",
      libraryDependencies ++= Seq(
        Dep.catsEffect          .value,
        Dep.catsEffectLaws      .value % Test,
        Dep.catsEffectTestkit   .value % Test,
        Dep.catsTestkit         .value % Test,
        Dep.catsTestkitScalaTest.value % Test,
        Dep.disciplineScalaTest .value % Test,
        Dep.scalaTest           .value % Test,
      ),
    )

  lazy val coreDefCallback = project
    .dependsOn(callback) // High priority
    .dependsOn(coreGeneric) // Med priority
    .dependsOn(utilFallbacks) // Low priority
    .configure(commonSettings, publicationSettings, definesMacros, hasNoTests, disableScalaDoc3)
    .settings(
      moduleName := "core",
    )

  lazy val coreDefCatsEffect = project
    .dependsOn(coreExtCatsEffect) // High priority
    .dependsOn(utilFallbacks) // Low priority
    .configure(commonSettings, publicationSettings, definesMacros, hasNoTests, disableScalaDoc3)
    .settings(
      moduleName := "core-cats-effect",
    )

  lazy val coreExtCats = project
    .dependsOn(coreGeneric)
    .configure(commonSettings, publicationSettings, hasNoTests, effectGenericModule)
    .settings(
      moduleName := "core-ext-cats",
      libraryDependencies += Dep.cats.value,
    )

  lazy val coreExtCatsEffect = project
    .dependsOn(coreExtCats, utilCatsEffect, utilFallbacks % Provided)
    .configure(commonSettings, publicationSettings, hasNoTests, effectGenericModule)
    .settings(
      moduleName := "core-ext-cats-effect",
      libraryDependencies += Dep.catsEffect.value,
    )

  lazy val coreGeneric = project
    .dependsOn(facadeMain, util)
    .configure(commonSettings, publicationSettings, definesMacros, hasNoTests, disableScalaDoc3, effectGenericModule)
    .settings(
      moduleName := "core-generic",
      libraryDependencies ++= Seq(
        Dep.microlibsTypes.value,
        Dep.sourcecode.value,
      ),
      genHooks := GenHooks(sourceDirectory.value / "main" / "scala"),
    )

  lazy val extra = project
    .dependsOn(coreGeneric)
    .configure(commonSettings, publicationSettings, definesMacros, hasNoTests, effectGenericModule)

  lazy val extraExtMonocle2 = project
    .dependsOn(extra, coreExtCats)
    .configure(commonSettings, publicationSettings, hasNoTests, effectGenericModule)
    .settings(
      moduleName := "extra-ext-monocle2",
      libraryDependencies += Dep.monocle2.value,
    )

  lazy val extraExtMonocle3 = project
    .dependsOn(extra, coreExtCats)
    .configure(commonSettings, publicationSettings, hasNoTests, effectGenericModule)
    .settings(
      moduleName := "extra-ext-monocle3",
      libraryDependencies += Dep.monocle3.value,
    )

  lazy val facadeMain = project
    .configure(commonSettings, publicationSettings, hasNoTests, disableScalaDoc3)
    .settings(
      moduleName := "facade",
      libraryDependencies += Dep.scalaJsDom.value,
    )

  lazy val facadeTest = project
    .configure(commonSettings, publicationSettings, hasNoTests, disableScalaDoc3)
    .dependsOn(facadeMain)
    .settings(
      moduleName := "facade-test",
    )

  lazy val ghpages = project
    .dependsOn(coreDefCallback, extra, extraExtMonocle3, ghpagesMacros)
    .configure(commonSettings, addReactJsDependencies(Compile), preventPublication, hasNoTests)
    .settings(
      scalaJSUseMainModuleInitializer := true,
      Compile / mainClass := Some("ghpages.GhPages"),
      Compile / fullOptJS / artifactPath := file("ghpages/res/ghpages.js"),
      Compile / fullOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false) },
    )

  lazy val ghpagesMacros = project
    .configure(commonSettings, preventPublication, hasNoTests, definesMacros)
    .settings(
      libraryDependencies += Dep.sourcecode.value,
    )

  lazy val tests = project
    .dependsOn(testUtil, coreExtCatsEffect, extraExtMonocle3)
    .dependsOn(coreDefCallback) // Low priority
    .configure(commonSettings, preventPublication, utestSettings, addReactJsDependencies(Test))
    .settings(
      Test / scalacOptions --= Seq(
        "-deprecation",
        "-Xlint:adapted-args"
      ),
      libraryDependencies ++= Seq(
        Dep.nyayaProp.value % Test,
        Dep.nyayaGen.value % Test,
        Dep.nyayaTest.value % Test,
        Dep.scalaJsJavaTime.value % Test,
      ),
      jsDependencies ++= Seq(
        Dep.sizzleJs(Test).value,
        (ProvidedJS / "component-es6.js" dependsOn Dep.reactDom.dev) % Test,
        (ProvidedJS / "component-fn.js"  dependsOn Dep.reactDom.dev) % Test,
        (ProvidedJS / "forward-ref.js"   dependsOn Dep.reactDom.dev) % Test,
        (ProvidedJS / "polyfill.js"      dependsOn Dep.reactDom.dev) % Test,
      ),
    )

  lazy val testUtil = project
    .dependsOn(coreGeneric, extra, facadeTest)
    .configure(commonSettings, publicationSettings, hasNoTests, effectGenericModule)
    .settings(
      moduleName := "test",
    )

  lazy val util = project
    .dependsOn(utilFallbacks % Provided)
    .configure(commonSettings, publicationSettings, hasNoTests, disableScalaDoc3, prohibitDefaultEffects)
    .settings(
      libraryDependencies += Dep.scalaJsDom.value,
    )

  lazy val utilCatsEffect = project
    .dependsOn(util)
    .configure(commonSettings, publicationSettings, hasNoTests, prohibitDefaultEffects)
    .settings(
      moduleName := "util-cats-effect",
      libraryDependencies += Dep.catsEffect.value,
    )

  lazy val utilDummyDefaults = project
    .dependsOn(util, utilFallbacks)
    .configure(commonSettings, preventPublication, hasNoTests)

  lazy val utilFallbacks = project
    .configure(commonSettings, publicationSettings, hasNoTests, prohibitDefaultEffects)
    .settings(
      moduleName := "util-fallbacks",
    )

/*
  lazy val testModule = project.in(file("test-module"))
    .configure(commonSettings, useScalaJsBundler, preventPublication, utestSettings)
    .dependsOn(core, extra, test)
    .settings(
      name := "test-module",
      npmDependencies in Test ++= Seq(
        "react"                             -> Ver.reactJs,
        "react-dom"                         -> Ver.reactJs,
        "react-addons-perf"                 -> "15.5.0-rc.2",
        "react-addons-css-transition-group" -> "16.7.0"))
*/

}
