addSbtPlugin("org.scala-js"       % "sbt-scalajs"          % "0.6.17")
addSbtPlugin("com.jsuereth"       % "sbt-pgp"              % "1.0.1")
addSbtPlugin("net.virtual-void"   % "sbt-dependency-graph" % "0.8.2")
addSbtPlugin("com.timushev.sbt"   % "sbt-updates"          % "0.3.0")

libraryDependencies += "org.scala-js" %% "scalajs-env-selenium" % "0.1.3"
