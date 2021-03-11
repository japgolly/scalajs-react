import sbt._
import sbt.Keys._
import com.jsuereth.sbtpgp.PgpKeys
import com.jsuereth.sbtpgp.PgpKeys._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv
import org.scalajs.jsdependencies.sbtplugin.JSDependenciesPlugin.autoImport._
import sbtrelease.ReleasePlugin.autoImport._
import scalafix.sbt.ScalafixPlugin
import xerial.sbt.Sonatype.autoImport._
//import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
//import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

object ScalajsReact {
  import Dependencies._

  type PE = Project => Project

  def byScalaVersion[A](f: PartialFunction[(Long, Long), Seq[A]]): Def.Initialize[Seq[A]] =
    Def.setting(CrossVersion.partialVersion(scalaVersion.value).flatMap(f.lift).getOrElse(Nil))

  def scalacFlags: Seq[String] = Seq(
    "-deprecation",
    "-feature",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-opt:l:inline",
    "-opt-inline-from:japgolly.scalajs.react.**",
    "-unchecked",                                    // Enable additional warnings where generated code depends on assumptions.
    "-Yno-generic-signatures",                       // Suppress generation of generic signatures for Java.
    "-Ypatmat-exhaust-depth", "off")

  def scalac213Flags = Seq(
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
    "-Ymacro-annotations"                            // Enable support for macro annotations, formerly in macro paradise.
  )

  def commonSettings: PE =
    _.enablePlugins(ScalaJSPlugin, ScalafixPlugin)
      .settings(
        scalaVersion                  := Ver.Scala213,
        crossScalaVersions            := Seq(Ver.Scala212, Ver.Scala213),
        scalacOptions                ++= scalacFlags,
        scalacOptions                ++= byScalaVersion {
                                           case (2, 12) => Nil
                                           case (2, 13) => scalac213Flags
                                         }.value,
        //scalacOptions               += "-Xlog-implicits",
        incOptions                    := incOptions.value.withLogRecompileOnMacro(false),
        updateOptions                 := updateOptions.value.withCachedResolution(true),
        releasePublishArtifactsAction := PgpKeys.publishSigned.value,
        releaseTagComment             := s"v${(version in ThisBuild).value}",
        releaseVcsSign                := true,
        libraryDependencies           += Dep.BetterMonadicFor)

  def preventPublication: PE =
    _.settings(
      publishTo := Some(Resolver.file("Unused transient repository", target.value / "fakepublish")),
      publishArtifact := false,
      publishLocal := {},
      publishLocalSigned := {},       // doesn't work
      publishSigned := {},            // doesn't work
      packagedArtifacts := Map.empty) // doesn't work - https://github.com/sbt/sbt-pgp/issues/42

  def publicationSettings: PE =
    _.settings(
      publishTo := sonatypePublishToBundle.value,
      pomExtra :=
        <scm>
          <connection>scm:git:github.com/japgolly/scalajs-react</connection>
          <developerConnection>scm:git:git@github.com:japgolly/scalajs-react.git</developerConnection>
          <url>github.com:japgolly/scalajs-react.git</url>
        </scm>
        <developers>
          <developer>
            <id>japgolly</id>
            <name>David Barri</name>
          </developer>
        </developers>)
    .configure(sourceMapsToGithub)

  def sourceMapsToGithub: PE =
    p => p.settings(
      scalacOptions ++= (if (isSnapshot.value) Seq.empty else Seq({
        val a = p.base.toURI.toString.replaceFirst("[^/]+/?$", "")
        val g = "https://raw.githubusercontent.com/japgolly/scalajs-react"
        s"-P:scalajs:mapSourceURI:$a->$g/v${version.value}/"
      }))
    )

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

  def utestSettings: PE =
    _.configure(InBrowserTesting.js)
      .settings(
        jsEnv                 := new JSDOMNodeJSEnv,
        scalacOptions in Test += "-language:reflectiveCalls",
        libraryDependencies   += Dep.MTest.value % Test,
        testFrameworks        += new TestFramework("utest.runner.Framework"))

  def extModuleName(shortName: String): PE =
    _.settings(name := s"ext-$shortName")

  def definesMacros: Project => Project =
    _.settings(
      scalacOptions += "-language:experimental.macros",
      libraryDependencies ++= Seq(
        Dep.ScalaReflect.value,
        Dep.ScalaCompiler.value % Provided))

  def hasNoTests: Project => Project =
    _.settings(
      fastOptJS     in Test := Attributed(artifactPath.in(fastOptJS).in(Test).value)(AttributeMap.empty),
      fullOptJS     in Test := Attributed(artifactPath.in(fullOptJS).in(Test).value)(AttributeMap.empty),
      sbt.Keys.test in Test := {},
      testOnly      in Test := {},
      testQuick     in Test := {})

  // ==============================================================================================
  lazy val root = Project("root", file("."))
    .settings(name := "scalajs-react")
    .aggregate(
      core, extra, test, /*testModule,*/
      cats, catsEffect, scalaz72,
      monocleCats, monocleScalaz,
      ghpagesMacros, ghpages)
    .configure(commonSettings, preventPublication, hasNoTests)

