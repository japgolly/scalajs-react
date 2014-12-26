import sbt._
import Keys._

import com.typesafe.sbt.pgp.PgpKeys._

import scala.scalajs.sbtplugin.env.nodejs.NodeJSEnv
import scala.scalajs.sbtplugin.env.phantomjs.PhantomJSEnv
import scala.scalajs.sbtplugin.ScalaJSPlugin._
import scala.scalajs.sbtplugin.ScalaJSPlugin.ScalaJSKeys._

object ScalajsReact extends Build {

  val Scala211 = "2.11.4"

  type PE = Project => Project

  def commonSettings: PE =
    _.settings(scalaJSSettings: _*)
      .settings(
        organization       := "com.github.japgolly.scalajs-react",
        version            := "0.7.0-SNAPSHOT",
        homepage           := Some(url("https://github.com/japgolly/scalajs-react")),
        licenses           += ("Apache-2.0", url("http://opensource.org/licenses/Apache-2.0")),
        scalaVersion       := Scala211,
        // crossScalaVersions := Seq("2.10.4", Scala211), https://github.com/japgolly/scalajs-react/issues/39
        scalacOptions     ++= Seq("-deprecation", "-unchecked", "-feature",
                                "-language:postfixOps", "-language:implicitConversions",
                                "-language:higherKinds", "-language:existentials"),
        updateOptions      := updateOptions.value.withCachedResolution(true))

  def preventPublication: PE =
    _.settings(
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
    _.settings(utest.jsrunner.Plugin.utestJsSettings: _*)
      .configure(useReact("test"))
      .settings(
        libraryDependencies += "com.lihaoyi" %%% "utest" % "0.2.3" % "test",
        requiresDOM := true,
        jsEnv in Test := new PhantomJSEnv)

  def useReact(scope: String = "compile"): PE =
    _.settings(
      jsDependencies += "org.webjars" % "react" % "0.12.1" % scope / "react-with-addons.js" commonJSName "React",
      skip in packageJSDependencies := false)

  def addCommandAliases(m: (String, String)*) = {
    val s = m.map(p => addCommandAlias(p._1, p._2)).reduce(_ ++ _)
    (_: Project).settings(s: _*)
  }

  def extModuleName(shortName: String): PE =
    _.settings(name := s"ext-$shortName")

  // ==============================================================================================
  lazy val root = Project("root", file("."))
    .aggregate(core, test, scalaz70, scalaz71, extra, ghpages)
    .configure(commonSettings, preventPublication, addCommandAliases(
      "t"  -> "; test:compile ; test/fastOptStage::test",
      "tt" -> ";+test:compile ;+test/fastOptStage::test",
      "T"  -> "; clean ;t",
      "TT" -> ";+clean ;tt"))

  // ==============================================================================================
  lazy val core = project
    .configure(commonSettings, publicationSettings)
    .settings(
      name := "core",
      libraryDependencies ++= Seq(
        "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.6"))

  lazy val test = project
    .configure(commonSettings, publicationSettings, utestSettings)
    .dependsOn(core, scalaz71, extra, monocle)
    .settings(
      name := "test",
      scalacOptions += "-language:reflectiveCalls")

  // ==============================================================================================
  def scalazModule(name: String, version: String) = {
    val shortName = name.replaceAll("[^a-zA-Z0-9]+", "")
    Project(shortName, file(name))
      .configure(commonSettings, publicationSettings, extModuleName(shortName))
      .dependsOn(core)
      .settings(
        libraryDependencies += "com.github.japgolly.fork.scalaz" %%% "scalaz-effect" % version)
  }

  lazy val scalaz70 = scalazModule("scalaz-7.0", "7.0.6")
  lazy val scalaz71 = scalazModule("scalaz-7.1", "7.1.0-4")

  // ==============================================================================================
  lazy val monocle = project
    .configure(commonSettings, publicationSettings, extModuleName("monocle"))
    .dependsOn(core, scalaz71)
    .settings(
      libraryDependencies += "com.github.japgolly.fork.monocle" %%% "monocle-core" % "1.0.1")

  // ==============================================================================================
  lazy val extra = project
    .configure(commonSettings, publicationSettings)
    .dependsOn(core, scalaz71)
    .settings(
      name := "extra")

  // ==============================================================================================
  lazy val ghpages = Project("gh-pages", file("gh-pages"))
    .dependsOn(core, scalaz71, extra, monocle)
    .configure(commonSettings, useReact(), preventPublication)
    .settings(
      emitSourceMaps := false,
      artifactPath in (Compile, fullOptJS) := file("gh-pages/res/ghpages.js"))
}