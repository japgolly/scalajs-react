import sbt._
import sbt.Keys._
import com.typesafe.sbt.pgp.PgpKeys
import com.typesafe.sbt.pgp.PgpKeys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.{CrossType => _, crossProject => _, _}
import sbtrelease.ReleasePlugin.autoImport._
//import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
//import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

object ScalajsReact {

  object Ver {
    val BetterMonadicFor = "0.3.1"
    val Cats             = "2.0.0-RC2"
    val KindProjector    = "0.10.3"
    val MacroParadise    = "2.1.1"
    val MonocleCats      = "2.0.0-RC1"
    val MonocleScalaz    = "1.6.0"
    val MTest            = "0.7.1"
    val Nyaya            = "0.9.0-RC1"
    val ReactJs          = "16.7.0"
    val Scala212         = "2.12.8"
    val Scala213         = "2.13.0"
    val ScalaCollCompat  = "2.1.2"
    val ScalaJsDom       = "0.9.7"
    val Scalaz72         = "7.2.28"
    val SizzleJs         = "2.3.0"
    val Sourcecode       = "0.1.7"
  }

  type PE = Project => Project

  def byScalaVersion[A](f: PartialFunction[(Long, Long), Seq[A]]): Def.Initialize[Seq[A]] =
    Def.setting(CrossVersion.partialVersion(scalaVersion.value).flatMap(f.lift).getOrElse(Nil))

  def scalacFlags = Seq(
    "-deprecation",
    "-unchecked",
    "-feature",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-opt:l:inline",
    "-opt-inline-from:japgolly.scalajs.react.**",
    "-P:scalajs:sjsDefinedByDefault")

