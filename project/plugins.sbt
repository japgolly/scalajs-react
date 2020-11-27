val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).filter(_.nonEmpty).getOrElse("1.2.0")

libraryDependencies ++= {
  if (scalaJSVersion.startsWith("0."))
    Seq(
      "org.scala-js" %% "scalajs-env-selenium" % "0.3.0"
    )
  else
    Seq(
      "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0",
      "org.scala-js" %% "scalajs-env-selenium"     % "1.1.0"
    )
}

addSbtPlugin("ch.epfl.scala"     % "sbt-scalafix"       % "0.9.24")
addSbtPlugin("org.scala-js"      % "sbt-scalajs"        % scalaJSVersion)
addSbtPlugin("com.jsuereth"      % "sbt-pgp"            % "1.1.2-1")
addSbtPlugin("com.github.gseitz" % "sbt-release"        % "1.0.13")
addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"       % "3.9.5")

{
  if (scalaJSVersion.startsWith("0."))
    Nil
  else
    Seq(
      addSbtPlugin("org.scala-js" % "sbt-jsdependencies" % "1.0.2")
    )
}

// addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.10.0")
