package japgolly.scalajs.react.callback

trait Timer {
  type Handle
  def delay(ms: Long)(proc: => Any): Handle
  def cancel(handle: Handle): Unit
}

object Timer {

  implicit object RealTimer extends Timer {
    import scala.scalajs.js.timers._

    override type Handle = SetTimeoutHandle

    override def delay(ms: Long)(proc: => Any): SetTimeoutHandle =
      setTimeout(ms.toDouble)(proc)

    override def cancel(handle: SetTimeoutHandle): Unit =
      try
        clearTimeout(handle)
      catch {
        case _: Throwable =>
      }
  }

}
