import sbt._
import Keys._
import com.jsuereth.sbtpgp.PgpKeys._
import dotty.tools.sbtplugin.DottyPlugin.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin._
import xerial.sbt.Sonatype.autoImport._

object Lib {
  type PE = Project => Project

  val ghProject = "scalajs-react"

  def byScalaVersion[A](f: PartialFunction[(Long, Long), Seq[A]]): Def.Initialize[Seq[A]] =
    Def.setting(CrossVersion.partialVersion(scalaVersion.value).flatMap(f.lift).getOrElse(Nil))

  def publicationSettings: PE =
    _.configure(sourceMapsToGithub(ghProject))
      .settings(
        publishTo := sonatypePublishToBundle.value,
        pomExtra :=
          <scm>
            <connection>scm:git:github.com/japgolly/{ghProject}</connection>
            <developerConnection>scm:git:git@github.com:japgolly/{ghProject}.git</developerConnection>
            <url>github.com:japgolly/{ghProject}.git</url>
          </scm>
          <developers>
            <developer>
              <id>japgolly</id>
              <name>David Barri</name>
            </developer>
          </developers>)

  def sourceMapsToGithub(ghProject: String): PE =
    p => p.settings(
      scalacOptions ++= {
        val _isDotty    = isDotty.value
        val _isSnapshot = isSnapshot.value
        val ver         = version.value
        if (_isSnapshot)
          Nil
        else {
          val a = p.base.toURI.toString.replaceFirst("[^/]+/?$", "")
          val g = s"https://raw.githubusercontent.com/japgolly/$ghProject"
          val flag = if (_isDotty) "-scalajs-mapSourceURI" else "-P:scalajs:mapSourceURI"
          s"$flag:$a->$g/v$ver/" :: Nil
        }
      }
    )

  def preventPublication: PE =
    _.settings(
      skip in publish    := true,
      publish            := (()),
      publishLocal       := (()),
      publishSigned      := (()),
      publishLocalSigned := (()),
      publishArtifact    := false,
      publishTo          := Some(Resolver.file("Unused transient repository", target.value / "fakepublish")),
      packagedArtifacts  := Map.empty)
    // .disablePlugins(plugins.IvyPlugin)

}
