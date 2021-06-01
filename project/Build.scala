import sbt._
import sbt.Keys._
import com.jsuereth.sbtpgp.PgpKeys
import com.jsuereth.sbtpgp.PgpKeys._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.scalajs.jsdependencies.sbtplugin.JSDependenciesPlugin.autoImport._
import sbtrelease.ReleasePlugin.autoImport._
import scalafix.sbt.ScalafixPlugin

object ScalajsReact {
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

  def scalac212Flags = Seq(
    "-opt:l:inline",
    "-opt-inline-from:japgolly.scalajs.react.**",
    "-Ypatmat-exhaust-depth", "off",
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
        scalaVersion                  := Ver.scala3,
        crossScalaVersions            := Seq(Ver.scala213, Ver.scala3),
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

  // ==============================================================================================

  lazy val root = Project("root", file("."))
    .settings(
      name := "scalajs-react",
      crossScalaVersions := Nil,
    )
    .configure(commonSettings, preventPublication, hasNoTests)
    .aggregate(
      core,
      extra,
      test,
      // testModule,
      scalaz72,
      cats,
      catsEffect,
      monocleScalaz,
      monocleCats,
      monocle3,
      ghpagesMacros,
      // ghpages,
    )

  // ==============================================================================================

  lazy val genHooks = TaskKey[Unit]("genHooks")

  lazy val core = project
    .configure(commonSettings, publicationSettings, definesMacros, hasNoTests)
    .settings(
      name := "core",
      libraryDependencies ++= Seq(
        Dep.scalaCollCompat.value,
        Dep.scalaJsDom.value,
        Dep.sourcecode.value,
      ),
      genHooks := GenHooks(sourceDirectory.value / "main" / "scala"),
    )

  lazy val extra = project
    .configure(commonSettings, publicationSettings, definesMacros, hasNoTests)
    .dependsOn(core)
    .settings(name := "extra")

  lazy val test = project
    .configure(commonSettings, publicationSettings, utestSettings, addReactJsDependencies(Test))
    .dependsOn(core, extra)
    .dependsOn(scalaz72 % "test->compile")
    .dependsOn(monocleScalaz % "test->compile")
    .dependsOn(cats % "test->compile")
    .settings(
      name := "test",
      Test / scalacOptions --= Seq(
        "-deprecation",
        "-Xlint:adapted-args"
      ),
      libraryDependencies ++= Seq(
        Dep.microlibsTestUtil .value % Test,
        Dep.nyayaProp         .value % Test,
        Dep.nyayaGen          .value % Test,
        Dep.nyayaTest         .value % Test,
        Dep.monocleScalaz     .value % Test,
        Dep.scalaJsJavaTime   .value % Test),
      jsDependencies ++= Seq(
        Dep.sizzleJs(Test).value,
        (ProvidedJS / "component-es6.js" dependsOn Dep.reactDom.dev) % Test,
        (ProvidedJS / "component-fn.js"  dependsOn Dep.reactDom.dev) % Test,
        (ProvidedJS / "forward-ref.js"   dependsOn Dep.reactDom.dev) % Test,
        (ProvidedJS / "polyfill.js"      dependsOn Dep.reactDom.dev) % Test))

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


  lazy val scalaz72 = project
    .in(file("scalaz-7.2"))
    .configure(commonSettings, publicationSettings, extModuleName("scalaz72"), hasNoTests)
    .dependsOn(core, extra)
    .settings(
      libraryDependencies += Dep.scalazEffect72.value)


  lazy val monocleScalaz = project
    .in(file("monocle-scalaz"))
    .configure(commonSettings, publicationSettings, extModuleName("monocle-scalaz"), hasNoTests)
    .dependsOn(core, extra, scalaz72)
    .settings(
      libraryDependencies += Dep.monocleScalaz.value)

  lazy val cats = project
    .configure(commonSettings, publicationSettings, extModuleName("cats"), hasNoTests)
    .dependsOn(core, extra)
    .settings(
      libraryDependencies += Dep.cats.value)


  lazy val monocleCats = project
    .in(file("monocle-cats"))
    .configure(commonSettings, publicationSettings, extModuleName("monocle-cats"), hasNoTests)
    .dependsOn(core, extra, cats)
    .settings(
      disable := scalaVersion.value.startsWith("3"),
      // Share the internal source code files with this module
      Compile / unmanagedSourceDirectories += (monocleScalaz / Compile / sourceDirectory).value / "scala" / "japgolly" / "scalajs" / "react" / "internal",
      libraryDependencies += Dep.monocleCats.value,
    )
    .configure(conditionallyDisable) // keep this last

  lazy val catsEffect = project
    .in(file("cats-effect"))
    .configure(commonSettings, publicationSettings, extModuleName("cats-effect"))
    .dependsOn(core, cats)
    .settings(
      libraryDependencies ++= Seq(
        Dep.cats                .value,
        Dep.catsEffect          .value,
        Dep.catsEffectLaws      .value % Test,
        Dep.catsTestkit         .value % Test,
        Dep.catsTestkitScalaTest.value % Test,
        Dep.scalaTest           .value % Test,
        Dep.disciplineScalaTest .value % Test))

  lazy val monocle3 = project
    .in(file("monocle3"))
    .configure(commonSettings, publicationSettings, extModuleName("monocle3"), hasNoTests)
    .dependsOn(core, extra, cats)
    .settings(
      disable := scalaVersion.value.startsWith("2.12"),
      libraryDependencies += Dep.monocle3.value,
    )
    .configure(conditionallyDisable) // keep this last

  // ===================================================================================================================

  lazy val ghpagesMacros = Project("gh-pages-macros", file("gh-pages-macros"))
    .configure(commonSettings, preventPublication, hasNoTests, definesMacros)
    .settings(
      libraryDependencies ++= Seq(
        Dep.sourcecode.value,
        Dep.scalaCollCompat.value))

  lazy val ghpages = Project("gh-pages", file("gh-pages"))
    .dependsOn(core, extra, monocleScalaz, ghpagesMacros)
    .configure(commonSettings, addReactJsDependencies(Compile), preventPublication, hasNoTests)
    .settings(
      libraryDependencies += Dep.monocleScalaz.value,
      scalaJSLinkerConfig ~= { _.withSourceMap(false) },
      scalaJSUseMainModuleInitializer := true,
      Compile / mainClass := Some("ghpages.GhPages"),
      Compile / fullOptJS / artifactPath := file("gh-pages/res/ghpages.js"))

}