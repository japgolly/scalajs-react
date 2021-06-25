import sbt._
import sbt.Keys._
import com.jsuereth.sbtpgp.PgpKeys
import com.jsuereth.sbtpgp.PgpKeys._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.scalajs.jsdependencies.sbtplugin.JSDependenciesPlugin.autoImport._
import sbtrelease.ReleasePlugin.autoImport._
import scalafix.sbt.ScalafixPlugin

object ScalaJsReact {
  import Dependencies._
  import Lib._

  def scalacCommonFlags: Seq[String] = Seq(
    "-deprecation",
    "-feature",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-unchecked",                                    // Enable additional warnings where generated code depends on assumptions.
    "-Yno-generic-signatures",                       // Suppress generation of generic signatures for Java.
  )

  def scalac213Flags = Seq(
    "-opt:l:inline",
    "-opt-inline-from:japgolly.scalajs.react.**",
    "-Wconf:msg=may.not.be.exhaustive:e",            // Make non-exhaustive matches errors instead of warnings
    "-Wconf:msg=Reference.to.uninitialized.value:e", // Make uninitialised value calls errors instead of warnings
    "-Wunused:explicits",                            // Warn if an explicit parameter is unused.
    "-Wunused:implicits",                            // Warn if an implicit parameter is unused.
    "-Wunused:imports",                              // Warn if an import selector is not referenced.
    "-Wunused:locals",                               // Warn if a local definition is unused.
    "-Wunused:nowarn",                               // Warn if a @nowarn annotation does not suppress any warnings.
    "-Wunused:patvars",                              // Warn if a variable bound in a pattern is unused.
    "-Wunused:privates",                             // Warn if a private member is unused.
    "-Xlint:adapted-args",                           // An argument list was modified to match the receiver.
    "-Xlint:constant",                               // Evaluation of a constant arithmetic expression resulted in an error.
    "-Xlint:delayedinit-select",                     // Selecting member of DelayedInit.
    "-Xlint:deprecation",                            // Enable -deprecation and also check @deprecated annotations.
    "-Xlint:eta-zero",                               // Usage `f` of parameterless `def f()` resulted in eta-expansion, not empty application `f()`.
    "-Xlint:implicit-not-found",                     // Check @implicitNotFound and @implicitAmbiguous messages.
    "-Xlint:inaccessible",                           // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any",                              // A type argument was inferred as Any.
    "-Xlint:missing-interpolator",                   // A string literal appears to be missing an interpolator id.
    "-Xlint:nonlocal-return",                        // A return statement used an exception for flow control.
    "-Xlint:nullary-unit",                           // `def f: Unit` looks like an accessor; add parens to look side-effecting.
    "-Xlint:option-implicit",                        // Option.apply used an implicit view.
    "-Xlint:poly-implicit-overload",                 // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow",                         // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align",                            // In a pattern, a sequence wildcard `_*` should match all of a repeated parameter.
    "-Xlint:valpattern",                             // Enable pattern checks in val definitions.
    "-Xmixin-force-forwarders:false",                // Only generate mixin forwarders required for program correctness.
    "-Yjar-compression-level", "9",                  // compression level to use when writing jar files
    "-Ymacro-annotations",                           // Enable support for macro annotations, formerly in macro paradise.
    "-Ypatmat-exhaust-depth", "off",
  )

  def scalac3Flags = Seq(
    "-source:3.0-migration",
    "-Ykind-projector",
    // "-Xprint:all",
  )

  def commonSettings: PE =
    _.enablePlugins(ScalaJSPlugin, ScalafixPlugin)
      .settings(
        scalaVersion                  := Ver.scala2,
        crossScalaVersions            := Seq(Ver.scala2, Ver.scala3),
        scalacOptions                ++= scalacCommonFlags,
        scalacOptions                ++= byScalaVersion {
                                           case (2, 13) => scalac213Flags
                                           case (3, _ ) => scalac3Flags
                                         }.value,
        //scalacOptions               += "-Xlog-implicits",
        incOptions                    := incOptions.value.withLogRecompileOnMacro(false),
        updateOptions                 := updateOptions.value.withCachedResolution(true),
        releasePublishArtifactsAction := PgpKeys.publishSigned.value,
        releaseTagComment             := s"v${(ThisBuild / version).value}",
        releaseVcsSign                := true,
        libraryDependencies          ++= Seq(Dep.betterMonadicFor, Dep.kindProjector).filter(_ => scalaVersion.value startsWith "2"),
        disable                       := false,
        dependencyOverrides          ++= globalDependencyOverrides.value,
      )

