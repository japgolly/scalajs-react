// This is here just to manually test using the ScalaJsReactEffectAgnosticism
// scalafix rule downstream.

ThisBuild / scalacOptions ++= {
  if (scalaVersion.value startsWith "2")
    "-Yrangepos" :: Nil
  else
    Nil
}

ThisBuild / semanticdbEnabled := true

ThisBuild / semanticdbVersion := "4.9.8"

ThisBuild / scalafixScalaBinaryVersion := "2.13"

ThisBuild / scalafixDependencies += {
  val ver = version.value.stripSuffix("-SNAPSHOT") + "-SNAPSHOT"
  "io.github.japgolly.scalajs-react" %% "scalafix" % ver
}
