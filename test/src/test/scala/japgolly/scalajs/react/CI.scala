package japgolly.scalajs.react

import japgolly.scalajs.react.internal.CompileTimeInfo

object CI {
  private val value =
    CompileTimeInfo.sysPropOrEnvVar("CI").map(_.trim.toLowerCase).filter(_.nonEmpty)

  def env: Boolean =
    value.isDefined

  def full: Boolean =
    value.contains("full")

  def unlessFull(body: => Any): Unit =
    if (full)
      ()
    else {
      body
      ()
    }
}
