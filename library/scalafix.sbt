ThisBuild / scalacOptions ++= {
  if (scalaVersion.value startsWith "2")
    "-Yrangepos" :: Nil
  else
    Nil
}

ThisBuild / semanticdbEnabled := true

// NOTE: Upgrade downstream-tests/scalafix.sbt too!
ThisBuild / semanticdbVersion := "4.12.0"