  // ==============================================================================================
  lazy val core = project
    .configure(commonSettings, publicationSettings, definesMacros, hasNoTests)
    .settings(
      name := "core",
      libraryDependencies ++= Seq(
        Dep.ScalaCollCompat.value,
        Dep.ScalaJsDom.value,
        Dep.Sourcecode.value))

  lazy val extra = project
    .configure(commonSettings, publicationSettings, definesMacros, hasNoTests)
    .dependsOn(core)
    .settings(name := "extra")

  lazy val test = project
    .configure(commonSettings, publicationSettings, utestSettings, addReactJsDependencies(Test), addMacroParadise)
    .dependsOn(core, extra)
    .dependsOn(scalaz72 % "test->compile")
    .dependsOn(monocleScalaz % "test->compile")
    .dependsOn(cats % "test->compile")
    .settings(
      name := "test",
      scalacOptions in Test --= Seq(
        "-deprecation",
        "-Xlint:adapted-args"
      ),
      libraryDependencies ++= Seq(
        Dep.MicrolibsTestUtil .value % Test,
        Dep.NyayaProp         .value % Test,
        Dep.NyayaGen          .value % Test,
        Dep.NyayaTest         .value % Test,
        Dep.MonocleScalazMacro.value % Test,
        Dep.ScalaJsJavaTime   .value % Test),
      jsDependencies ++= Seq(
        Dep.SizzleJs(Test).value,
        (ProvidedJS / "component-es6.js" dependsOn Dep.ReactDom.dev) % Test,
        (ProvidedJS / "component-fn.js"  dependsOn Dep.ReactDom.dev) % Test,
        (ProvidedJS / "forward-ref.js"   dependsOn Dep.ReactDom.dev) % Test,
        (ProvidedJS / "polyfill.js"      dependsOn Dep.ReactDom.dev) % Test))

  /*
  lazy val testModule = project.in(file("test-module"))
    .configure(commonSettings, useScalaJsBundler, preventPublication, utestSettings)
    .dependsOn(core, extra, test)
    .settings(
      name := "test-module",
      npmDependencies in Test ++= Seq(
        "react"                             -> Ver.ReactJs,
        "react-dom"                         -> Ver.ReactJs,
        "react-addons-perf"                 -> "15.5.0-rc.2",
        "react-addons-css-transition-group" -> "16.7.0"))
  */

  lazy val scalaz72 =
    Project("scalaz72", file("scalaz-7.2"))
      .configure(commonSettings, publicationSettings, extModuleName("scalaz72"), hasNoTests)
      .dependsOn(core, extra)
      .settings(
        libraryDependencies ++= Seq(
          Dep.ScalazEffect72.value,
          Dep.KindProjector))

  lazy val monocleScalaz = project
    .in(file("monocle-scalaz"))
    .configure(commonSettings, publicationSettings, extModuleName("monocle-scalaz"), hasNoTests)
    .dependsOn(core, extra, scalaz72)
    .settings(
      libraryDependencies += Dep.MonocleScalaz.value)

  lazy val cats = project
    .configure(commonSettings, publicationSettings, extModuleName("cats"), hasNoTests)
    .dependsOn(core, extra)
    .settings(
        libraryDependencies ++= Seq(
          Dep.Cats.value,
          Dep.KindProjector))

  lazy val monocleCats = project
    .in(file("monocle-cats"))
    .configure(commonSettings, publicationSettings, extModuleName("monocle-cats"), hasNoTests)
    .dependsOn(core, extra, cats)
    .settings(
      // Share the internal source code files with this module
      unmanagedSourceDirectories in Compile += (sourceDirectory in (monocleScalaz, Compile)).value / "scala" / "japgolly" / "scalajs" / "react" / "internal",
      libraryDependencies += Dep.MonocleCats.value)

  lazy val catsEffect = project
    .in(file("cats-effect"))
    .configure(commonSettings, publicationSettings, extModuleName("cats-effect"))
    .dependsOn(core, cats)
    .settings(
      libraryDependencies ++= Seq(
        Dep.Cats                .value,
        Dep.CatsEffect          .value,
        Dep.CatsEffectLaws      .value % Test,
        Dep.CatsTestkit         .value % Test,
        Dep.CatsTestkitScalaTest.value % Test,
        Dep.ScalaTest           .value % Test,
        Dep.DisciplineScalaTest .value % Test))

  // ==============================================================================================
  lazy val ghpagesMacros = Project("gh-pages-macros", file("gh-pages-macros"))
    .configure(commonSettings, preventPublication, hasNoTests, definesMacros)
    .settings(
      libraryDependencies ++= Seq(
        Dep.Sourcecode.value,
        Dep.ScalaCollCompat.value))

  lazy val ghpages = Project("gh-pages", file("gh-pages"))
    .dependsOn(core, extra, monocleScalaz, ghpagesMacros)
    .configure(commonSettings, addReactJsDependencies(Compile), addMacroParadise, preventPublication, hasNoTests)
    .settings(
      libraryDependencies += Dep.MonocleScalazMacro.value,
      scalaJSLinkerConfig ~= { _.withSourceMap(false) },
      scalaJSUseMainModuleInitializer := true,
      mainClass in Compile := Some("ghpages.GhPages"),
      artifactPath in (Compile, fullOptJS) := file("gh-pages/res/ghpages.js"))

}
