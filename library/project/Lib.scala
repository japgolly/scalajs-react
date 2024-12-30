import sbt._
import Keys._
import com.jsuereth.sbtpgp.PgpKeys
import com.jsuereth.sbtpgp.PgpKeys._
import org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import xerial.sbt.Sonatype.autoImport._
import Dependencies._

object Lib {

  type PE = Project => Project

  val ghProject = "scalajs-react"

  def scalacCommonFlags: Seq[String] = Seq(
    "-deprecation",
    "-feature",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-unchecked",                                    // Enable additional warnings where generated code depends on assumptions.
    "-Wconf:msg=may.not.be.exhaustive:e",            // Make non-exhaustive matches errors instead of warnings
    "-Wconf:msg=Reference.to.uninitialized.value:e", // Make uninitialised value calls errors instead of warnings
    "-Yno-generic-signatures",                       // Suppress generation of generic signatures for Java.
  )

  def scalac2Flags = Seq(
    "-opt:l:inline",
    "-opt-inline-from:japgolly.scalajs.react.**",
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
    "-Wconf:msg=unused:s", // Scala 3.1 doesn't support @nowarn("cat=unused")
    "-Ykind-projector",
    // "-Xprint:all",
  )

  def commonSettingsWithoutPlugins: PE = _
    .configure(sourceMapsToGithub(ghProject))
    .settings(
      scalaVersion                  := Ver.scala2,
      crossScalaVersions            := Seq(Ver.scala2, Ver.scala3),
      scalacOptions                ++= scalacCommonFlags,
      scalacOptions                ++= byScalaVersion {
                                         case (2, _) => scalac2Flags
                                         case (3, _) => scalac3Flags
                                       }.value,
      //scalacOptions               += "-Xlog-implicits",
      incOptions                    := incOptions.value.withLogRecompileOnMacro(false),
      updateOptions                 := updateOptions.value.withCachedResolution(true),
      libraryDependencies          ++= Seq(Dep.betterMonadicFor, Dep.kindProjector).filter(_ => scalaVersion.value startsWith "2"),
      disable                       := false,
      dependencyOverrides          ++= globalDependencyOverrides.value,
    )

  def byScalaVersion[A](f: PartialFunction[(Long, Long), Seq[A]]): Def.Initialize[Seq[A]] =
    Def.setting(CrossVersion.partialVersion(scalaVersion.value).flatMap(f.lift).getOrElse(Nil))

  def publicationSettings: PE =
    _.settings(
    developers := List(
      Developer("japgolly", "David Barri", "japgolly@gmail.com", url("https://japgolly.github.io/japgolly/")),
    )
  )

  def sourceMapsToGithub(ghProject: String): PE =
    p => p.settings(
      scalacOptions ++= {
        val isDotty = scalaVersion.value startsWith "3"
        val ver     = version.value
        if (isSnapshot.value)
          Nil
        else {
          val a = p.base.toURI.toString.replaceFirst("[^/]+/?$", "")
          val g = s"https://raw.githubusercontent.com/japgolly/$ghProject"
          val flag = if (isDotty) "-scalajs-mapSourceURI" else "-P:scalajs:mapSourceURI"
          s"$flag:$a->$g/v$ver/library/" :: Nil
        }
      }
    )

  def preventPublication: PE =
    _.settings(publish / skip := true)

  def utestSettings(scope: Configuration): PE =
    _.configure(InBrowserTesting.js)
      .settings(
        jsEnv                := new JSDOMNodeJSEnv(JSDOMNodeJSEnv.Config().withArgs("--experimental-worker" :: Nil)),
        Test / scalacOptions += "-language:reflectiveCalls",
        libraryDependencies  += Dep.utest.value % scope,
        libraryDependencies  += Dep.microlibsTestUtil.value % scope,
        testFrameworks       += new TestFramework("utest.runner.Framework"))

  def utestSettings: PE =
    utestSettings(Test)

  def definesMacros: Project => Project =
    _.settings(
      scalacOptions       ++= (if (scalaVersion.value startsWith "3") Nil else Seq("-language:experimental.macros")),
      libraryDependencies ++= (if (scalaVersion.value startsWith "3") Nil else Seq(Dep.scalaReflect.value, Dep.scalaCompiler.value % Provided)),
      libraryDependencies  += Dep.microlibsCompileTime.value,
    )

  def hasNoTests: Project => Project =
    _.settings(
      Test / fastOptJS     := Attributed((Test / fastOptJS / artifactPath).value)(AttributeMap.empty),
      Test / fullOptJS     := Attributed((Test / fullOptJS / artifactPath).value)(AttributeMap.empty),
      Test / sbt.Keys.test := { (Test / compile).value; () },
      Test / testOnly      := { (Test / compile).value; () },
      Test / testQuick     := { (Test / compile).value; () })

  /*
  lazy val yarnOnPath: Boolean =
    try {
      Process("yarn --version").!!
      true
    } catch {
      case t: Throwable => false
    }

  def useScalaJsBundler: PE =
    _.enablePlugins(ScalaJSBundlerPlugin)
      .settings(
        // useYarn := yarnOnPath,
        version in webpack := "2.6.1")
  */

  def disableScalaDoc3: PE =
    _.settings(
      Compile / doc / sources := { if (scalaVersion.value startsWith "3") Seq.empty else (Compile / doc / sources).value },
    )

  val disable = settingKey[Boolean]("Disable project?")

  def conditionallyDisable: Project => Project = {

    def clearWhenDisabled[A](key: SettingKey[Seq[A]]) =
      Def.setting[Seq[A]] {
        val disabled = disable.value
        val as = key.value
        if (disabled) Nil else as
      }

    _.settings(
      libraryDependencies := clearWhenDisabled(libraryDependencies).value,
      Compile / unmanagedSourceDirectories := clearWhenDisabled(Compile / unmanagedSourceDirectories).value,
      Test / unmanagedSourceDirectories := clearWhenDisabled(Test / unmanagedSourceDirectories).value,
      publish / skip := ((publish / skip).value || disable.value),
    )
  }
}