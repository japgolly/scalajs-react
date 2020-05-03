libraryDependencies ++= Seq(
  "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.0.0",
  "org.scala-js" %% "scalajs-env-selenium"     % "1.0.0"
)

addSbtPlugin("org.scala-js"      % "sbt-jsdependencies" % "1.0.0")
addSbtPlugin("org.scala-js"      % "sbt-scalajs"        % "1.0.1")
addSbtPlugin("com.jsuereth"      % "sbt-pgp"            % "1.1.2")
addSbtPlugin("com.github.gseitz" % "sbt-release"        % "1.0.13")

// addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.10.0")
