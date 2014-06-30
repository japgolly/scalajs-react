addSbtPlugin("org.scala-lang.modules.scalajs" % "scalajs-sbt-plugin" % "0.5.0")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.lihaoyi" % "workbench" % "0.1.2")

// resolvers += Resolver.url(
  // "bintray-sbt-plugin-releases",
    // url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
        // Resolver.ivyStylePatterns)

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.1")

