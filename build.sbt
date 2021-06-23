ThisBuild / organization := "com.github.japgolly.scalajs-react"
ThisBuild / homepage     := Some(url("https://github.com/japgolly/scalajs-react"))
ThisBuild / licenses     := ("Apache-2.0", url("http://opensource.org/licenses/Apache-2.0")) :: Nil
ThisBuild / shellPrompt  := ((s: State) => Project.extract(s).currentRef.project + "> ")
sonatypeProfileName      :=  "com.github.japgolly"

val callback              = ScalaJsReact.callback
val callbackExtCats       = ScalaJsReact.callbackExtCats
val callbackExtCatsEffect = ScalaJsReact.callbackExtCatsEffect
val coreDefCallback       = ScalaJsReact.coreDefCallback
val coreDefCatsEffect     = ScalaJsReact.coreDefCatsEffect
val coreExtCats           = ScalaJsReact.coreExtCats
val coreExtCatsEffect     = ScalaJsReact.coreExtCatsEffect
val coreGeneric           = ScalaJsReact.coreGeneric
val extra                 = ScalaJsReact.extra
val facadeMain            = ScalaJsReact.facadeMain
val facadeTest            = ScalaJsReact.facadeTest
val root                  = ScalaJsReact.root
val tests                 = ScalaJsReact.tests
val testUtil              = ScalaJsReact.testUtil
val util                  = ScalaJsReact.util
val utilDummyDefaults     = ScalaJsReact.utilDummyDefaults


// val ghpages               = ScalaJsReact.ghpages
// val ghpagesMacros         = ScalaJsReact.ghpagesMacros
// val scalaz72          = ScalaJsReact.scalaz72
// val monocleScalaz     = ScalaJsReact.monocleScalaz
// val monocleCats       = ScalaJsReact.monocleCats
// val monocle3          = ScalaJsReact.monocle3
//val testModule      = ScalaJsReact.testModule // Too damn buggy
