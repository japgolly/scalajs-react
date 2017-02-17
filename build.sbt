version in ThisBuild := "1.0.0-SNAPSHOT"

shellPrompt in ThisBuild := ((s: State) => Project.extract(s).currentRef.project + "> ")

val root          = ScalajsReact.root
val core          = ScalajsReact.core
val extra         = ScalajsReact.extra
//val test          = ScalajsReact.test
val scalaz72      = ScalajsReact.scalaz72
val monocle       = ScalajsReact.monocle
//val ghpagesMacros = ScalajsReact.ghpagesMacros
//val ghpages       = ScalajsReact.ghpages
