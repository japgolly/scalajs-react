libraryDependencies ++= Seq(
  "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0",
  "org.scala-js" %% "scalajs-env-selenium"     % "1.1.0")

addSbtPlugin("ch.epfl.scala"  % "sbt-scalafix"       % "0.9.28")
addSbtPlugin("com.github.sbt" % "sbt-pgp"            % "2.1.2")
addSbtPlugin("com.github.sbt" % "sbt-release"        % "1.0.15")
addSbtPlugin("org.scala-js"   % "sbt-jsdependencies" % "1.0.2")
addSbtPlugin("org.scala-js"   % "sbt-scalajs"        % "1.5.1")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype"       % "3.9.7")

// addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.10.0")
