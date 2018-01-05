addSbtPlugin("org.scala-js"       % "sbt-scalajs"          % "0.6.21")
addSbtPlugin("ch.epfl.scala"      % "sbt-scalajs-bundler"  % "0.8.0")
addSbtPlugin("com.jsuereth"       % "sbt-pgp"              % "1.1.0")
addSbtPlugin("net.virtual-void"   % "sbt-dependency-graph" % "0.9.0")
addSbtPlugin("com.timushev.sbt"   % "sbt-updates"          % "0.3.3")

libraryDependencies += "org.scala-js" %% "scalajs-env-selenium" % "0.2.0"
