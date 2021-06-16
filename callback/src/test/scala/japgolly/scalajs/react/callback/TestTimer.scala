package japgolly.scalajs.react.callback

import scala.annotation.tailrec
import scala.collection.immutable.SortedSet
import scala.util.Try

class TestTimer extends Timer {
  import TestTimer.DelayedProc

  private var _time = 0.0
  private var _handles = SortedSet.empty[Handle]

  override type Handle = DelayedProc

  override def delay(ms: Long)(proc: => Any): Handle = {
    val h = DelayedProc(_time + ms, () => proc)
    _handles += h
    h
  }

  override def cancel(handle: Handle): Unit =
    handle.cancelled = true

  def time = _time

  def progressTimeBy(ms: Double): Unit = {
    _time += ms

    @tailrec
    def action(): Unit =
      if (_handles.nonEmpty) {
        val h = _handles.head
        if (h.runAtTime <= _time) {
          _handles = _handles.tail
          if (!h.cancelled) {
            h.cancelled = true
            Try(h.proc())
          }
          action()
        }
      }

    action()
  }
}

object TestTimer {

  final case class DelayedProc(runAtTime: Double, proc: () => Any) {
    var cancelled = false
  }

  implicit val ordering: Ordering[DelayedProc] =
    Ordering.by(_.runAtTime)
}