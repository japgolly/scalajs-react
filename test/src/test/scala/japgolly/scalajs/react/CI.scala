package japgolly.scalajs.react

import japgolly.scalajs.react.internal.CompileTimeInfo

object CI {
  private val value =
    CompileTimeInfo.envVarOrSysProp("CI").map(_.trim.toLowerCase).filter(_.nonEmpty)

  println(s"CI = ${value}")

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
