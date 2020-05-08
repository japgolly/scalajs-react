package japgolly.scalajs.react

import japgolly.scalajs.react.internal.CompileTimeInfo

object TestEnv {
  private val ciValue =
    CompileTimeInfo.sysPropOrEnvVar("CI").map(_.trim.toLowerCase).filter(_.nonEmpty)

  def inCI: Boolean =
    ciValue.isDefined

  def fullCI: Boolean =
    ciValue.contains("full")

  def unlessFullCI(body: => Any): Unit =
    if (!fullCI)
      body

  val scalaJs1: Boolean =
    CompileTimeInfo.envVar("SCALAJS_VERSION").forall(_.startsWith("1."))

  def scalaJs06: Boolean =
    !scalaJs1
}
