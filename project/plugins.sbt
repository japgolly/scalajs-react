val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).filter(_.nonEmpty).getOrElse("1.0.1")

libraryDependencies ++= {
  if (scalaJSVersion.startsWith("0."))
    Seq(
      "org.scala-js" %% "scalajs-env-selenium" % "0.3.0"
    )
  else
    Seq(
      "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0",
      "org.scala-js" %% "scalajs-env-selenium"     % "1.0.0"
    )
}

addSbtPlugin("org.scala-js"      % "sbt-scalajs"        % scalaJSVersion)
addSbtPlugin("com.jsuereth"      % "sbt-pgp"            % "1.1.2")
addSbtPlugin("com.github.gseitz" % "sbt-release"        % "1.0.13")

{
  if (scalaJSVersion.startsWith("0."))
    Nil
  else
    Seq(
      addSbtPlugin("org.scala-js" % "sbt-jsdependencies" % "1.0.0")
    )
}

// addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.10.0")
