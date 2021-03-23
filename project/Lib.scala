import sbt._
import Keys._
import com.jsuereth.sbtpgp.PgpKeys._
import dotty.tools.sbtplugin.DottyPlugin.autoImport._
import org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import xerial.sbt.Sonatype.autoImport._

object Lib {
  import Dependencies.Dep

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
    _.settings(publish / skip := true)

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
      scalacOptions       ++= (if (isDotty.value) Nil else Seq("-language:experimental.macros")),
      libraryDependencies  += Dep.MicrolibsMacroUtils.value,
      libraryDependencies ++= (if (isDotty.value) Nil else Seq(Dep.ScalaReflect.value, Dep.ScalaCompiler.value % Provided)),
    )

  def hasNoTests: Project => Project =
    _.settings(
      fastOptJS     in Test := Attributed(artifactPath.in(fastOptJS).in(Test).value)(AttributeMap.empty),
      fullOptJS     in Test := Attributed(artifactPath.in(fullOptJS).in(Test).value)(AttributeMap.empty),
      sbt.Keys.test in Test := { (Test / compile).value; () },
      testOnly      in Test := { (Test / compile).value; () },
      testQuick     in Test := { (Test / compile).value; () })

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
