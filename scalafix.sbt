ThisBuild / scalacOptions += "-Yrangepos"

ThisBuild / semanticdbEnabled := true

ThisBuild / semanticdbVersion := "4.3.20"

ThisBuild / scalafixScalaBinaryVersion := "2.13"

ThisBuild / scalafixDependencies ++= Seq(
  "com.github.liancheng" %% "organize-imports" % "0.4.1"
)

//ThisBuild / scalacOptions += "-P:semanticdb:synthetics:on",
//ThisBuild / scalafixDependencies += "org.scala-lang" %% "scala-rewrites" % "0.1.0-SNAPSHOT"
