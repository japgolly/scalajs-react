libraryDependencies ++= Seq(
  "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0",
  "org.scala-js" %% "scalajs-env-selenium"     % "1.1.1")

addSbtPlugin("ch.epfl.scala"  % "sbt-scalafix"       % "0.10.0")
addSbtPlugin("com.github.sbt" % "sbt-ci-release"     % "1.5.10")
addSbtPlugin("org.scala-js"   % "sbt-jsdependencies" % "1.0.2")
addSbtPlugin("org.scala-js"   % "sbt-scalajs"        % "1.10.1")
