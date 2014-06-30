name := "scalala-react-js"

version := "0.1.0"

// scalaVersion := "2.10.4"
scalaVersion := "2.11.1"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-language:_")

scalaJSSettings

libraryDependencies ++= Seq(
  "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.6"
  ,"com.scalatags" %%% "scalatags" % "0.3.5"
)
