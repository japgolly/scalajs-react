import sbt._
import Keys._

import scala.scalajs.sbtplugin.env.nodejs.NodeJSEnv
import scala.scalajs.sbtplugin.env.phantomjs.PhantomJSEnv
import scala.scalajs.sbtplugin.ScalaJSPlugin._
import scala.scalajs.sbtplugin.ScalaJSPlugin.ScalaJSKeys._

import bintray.{Keys => BK}

object ScalajsReact extends Build {

  val Scala211 = "2.11.1"

  val root = Project("scalajs-react", file("."))
    .settings(scalaJSSettings: _*)
    .settings(bintray.Plugin.bintrayPublishSettings: _*)
    .settings(utest.jsrunner.Plugin.utestJsSettings: _*)
    .settings(
      name := "scalajs-react"
      ,homepage := Some(url("https://github.com/japgolly/scalajs-react"))
      ,licenses += ("Apache-2.0", url("http://opensource.org/licenses/Apache-2.0"))
      ,organization := "japgolly.scalajs.react"
      ,BK.repository in BK.bintray := "scala"
      ,publishMavenStyle := true
      ,version := "0.2.0"
      ,scalaVersion := Scala211
      ,crossScalaVersions := Seq("2.10.4", Scala211)
      ,scalacOptions ++= Seq("-deprecation", "-unchecked", "-language:_")
      ,libraryDependencies ++= Seq(
        "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.6"
        ,"com.scalatags" %%% "scalatags" % "0.3.5"
        ,"com.lihaoyi" %%% "utest" % "0.1.7" % "test"
      )
      ,jsDependencies ++= Seq(
        "org.webjars" % "react" % "0.10.0" / "react-with-addons.min.js"
      )
      ,skip in packageJSDependencies := false
      ,jsEnv in Test := new NodeJSEnv
    )

}
