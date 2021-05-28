ThisBuild / scalacOptions += "-Yrangepos"

ThisBuild / semanticdbEnabled := true

ThisBuild / semanticdbVersion := "4.4.19"

ThisBuild / scalafixScalaBinaryVersion := "2.13"

ThisBuild / scalafixDependencies ++= Seq(
  "com.github.liancheng" %% "organize-imports" % "0.5.0"
)
