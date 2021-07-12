import java.util.Properties
import org.scalajs.linker.interface._
import Dependencies._
import Lib._

ThisBuild / organization := "com.github.japgolly.scalajs-react-test"
ThisBuild / shellPrompt  := ((s: State) => Project.extract(s).currentRef.project + "> ")

def scalacCommonFlags: Seq[String] = Seq(
  "-deprecation",
  "-feature",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-unchecked",
)

def scalac2Flags = Seq(
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
)

def commonSettings: Project => Project = _
  .configure(preventPublication)
  .settings(
    scalaVersion       := Ver.scala3,
    crossScalaVersions := Seq(Ver.scala2, Ver.scala3),
    scalacOptions     ++= scalacCommonFlags,
    scalacOptions     ++= byScalaVersion {
                            case (2, _) => scalac2Flags
                            case (3, _) => scalac3Flags
                          }.value,
  )

lazy val cleanTestAll = taskKey[Unit]("cleanTestAll")

val enableJSCE = System.getProperty("downstream_tests.enableJSCE") != null

lazy val root = Project("root", file("."))
  .configure(commonSettings)
  .aggregate(macros, jvm, js, jsCE)
  .settings(
    cleanTestAll := (
      if (enableJSCE) // How to do this in a better way?
        Def.sequential(
          macros        / clean,
          jvm           / clean,
          js            / clean,
          jsCE          / clean,
                   Test / compile,
          jvm    / Test / test,
          js     / Test / test,
          jsCE   / Test / test,
        ).value
      else
        Def.sequential(
          macros        / clean,
          jvm           / clean,
          js            / clean,
                   Test / compile,
          jvm    / Test / test,
          js     / Test / test,
        ).value
    ),
  )

lazy val macros = project
  .in(file("macros"))
  .enablePlugins(ScalaJSPlugin)
  .configure(commonSettings, definesMacros)
  .settings(
    libraryDependencies += Dep.microlibsCompileTime.value,
  )

val useFullOptJS = System.getProperty("downstream_tests.fullOptJS") != null
val jsStage      = if (useFullOptJS) FullOptStage else FastOptStage
val jsOptKey     = if (useFullOptJS) fullOptJS else fastOptJS

lazy val jvm = project
  .in(file("jvm"))
  .configure(commonSettings, utestSettings)
  .settings(
    libraryDependencies ++= Seq(
      Dep.microlibsTestUtil.value % Test,
    ),
    Test / fork := true,
    Test / javaOptions ++=
      sys.props.iterator
        .filter(_._1.matches("(downstream_tests|japgolly).*"))
        .map(x => s"-D${x._1}=${x._2}")
        .toSeq,
    Test / javaOptions += {
      val jsFile = (js / Compile / jsOptKey).value
      s"-Djs_file=${jsFile.data.absolutePath}"
    },
  )

lazy val js = project
  .in(file("js"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(macros)
  .configure(commonSettings, utestSettings, addReactJsDependencies(Test))
  .settings(
    scalaJSStage := jsStage,
    libraryDependencies ++= {
      val ver = version.value.stripSuffix("-SNAPSHOT") + "-SNAPSHOT"
      Seq(
        "com.github.japgolly.scalajs-react" %%% "core" % ver,
        "com.github.japgolly.scalajs-react" %%% "core-ext-cats_effect" % ver,
        "com.github.japgolly.scalajs-react" %%% "extra" % ver,
        "com.github.japgolly.scalajs-react" %%% "test" % ver % Test,
        Dep.microlibsCompileTime.value % Test,
        Dep.microlibsTestUtil.value % Test,
        Dep.scalaJsJavaTime.value % Test,
      )
    },
    jsDependencies += (ProvidedJS / "polyfill.js") % Test,
    scalaJSLinkerConfig ~= { _
      .withSemantics(_
        .withRuntimeClassNameMapper(Semantics.RuntimeClassNameMapper.discardAll())
      )
    },
  )

lazy val jsCE = project
  .in(file("js-ce"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(macros)
  .configure(commonSettings, utestSettings, addReactJsDependencies(Test))
  .settings(
    scalaJSStage := jsStage,
    libraryDependencies ++= {
      val ver = version.value.stripSuffix("-SNAPSHOT") + "-SNAPSHOT"
      Seq(
        "com.github.japgolly.scalajs-react" %%% "core-bundle-cats_effect" % ver,
        "com.github.japgolly.scalajs-react" %%% "extra" % ver,
        "com.github.japgolly.scalajs-react" %%% "test" % ver % Test,
        Dep.microlibsCompileTime.value % Test,
        Dep.microlibsTestUtil.value % Test,
        Dep.scalaJsJavaTime.value % Test,
      )
    },
    jsDependencies += (ProvidedJS / "polyfill.js") % Test,
  )

lazy val generic = project
  .enablePlugins(ScalaJSPlugin)
  .configure(commonSettings)
  .settings(
    scalaJSStage := jsStage,
    libraryDependencies ++= {
      val ver = version.value.stripSuffix("-SNAPSHOT") + "-SNAPSHOT"
      Seq(
        "com.github.japgolly.scalajs-react" %%% "core-generic" % ver,
        "com.github.japgolly.scalajs-react" %%% "util-dummy-defaults" % ver % Provided,
      )
    },
  )
