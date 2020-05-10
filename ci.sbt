val CI = Option(System.getProperty("CI", "")).map(_.trim.toLowerCase).filter(_.nonEmpty)

ThisBuild / parallelExecution := CI.isEmpty

ThisBuild / scalacOptions ++= {
  CI match {
    case Some("full") => Seq("-Xdisable-assertions", "-Xelide-below", "OFF")
    case _            => Nil
  }
}

ThisBuild / scalaJSStage := {
  CI match {
    case Some("full") => FullOptStage
    case _            => FastOptStage
  }
}

// // START Scala.JS 0.6
// import org.scalajs.core.tools.sem.Semantics
//  ScalajsReact.test / Test / fullOptJS / scalaJSSemantics ~= {
//   CI match {
//     case Some("full") => _.withRuntimeClassNameMapper(Semantics.RuntimeClassNameMapper.discardAll())
//     case _            => identity
//   }
// }
// // END Scala.JS 0.6

// // START Scala.JS 1.0
// import org.scalajs.linker.interface.Semantics
// ThisBuild / fullOptJS / scalaJSLinkerConfig ~= {
//   CI match {
//     case Some("full") => _.withSemantics(_.withRuntimeClassNameMapper(Semantics.RuntimeClassNameMapper.discardAll()))
//     case _            => identity
//   }
// }
// // END Scala.JS 1.0
