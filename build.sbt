ThisBuild / organization  := "com.github.japgolly.scalajs-react"
ThisBuild / homepage      := Some(url("https://github.com/japgolly/scalajs-react"))
ThisBuild / licenses      := ("Apache-2.0", url("http://opensource.org/licenses/Apache-2.0")) :: Nil
ThisBuild / shellPrompt   := ((s: State) => Project.extract(s).currentRef.project + "> ")
ThisBuild / versionScheme := Some("early-semver")
sonatypeProfileName       := "com.github.japgolly"

val callback              = ScalaJsReact.callback
val callbackExtCats       = ScalaJsReact.callbackExtCats
val callbackExtCatsEffect = ScalaJsReact.callbackExtCatsEffect
val coreBundleCallback    = ScalaJsReact.coreBundleCallback
val coreBundleCatsEffect  = ScalaJsReact.coreBundleCatsEffect
val coreExtCats           = ScalaJsReact.coreExtCats
val coreExtCatsEffect     = ScalaJsReact.coreExtCatsEffect
val coreGeneric           = ScalaJsReact.coreGeneric
val extra                 = ScalaJsReact.extra
val extraExtMonocle2      = ScalaJsReact.extraExtMonocle2
val extraExtMonocle3      = ScalaJsReact.extraExtMonocle3
val facadeMain            = ScalaJsReact.facadeMain
val facadeTest            = ScalaJsReact.facadeTest
val ghpages               = ScalaJsReact.ghpages
val ghpagesMacros         = ScalaJsReact.ghpagesMacros
val root                  = ScalaJsReact.root
val scalafixRules         = ScalaJsReact.scalafixRules
val tests                 = ScalaJsReact.tests
val testUtil              = ScalaJsReact.testUtil
val util                  = ScalaJsReact.util
val utilCatsEffect        = ScalaJsReact.utilCatsEffect
val utilDummyDefaults     = ScalaJsReact.utilDummyDefaults
val utilFallbacks         = ScalaJsReact.utilFallbacks

//val testModule      = ScalaJsReact.testModule // Too damn buggy
