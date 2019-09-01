organization in ThisBuild := "com.github.japgolly.scalajs-react"
homepage     in ThisBuild := Some(url("https://github.com/japgolly/scalajs-react"))
licenses     in ThisBuild := ("Apache-2.0", url("http://opensource.org/licenses/Apache-2.0")) :: Nil
shellPrompt  in ThisBuild := ((s: State) => Project.extract(s).currentRef.project + "> ")

val root          = ScalajsReact.root
val core          = ScalajsReact.core
val extra         = ScalajsReact.extra
val scalaz72      = ScalajsReact.scalaz72
val monocle       = ScalajsReact.monocle
val monocleScalaz = ScalajsReact.monocleScalaz
val monocleCats   = ScalajsReact.monocleCats
val cats          = ScalajsReact.cats
val test          = ScalajsReact.test
//val testModule    = ScalajsReact.testModule // Too damn buggy
val ghpagesMacros = ScalajsReact.ghpagesMacros
val ghpages       = ScalajsReact.ghpages
