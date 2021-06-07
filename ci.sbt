val CI = Option(System.getProperty("CI", "")).map(_.trim.toLowerCase).filter(_.nonEmpty)

ThisBuild / parallelExecution := CI.isEmpty

ThisBuild / scalaJSStage := {
  CI match {
    case Some("full") => FullOptStage
    case _            => FastOptStage
  }
}