  def commonSettings: PE =
    _.enablePlugins(ScalaJSPlugin)
      .settings(
        scalaVersion                  := Ver.Scala213,
        crossScalaVersions            := Seq(Ver.Scala212, Ver.Scala213),
        scalacOptions                ++= scalacFlags,
        scalacOptions                ++= byScalaVersion {
                                           case (2, 12) => Nil
                                           case (2, 13) => Seq("-Ymacro-annotations")
                                         }.value,
        //scalacOptions               += "-Xlog-implicits",
        incOptions                    := incOptions.value.withLogRecompileOnMacro(false),
        updateOptions                 := updateOptions.value.withCachedResolution(true),
        triggeredMessage              := Watched.clearWhenTriggered,
        releasePublishArtifactsAction := PgpKeys.publishSigned.value,
        releaseTagComment             := s"v${(version in ThisBuild).value}",
        releaseVcsSign                := true,
        addCompilerPlugin("com.olegpy" %% "better-monadic-for" % Ver.BetterMonadicFor))

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
        jsEnv                 := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv,
        scalacOptions in Test += "-language:reflectiveCalls",
        libraryDependencies   += "com.lihaoyi" %%% "utest" % Ver.MTest % "test",
        testFrameworks        += new TestFramework("utest.runner.Framework"))

  case class ReactArtifact(filename: String) {
    val dev = s"umd/$filename.development.js"
    val prod = s"umd/$filename.production.min.js"
  }
  val React             = ReactArtifact("react")
  val ReactDom          = ReactArtifact("react-dom")
  val ReactDomServer    = ReactArtifact("react-dom-server.browser")
  val ReactDomTestUtils = ReactArtifact("react-dom-test-utils")

  def addReactJsDependencies(scope: Configuration): PE = {
    _.settings(
      dependencyOverrides += "org.webjars.npm" % "js-tokens" % "3.0.2", // https://github.com/webjars/webjars/issues/1789
      dependencyOverrides += "org.webjars.npm" % "scheduler" % "0.12.0-alpha.3",
      jsDependencies ++= Seq(

        "org.webjars.npm" % "react" % Ver.ReactJs % scope
          /        "umd/react.development.js"
          minified "umd/react.production.min.js"
          commonJSName "React",

        "org.webjars.npm" % "react-dom" % Ver.ReactJs % scope
          /         "umd/react-dom.development.js"
          minified  "umd/react-dom.production.min.js"
          dependsOn "umd/react.development.js"
          commonJSName "ReactDOM",

        "org.webjars.npm" % "react-dom" % Ver.ReactJs % scope
          /         "umd/react-dom-test-utils.development.js"
          minified  "umd/react-dom-test-utils.production.min.js"
          dependsOn "umd/react-dom.development.js"
          commonJSName "ReactTestUtils",

        "org.webjars.npm" % "react-dom" % Ver.ReactJs % scope
          /         "umd/react-dom-server.browser.development.js"
          minified  "umd/react-dom-server.browser.production.min.js"
          dependsOn "umd/react-dom.development.js"
          commonJSName "ReactDOMServer"),

      skip in packageJSDependencies := false)

  }

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

  def paradisePlugin = Def.setting{
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v <= 12 =>
        Seq(compilerPlugin("org.scalamacros" % "paradise" % Ver.MacroParadise cross CrossVersion.patch))
      case _ =>
        // if scala 2.13.0-M4 or later, macro annotations merged into scala-reflect
        // https://github.com/scala/scala/pull/6606
        Nil
    }
  }

  def kindProjector =
    compilerPlugin("org.typelevel" %% "kind-projector" % Ver.KindProjector cross CrossVersion.binary)

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
      cats, scalaz72,
      monocle, monocleCats, monocleScalaz,
      ghpagesMacros, ghpages)
    .configure(commonSettings, preventPublication, hasNoTests)

  // ==============================================================================================
  lazy val core = project
    .configure(commonSettings, publicationSettings, definesMacros, hasNoTests)
    .settings(
      name := "core",
      libraryDependencies ++= Seq(
        "org.scala-lang.modules" %%% "scala-collection-compat" % Ver.ScalaCollCompat,
        "org.scala-js" %%% "scalajs-dom" % Ver.ScalaJsDom,
        "com.lihaoyi" %%% "sourcecode" % Ver.Sourcecode))

  lazy val extra = project
    .configure(commonSettings, publicationSettings, definesMacros, hasNoTests)
    .dependsOn(core)
    .settings(name := "extra")

  lazy val test = project
    .configure(commonSettings, publicationSettings, utestSettings, addReactJsDependencies(Test))
    .dependsOn(core, extra)
    .dependsOn(scalaz72 % "test->compile")
    .dependsOn(monocleScalaz % "test->compile")
    .dependsOn(cats % "test->compile")
    .settings(
      name := "test",
      scalacOptions in Test -= "-deprecation",
      libraryDependencies ++= Seq(
        "com.github.japgolly.nyaya" %%% "nyaya-prop" % Ver.Nyaya % Test,
        "com.github.japgolly.nyaya" %%% "nyaya-gen"  % Ver.Nyaya % Test,
        "com.github.japgolly.nyaya" %%% "nyaya-test" % Ver.Nyaya % Test,
        "com.github.julien-truffaut" %%% "monocle-macro" % Ver.MonocleScalaz % Test),
      jsDependencies ++= Seq(
        "org.webjars.bower" % "sizzle" % Ver.SizzleJs % Test / "sizzle.min.js" commonJSName "Sizzle",
        (ProvidedJS / "component-es6.js" dependsOn ReactDom.dev) % Test,
        (ProvidedJS / "component-fn.js" dependsOn ReactDom.dev) % Test,
        (ProvidedJS / "forward-ref.js"  dependsOn ReactDom.dev) % Test),
      libraryDependencies ++= paradisePlugin.value,
    )

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

  def scalazModule(name: String, version: String) = {
    val shortName = name.replaceAll("[^a-zA-Z0-9]+", "")
    Project(shortName, file(name))
      .configure(commonSettings, publicationSettings, extModuleName(shortName), hasNoTests)
      .dependsOn(core, extra)
      .settings(
        libraryDependencies += "org.scalaz" %%% "scalaz-effect" % version,
        addCompilerPlugin(kindProjector))
  }

  lazy val scalaz72 = scalazModule("scalaz-7.2", Ver.Scalaz72)

  lazy val monocle = project
    .in(file("monocle"))
    .configure(commonSettings, publicationSettings, extModuleName("monocle"), hasNoTests)
    .dependsOn(core, extra, scalaz72)
    .settings(
      libraryDependencies += "com.github.julien-truffaut" %%% "monocle-core" % Ver.MonocleScalaz)

  lazy val monocleScalaz = project
    .in(file("monocle-scalaz"))
    .configure(commonSettings, publicationSettings, extModuleName("monocle-scalaz"), hasNoTests)
    .dependsOn(core, extra, scalaz72)
    .settings(
      libraryDependencies += "com.github.julien-truffaut" %%% "monocle-core" % Ver.MonocleScalaz)

  lazy val cats = project
    .configure(commonSettings, publicationSettings, extModuleName("cats"), hasNoTests)
    .dependsOn(core, extra)
    .settings(
      libraryDependencies += "org.typelevel" %%% "cats-core" % Ver.Cats,
      addCompilerPlugin(kindProjector))

  lazy val monocleCats = project
    .in(file("monocle-cats"))
    .configure(commonSettings, publicationSettings, extModuleName("monocle-cats"), hasNoTests)
    .dependsOn(core, extra, cats)
    .settings(
      // Share the internal source code files with this module
      unmanagedSourceDirectories in Compile += (sourceDirectory in (monocleScalaz, Compile)).value / "scala" / "japgolly" / "scalajs" / "react" / "internal",
      libraryDependencies += "com.github.julien-truffaut" %%% "monocle-core" % Ver.MonocleCats)

  // ==============================================================================================
  lazy val ghpagesMacros = Project("gh-pages-macros", file("gh-pages-macros"))
    .configure(commonSettings, preventPublication, hasNoTests, definesMacros)
    .settings(
      libraryDependencies += "org.scala-lang.modules" %%% "scala-collection-compat" % Ver.ScalaCollCompat,
      crossScalaVersions := Seq(Ver.Scala212))

  lazy val ghpages = Project("gh-pages", file("gh-pages"))
    .dependsOn(core, extra, monocleScalaz, ghpagesMacros)
    .configure(commonSettings, addReactJsDependencies(Compile), preventPublication, hasNoTests)
    .settings(
      crossScalaVersions := Seq(Ver.Scala212),
      libraryDependencies += "com.github.julien-truffaut" %%% "monocle-macro" % Ver.MonocleScalaz,
      emitSourceMaps := false,
      scalaJSUseMainModuleInitializer := true,
      mainClass in Compile := Some("ghpages.GhPages"),
      artifactPath in (Compile, fullOptJS) := file("gh-pages/res/ghpages.js"),
      libraryDependencies ++= paradisePlugin.value,
    )

}
