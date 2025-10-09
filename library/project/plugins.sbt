libraryDependencies ++= Seq(
  "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0",
  "org.scala-js" %% "scalajs-env-selenium"     % "1.1.1")

addSbtPlugin("ch.epfl.scala"  % "sbt-scalafix"       % "0.12.1")
addSbtPlugin("com.github.sbt" % "sbt-ci-release"     % "1.11.2")
addSbtPlugin("org.scala-js"   % "sbt-jsdependencies" % "1.0.2")
addSbtPlugin("org.scala-js"   % "sbt-scalajs"        % "1.20.1")
