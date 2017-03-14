addSbtPlugin("org.scala-js"       % "sbt-scalajs"          % "0.6.14")
addSbtPlugin("com.jsuereth"       % "sbt-pgp"              % "1.0.1")
addSbtPlugin("ch.epfl.scala"      % "sbt-scalajs-bundler"  % "0.5.0")

addSbtPlugin("net.virtual-void"   % "sbt-dependency-graph" % "0.8.2")
addSbtPlugin("com.timushev.sbt"   % "sbt-updates"          % "0.3.0")

libraryDependencies += "org.scala-js" %% "scalajs-env-selenium" % "0.1.3"
