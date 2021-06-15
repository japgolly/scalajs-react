ThisBuild / organization := "com.github.japgolly.scalajs-react"
ThisBuild / homepage     := Some(url("https://github.com/japgolly/scalajs-react"))
ThisBuild / licenses     := ("Apache-2.0", url("http://opensource.org/licenses/Apache-2.0")) :: Nil
ThisBuild / shellPrompt  := ((s: State) => Project.extract(s).currentRef.project + "> ")
sonatypeProfileName      :=  "com.github.japgolly"

val root              = ScalajsReact.root
val facade            = ScalajsReact.facade
val util              = ScalajsReact.util
val utilDummyDefaults = ScalajsReact.utilDummyDefaults
val callback          = ScalajsReact.callback
val coreGeneral       = ScalajsReact.coreGeneral
val core              = ScalajsReact.core
val extra             = ScalajsReact.extra
val scalaz72          = ScalajsReact.scalaz72
val monocleScalaz     = ScalajsReact.monocleScalaz
val monocleCats       = ScalajsReact.monocleCats
val monocle3          = ScalajsReact.monocle3
val cats              = ScalajsReact.cats
val catsEffect        = ScalajsReact.catsEffect
val testUtil          = ScalajsReact.testUtil
//val testModule      = ScalajsReact.testModule // Too damn buggy
val tests              = ScalajsReact.tests
val ghpagesMacros     = ScalajsReact.ghpagesMacros
val ghpages           = ScalajsReact.ghpages
