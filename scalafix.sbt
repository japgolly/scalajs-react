ThisBuild / scalacOptions ++= {
  if (scalaVersion.value startsWith "2")
    "-Yrangepos" :: Nil
  else
    Nil
}

ThisBuild / semanticdbEnabled := true

ThisBuild / semanticdbVersion := "4.4.23"

ThisBuild / scalafixScalaBinaryVersion := "2.13"

ThisBuild / scalafixDependencies ++= Seq(
  "com.github.liancheng" %% "organize-imports" % "0.5.0"
)
