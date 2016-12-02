import sbt._
import Keys._
import com.typesafe.sbt.pgp.PgpKeys._
import org.scalajs.sbtplugin.ScalaJSPlugin
import ScalaJSPlugin.autoImport._
import scalajsbundler.ScalaJSBundlerPlugin
import ScalaJSBundlerPlugin.autoImport._

object ScalajsReact {

  object Ver {
    val Scala211      = "2.11.8"
    val Scala212      = "2.12.0"
    val ScalaJsDom    = "0.9.1"
    val ReactJs       = "15.3.2"
    val Monocle       = "1.3.2"
    val Scalaz72      = "7.2.7"
    val MTest         = "0.4.4"
    val MacroParadise = "2.1.0"
    val SizzleJs      = "2.3.0"
    val Nyaya         = "0.8.1"
  }

  type PE = Project => Project

  val clearScreenTask = TaskKey[Unit]("clear", "Clears the screen.")

  def commonSettings: PE =
    _.enablePlugins(ScalaJSBundlerPlugin)
      .settings(
        organization       := "com.github.japgolly.scalajs-react",
        homepage           := Some(url("https://github.com/japgolly/scalajs-react")),
        licenses           += ("Apache-2.0", url("http://opensource.org/licenses/Apache-2.0")),
        scalaVersion       := Ver.Scala211,
        crossScalaVersions := Seq(Ver.Scala211, Ver.Scala212),
        scalacOptions     ++= Seq("-deprecation", "-unchecked", "-feature",
                                "-language:postfixOps", "-language:implicitConversions",
                                "-language:higherKinds", "-language:existentials"),
        //scalacOptions    += "-Xlog-implicits",
        updateOptions      := updateOptions.value.withCachedResolution(true),
        incOptions         := incOptions.value.withLogRecompileOnMacro(false),
        triggeredMessage   := Watched.clearWhenTriggered,
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
    _.settings(
      libraryDependencies += "com.lihaoyi" %%% "utest" % Ver.MTest % Test,
      testFrameworks      += new TestFramework("utest.runner.Framework"),
      requiresDOM         := true
    )

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
        "org.scala-lang" % "scala-reflect"  % scalaVersion.value,
        "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"))

  def macroParadisePlugin =
    compilerPlugin("org.scalamacros" % "paradise" % Ver.MacroParadise cross CrossVersion.full)

  def hasNoTests: Project => Project =
    _.settings(
      sbt.Keys.test in Test := (),
      testOnly      in Test := (),
      testQuick     in Test := ())

  // ==============================================================================================
  lazy val root = Project("root", file("."))
    .aggregate(core, test, scalaz72, monocle, extra, ghpagesMacros, ghpages)
    .configure(commonSettings, preventPublication, hasNoTests, addCommandAliases(
      "/"   -> "project root",
      "L"   -> "root/publishLocal",
      "C"   -> "root/clean",
      "T"   -> ";root/clean;root/test",
      "c"   -> "compile",
      "tc"  -> "test:compile",
      "t"   -> "test",
      "to"  -> "test/test-only",
      "cc"  -> ";clean;compile",
      "ctc" -> ";clean;test:compile",
      "ct"  -> ";clean;test"))

  // ==============================================================================================
  lazy val core = project
    .configure(commonSettings, publicationSettings, definesMacros, hasNoTests)
    .settings(
      name := "core",
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % Ver.ScalaJsDom),
      npmDependencies in Compile ++= Seq(
        "react" -> Ver.ReactJs,
        "react-dom" -> Ver.ReactJs,
        "react-addons-perf" -> Ver.ReactJs,
        "react-addons-css-transition-group" -> Ver.ReactJs
      )
    )

  lazy val extra = project
    .configure(commonSettings, publicationSettings, definesMacros, hasNoTests)
    .dependsOn(core)
    .settings(name := "extra")

  lazy val test = project
    .configure(commonSettings, publicationSettings, utestSettings, InBrowserTesting.js)
    .dependsOn(core, extra, monocle)
    .settings(
      name := "test",
      libraryDependencies ++= Seq(
        "com.github.japgolly.nyaya" %%% "nyaya-prop" % Ver.Nyaya % "test",
        "com.github.japgolly.nyaya" %%% "nyaya-gen"  % Ver.Nyaya % "test",
        "com.github.japgolly.nyaya" %%% "nyaya-test" % Ver.Nyaya % "test",
        monocleLib("macro") % "test"),
      npmDependencies in Compile ++= Seq(
        "react-addons-test-utils" -> Ver.ReactJs
      ),
      npmDependencies in Test ++= Seq(
        "react-dom" -> Ver.ReactJs, // for JS component Type Test.
        "sizzle" -> Ver.SizzleJs
      ),
      addCompilerPlugin(macroParadisePlugin),
      scalacOptions in Test += "-language:reflectiveCalls")

  // ==============================================================================================
  def scalazModule(name: String, version: String) = {
    val shortName = name.replaceAll("[^a-zA-Z0-9]+", "")
    Project(shortName, file(name))
      .configure(commonSettings, publicationSettings, extModuleName(shortName), hasNoTests)
      .dependsOn(core, extra)
      .settings(
        libraryDependencies += "org.scalaz" %%% "scalaz-effect" % version)
  }

  lazy val scalaz72 = scalazModule("scalaz-7.2", Ver.Scalaz72)

  // ==============================================================================================
  lazy val monocle = project
    .configure(commonSettings, publicationSettings, extModuleName("monocle"), hasNoTests)
    .dependsOn(core, extra, scalaz72)
    .settings(libraryDependencies += monocleLib("core"))

  def monocleLib(name: String) =
    "com.github.julien-truffaut" %%%! s"monocle-$name" % Ver.Monocle

  // ==============================================================================================
  lazy val ghpagesMacros = Project("gh-pages-macros", file("gh-pages-macros"))
    .configure(commonSettings, preventPublication, hasNoTests, definesMacros)

  lazy val ghpages = Project("gh-pages", file("gh-pages"))
    .dependsOn(core, extra, monocle, ghpagesMacros)
    .configure(commonSettings, preventPublication, hasNoTests)
    .settings(
      libraryDependencies += monocleLib("macro"),
      addCompilerPlugin(macroParadisePlugin),
      emitSourceMaps := false,
      webpack in (Compile, fullOptJS) := {
        val files = (webpack in (Compile, fullOptJS)).value
        // We have only one entry point so the body will be executed exactly once
        files.foreach { f =>
          IO.copyFile(f, file("gh-pages/res/ghpages.js"))
        }
        files
      }
    )
}