  def shimDummyDefaults: PE =
    _.dependsOn(utilDummyDefaults % Provided)

  lazy val genHooks = TaskKey[Unit]("genHooks")

  // ==============================================================================================

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
      tests,
      testUtil,
      util,
      utilCatsEffect,
      utilDummyDefaults,
    )

  // ==============================================================================================

  lazy val callback = project
    .dependsOn(util)
    .configure(commonSettings, publicationSettings, utestSettings)
    .settings(
      libraryDependencies += Dep.cats.value % Test,
    )

  lazy val callbackExtCats = project
    .dependsOn(callback)
    .configure(commonSettings, publicationSettings, hasNoTests)
    .settings(
      moduleName := "callback-ext-cats",
      libraryDependencies += Dep.cats.value,
    )

  lazy val callbackExtCatsEffect = project
    .dependsOn(callbackExtCats, utilCatsEffect)
    .configure(commonSettings, publicationSettings)
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
    .dependsOn(coreGeneric) // Low priority
    .configure(commonSettings, publicationSettings, definesMacros, hasNoTests, disableScalaDoc3)
    .settings(
      moduleName := "core",
    )

  lazy val coreDefCatsEffect = project
    .dependsOn(coreExtCatsEffect)
    .configure(commonSettings, publicationSettings, definesMacros, hasNoTests, disableScalaDoc3)
    .settings(
      moduleName := "core-cats-effect",
    )

  lazy val coreExtCats = project
    .dependsOn(coreGeneric)
    .configure(commonSettings, publicationSettings, hasNoTests)
    .configure(shimDummyDefaults)
    .settings(
      moduleName := "core-ext-cats",
      libraryDependencies += Dep.cats.value,
    )

  lazy val coreExtCatsEffect = project
    .dependsOn(coreExtCats, utilCatsEffect)
    .configure(commonSettings, publicationSettings, hasNoTests)
    .configure(shimDummyDefaults)
    .settings(
      moduleName := "core-ext-cats-effect",
      libraryDependencies += Dep.catsEffect.value,
    )

  lazy val coreGeneric = project
    .dependsOn(facadeMain, util)
    .configure(commonSettings, publicationSettings, definesMacros, hasNoTests, disableScalaDoc3)
    .configure(shimDummyDefaults)
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
    .configure(commonSettings, publicationSettings, definesMacros, hasNoTests)
    .configure(shimDummyDefaults)

  lazy val extraExtMonocle2 = project
    .dependsOn(extra, coreExtCats)
    .configure(commonSettings, publicationSettings, hasNoTests)
    .configure(shimDummyDefaults)
    .settings(
      moduleName := "extra-ext-monocle2",
      libraryDependencies += Dep.monocle2.value,
    )

  lazy val extraExtMonocle3 = project
    .dependsOn(extra, coreExtCats)
    .configure(commonSettings, publicationSettings, hasNoTests)
    .configure(shimDummyDefaults)
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
    .dependsOn(coreDefCallback, testUtil)
    .dependsOn(extraExtMonocle3 % "test->compile")
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
    .configure(commonSettings, publicationSettings, hasNoTests)
    .configure(shimDummyDefaults)
    .settings(
      moduleName := "test",
    )

  lazy val util = project
    .configure(commonSettings, publicationSettings, hasNoTests)
    .settings(
      libraryDependencies += Dep.scalaJsDom.value,
    )

  lazy val utilCatsEffect = project
    .dependsOn(util)
    .configure(commonSettings, publicationSettings, hasNoTests)
    .settings(
      moduleName := "util-cats-effect",
      libraryDependencies += Dep.catsEffect.value,
    )

  lazy val utilDummyDefaults = project
    .dependsOn(util)
    .configure(commonSettings, preventPublication, hasNoTests)

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
