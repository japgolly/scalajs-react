addSbtPlugin("org.scala-lang.modules.scalajs" % "scalajs-sbt-plugin" % "0.5.1")

addSbtPlugin("com.lihaoyi" % "utest-js-plugin" % "0.1.7")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
    url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
        Resolver.ivyStylePatterns)

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.1")
