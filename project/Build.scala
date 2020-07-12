import sbt._
import sbt.Keys._
import com.typesafe.sbt.pgp.PgpKeys
import com.typesafe.sbt.pgp.PgpKeys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv
import org.scalajs.jsdependencies.sbtplugin.JSDependenciesPlugin
import org.scalajs.jsdependencies.sbtplugin.JSDependenciesPlugin.autoImport._
import sbtrelease.ReleasePlugin.autoImport._
import xerial.sbt.Sonatype.autoImport._
//import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
//import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

object ScalajsReact {

  object Ver {
    val BetterMonadicFor      = "0.3.1"
    val Cats                  = "2.1.1"
    val CatsEffect            = "2.1.4"
    val CatsTestkitScalaTest  = "1.0.1"
    val DisciplineScalaTest   = "1.0.1"
    val KindProjector         = "0.11.0"
    val MacroParadise         = "2.1.1"
    val Microlibs             = "2.3"
    val MonocleCats           = "2.0.5"
    val MonocleScalaz         = "1.6.3"
    val MTest                 = "0.7.4"
    val Nyaya                 = "0.9.2"
    val ReactJs               = "16.13.1"
    val Scala212              = "2.12.11"
    val Scala213              = "2.13.2"
    val ScalaCollCompat       = "2.1.6"
    val ScalaJsDom            = "1.0.0"
    val ScalaJsTime           = "1.0.0"
    val ScalaTest             = "3.1.2"
    val Scalaz72              = "7.2.30"
    val SizzleJs              = "2.3.0"
    val Sourcecode            = "0.2.1"
  }

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
    "-Ypatmat-exhaust-depth", "off") ++
    (if (scalaJSVersion.startsWith("0.")) Seq("-P:scalajs:sjsDefinedByDefault") else Nil)

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
    "-Xlint:nullary-override",                       // Non-nullary `def f()` overrides nullary `def f`.
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
    _.enablePlugins(ScalaJSPlugin)
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
    _.enablePlugins(JSDependenciesPlugin)
      .settings(
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
    compilerPlugin("org.typelevel" %% "kind-projector" % Ver.KindProjector cross CrossVersion.full)

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
      scalacOptions in Test --= Seq(
        "-deprecation",
        "-Xlint:adapted-args"
      ),
      libraryDependencies ++= Seq(
        "com.github.japgolly.microlibs" %%% "test-util"         % Ver.Microlibs     % Test,
        "com.github.japgolly.nyaya"     %%% "nyaya-prop"        % Ver.Nyaya         % Test,
        "com.github.japgolly.nyaya"     %%% "nyaya-gen"         % Ver.Nyaya         % Test,
        "com.github.japgolly.nyaya"     %%% "nyaya-test"        % Ver.Nyaya         % Test,
        "com.github.julien-truffaut"    %%% "monocle-macro"     % Ver.MonocleScalaz % Test,
        "org.scala-js"                  %%% "scalajs-java-time" % Ver.ScalaJsTime   % Test),
      jsDependencies ++= Seq(
        "org.webjars.bower" % "sizzle" % Ver.SizzleJs % Test / "sizzle.min.js" commonJSName "Sizzle",
        (ProvidedJS / "component-es6.js" dependsOn ReactDom.dev) % Test,
        (ProvidedJS / "component-fn.js"  dependsOn ReactDom.dev) % Test,
        (ProvidedJS / "forward-ref.js"   dependsOn ReactDom.dev) % Test,
        (ProvidedJS / "polyfill.js"      dependsOn ReactDom.dev) % Test),
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

  lazy val catsEffect = project
    .in(file("cats-effect"))
    .configure(commonSettings, publicationSettings, extModuleName("cats-effect"))
    .dependsOn(core, cats)
    .settings(
      libraryDependencies ++= Seq(
        "org.typelevel" %%% "cats-core"              % Ver.Cats,
        "org.typelevel" %%% "cats-effect"            % Ver.CatsEffect,
        "org.typelevel" %%% "cats-effect-laws"       % Ver.CatsEffect           % Test,
        "org.typelevel" %%% "cats-testkit"           % Ver.Cats                 % Test,
        "org.typelevel" %%% "cats-testkit-scalatest" % Ver.CatsTestkitScalaTest % Test,
        "org.scalatest" %%% "scalatest"              % Ver.ScalaTest            % Test,
        "org.typelevel" %%% "discipline-scalatest"   % Ver.DisciplineScalaTest  % Test
    ))

  // ==============================================================================================
  lazy val ghpagesMacros = Project("gh-pages-macros", file("gh-pages-macros"))
    .configure(commonSettings, preventPublication, hasNoTests, definesMacros)
    .settings(
      libraryDependencies ++= Seq(
        "com.lihaoyi" %%% "sourcecode" % Ver.Sourcecode,
        "org.scala-lang.modules" %%% "scala-collection-compat" % Ver.ScalaCollCompat
      ))

  lazy val ghpages = Project("gh-pages", file("gh-pages"))
    .dependsOn(core, extra, monocleScalaz, ghpagesMacros)
    .configure(commonSettings, addReactJsDependencies(Compile), preventPublication, hasNoTests)
    .settings(
      libraryDependencies += "com.github.julien-truffaut" %%% "monocle-macro" % Ver.MonocleScalaz,
      scalaJSLinkerConfig ~= { _.withSourceMap(false) },
      scalaJSUseMainModuleInitializer := true,
      mainClass in Compile := Some("ghpages.GhPages"),
      artifactPath in (Compile, fullOptJS) := file("gh-pages/res/ghpages.js"),
      libraryDependencies ++= paradisePlugin.value,
    )

}
