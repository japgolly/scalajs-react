import sbt._
import Keys._

import scala.scalajs.sbtplugin.env.nodejs.NodeJSEnv
import scala.scalajs.sbtplugin.env.phantomjs.PhantomJSEnv
import scala.scalajs.sbtplugin.ScalaJSPlugin._
import scala.scalajs.sbtplugin.ScalaJSPlugin.ScalaJSKeys._

object ScalajsReact extends Build {

  val Scala211 = "2.11.1"

  val root = Project("scalajs-react", file("."))
    .settings(scalaJSSettings: _*)
    .settings(utest.jsrunner.Plugin.utestJsSettings: _*)
    .settings(
      organization := "com.github.japgolly.scalajs-react"
      ,name        := "scalajs-react"
      ,version     := "0.2.1-SNAPSHOT"

      ,homepage := Some(url("https://github.com/japgolly/scalajs-react"))

      ,scalaVersion       := Scala211
      ,crossScalaVersions := Seq("2.10.4", Scala211)

      ,scalacOptions ++= Seq("-deprecation", "-unchecked", "-language:_")

      ,libraryDependencies ++= Seq(
        "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.6"
        ,"com.scalatags" %%% "scalatags" % "0.3.5"
        ,"com.lihaoyi" %%% "utest" % "0.1.7" % "test"
      )

      ,jsDependencies ++= Seq(
        "org.webjars" % "react" % "0.11.0" / "react-with-addons.min.js"
      )
      ,skip in packageJSDependencies := false

      ,jsEnv in Test := new NodeJSEnv

      ,publishTo := {
        val nexus = "https://oss.sonatype.org/"
        if (version.value.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases"  at nexus + "service/local/staging/deploy/maven2")
      }
      ,licenses += ("Apache-2.0", url("http://opensource.org/licenses/Apache-2.0"))
      ,pomExtra :=
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
        </developers>

    )

}
