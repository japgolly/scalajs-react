import sbt._
import Keys._

import com.typesafe.sbt.pgp.PgpKeys._

import org.scalajs.sbtplugin.ScalaJSPlugin
import ScalaJSPlugin._
import ScalaJSPlugin.autoImport._

object ScalajsReact extends Build {

  val Scala211 = "2.11.7"

  type PE = Project => Project

  val clearScreenTask = TaskKey[Unit]("clear", "Clears the screen.")

  def commonSettings: PE =
    _.enablePlugins(ScalaJSPlugin)
      .settings(
        organization       := "com.github.japgolly.scalajs-react",
        version            := "0.10.0-SNAPSHOT",
        homepage           := Some(url("https://github.com/japgolly/scalajs-react")),
        licenses           += ("Apache-2.0", url("http://opensource.org/licenses/Apache-2.0")),
        scalaVersion       := Scala211,
        // crossScalaVersions := Seq("2.10.4", Scala211), https://github.com/japgolly/scalajs-react/issues/39
        scalacOptions     ++= Seq("-deprecation", "-unchecked", "-feature",
                                "-language:postfixOps", "-language:implicitConversions",
                                "-language:higherKinds", "-language:existentials"),
        //scalacOptions    += "-Xlog-implicits",
        updateOptions      := updateOptions.value.withCachedResolution(true),
        clearScreenTask    := { println("\033[2J\033[;H") })

  def preventPublication: PE =
    _.settings(
      publishTo := Some(Resolver.file("Unused transient repository", target.value / "fakepublish")),
      publishArtifact := false,
      publishLocalSigned := (),       // doesn't work
      publishSigned := (),            // doesn't work
      packagedArtifacts := Map.empty) // doesn't work - https://github.com/sbt/sbt-pgp/issues/42

  def publicationSettings: PE =
    _.settings(
      publishTo := {
        val nexus = "https://oss.sonatype.org/"
        if (isSnapshot.value)
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases"  at nexus + "service/local/staging/deploy/maven2")
      },
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

  def utestSettings: PE =
    _.configure(useReactJs("test"))
      .settings(
        libraryDependencies  += "com.lihaoyi" %%% "utest" % "0.3.1",
        jsDependencies += (ProvidedJS / "sampleReactComponent.js" dependsOn "react-with-addons.js") % Test, // dependency for JS Component Type Test.
        testFrameworks       += new TestFramework("utest.runner.Framework"),
        scalaJSStage in Test := FastOptStage,
        requiresDOM          := true,
        jsEnv in Test        := new PhantomJS2Env(scalaJSPhantomJSClassLoader.value))

  def useReactJs(scope: String = "compile"): PE =
    _.settings(
      jsDependencies += "org.webjars" % "react" % "0.13.3" % scope / "react-with-addons.js" commonJSName "React",
      skip in packageJSDependencies := false)

  def addCommandAliases(m: (String, String)*) = {
    val s = m.map(p => addCommandAlias(p._1, p._2)).reduce(_ ++ _)
    (_: Project).settings(s: _*)
  }

  def extModuleName(shortName: String): PE =
    _.settings(name := s"ext-$shortName")

  def definesMacros: Project => Project =
    _.settings(
      scalacOptions += "-language:experimental.macros",
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % Scala211,
        "org.scala-lang" % "scala-compiler" % Scala211 % "provided"))

  def macroParadisePlugin =
    compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)

  def hasNoTests: Project => Project =
    _.settings(
      sbt.Keys.test in Test := (),
      testOnly      in Test := (),
      testQuick     in Test := ())

  // ==============================================================================================
  lazy val root = Project("root", file("."))
    .aggregate(core, test, scalaz71, monocle, extra, ghpagesMacros, ghpages)
    .configure(commonSettings, preventPublication, hasNoTests, addCommandAliases(
      "C"  -> "root/clean",
      "t"  -> ";clear;  test:compile ; test/test",
      "tt" -> ";clear; +test:compile ;+test/test",
      "T"  -> "; clean ;t",
      "TT" -> ";+clean ;tt"))

  // ==============================================================================================
  lazy val core = project
    .configure(commonSettings, publicationSettings, definesMacros, hasNoTests)
    .settings(
      name := "core",
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "0.8.1"))

  lazy val extra = project
    .configure(commonSettings, publicationSettings, definesMacros, hasNoTests)
    .dependsOn(core)
    .settings(name := "extra")

  lazy val test = project
    .configure(commonSettings, publicationSettings, utestSettings)
    .dependsOn(core, extra, scalaz71, monocle)
    .settings(
      name := "test",
      libraryDependencies += monocleLib("macro") % "test",
      addCompilerPlugin(macroParadisePlugin),
      scalacOptions in Test += "-language:reflectiveCalls")

  // ==============================================================================================
  def scalazModule(name: String, version: String) = {
    val shortName = name.replaceAll("[^a-zA-Z0-9]+", "")
    Project(shortName, file(name))
      .configure(commonSettings, publicationSettings, extModuleName(shortName), hasNoTests)
      .dependsOn(core, extra)
      .settings(
        libraryDependencies += "com.github.japgolly.fork.scalaz" %%% "scalaz-effect" % version)
  }

  lazy val scalaz71 = scalazModule("scalaz-7.1", "7.1.3")

  // ==============================================================================================
  lazy val monocle = project
    .configure(commonSettings, publicationSettings, extModuleName("monocle"), hasNoTests)
    .dependsOn(core, extra, scalaz71)
    .settings(libraryDependencies += monocleLib("core"))

  def monocleLib(name: String) =
    "com.github.japgolly.fork.monocle" %%%! s"monocle-$name" % "1.1.1"

  // ==============================================================================================
  lazy val ghpagesMacros = Project("gh-pages-macros", file("gh-pages-macros"))
    .configure(commonSettings, preventPublication, hasNoTests, definesMacros)

  lazy val ghpages = Project("gh-pages", file("gh-pages"))
    .dependsOn(core, extra, scalaz71, monocle, ghpagesMacros)
    .configure(commonSettings, useReactJs(), preventPublication, hasNoTests)
    .settings(
      libraryDependencies += monocleLib("macro"),
      addCompilerPlugin(macroParadisePlugin),
      emitSourceMaps := false,
      artifactPath in (Compile, fullOptJS) := file("gh-pages/res/ghpages.js"))
}
