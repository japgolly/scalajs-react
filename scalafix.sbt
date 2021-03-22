ThisBuild / scalacOptions ++= {
  if (!isDotty.value)
    "-Yrangepos" :: Nil
  else
    Nil
}

ThisBuild / semanticdbEnabled := true

ThisBuild / semanticdbVersion := "4.4.10"

ThisBuild / scalafixScalaBinaryVersion := "2.13"

ThisBuild / scalafixDependencies ++= Seq(
  "com.github.liancheng" %% "organize-imports" % "0.5.0"
)

//ThisBuild / scalacOptions += "-P:semanticdb:synthetics:on",
//ThisBuild / scalafixDependencies += "org.scala-lang" %% "scala-rewrites" % "0.1.0-SNAPSHOT"
