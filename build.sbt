scalaJSSettings

name := "Scala.js experiment"

// scalaVersion := "2.10.4"

scalaVersion := "2.11.1"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.1.0-M7"

libraryDependencies += "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.6"

// Loads DOM into Rhino which enables sbt run
ScalaJSKeys.requiresDOM := true

