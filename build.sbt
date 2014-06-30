scalaJSSettings

name := "Scala.js experiment"

// scalaVersion := "2.10.4"

scalaVersion := "2.11.1"

scalacOptions ++= Seq( "-deprecation", "-unchecked", "-language:_" )

resolvers += bintray.Opts.resolver.repo("japgolly", "forks")

libraryDependencies += "japgolly.scalaz" %%% "scalaz-core" % "7.1.0-RC1"

libraryDependencies += "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.6"

libraryDependencies += "com.scalatags" %%% "scalatags" % "0.3.5"

// Loads DOM into Rhino which enables sbt run
ScalaJSKeys.requiresDOM := true

//==============================================================================

workbenchSettings

bootSnippet := "golly.Golly().main();"

refreshBrowsers <<= refreshBrowsers.triggeredBy(ScalaJSKeys.fastOptJS in Compile)

