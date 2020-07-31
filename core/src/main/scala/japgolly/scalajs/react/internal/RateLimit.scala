package japgolly.scalajs.react.internal

import scala.scalajs.js

object RateLimit {

  type Clock = () => Long

  val realClock: Clock =
    () => System.currentTimeMillis()

  def fn[A, B](run: A => B, maxPerWindow: Int, windowMs: Long, clock: Clock = realClock): A => Option[B] = {
    val windowRuns = new js.Array[Long]

    a => {
      val now         = clock()
      val windowStart = now - windowMs

      // prune current window
      while (windowRuns.nonEmpty && windowRuns(0) < windowStart)
        windowRuns.shift()

      if (windowRuns.length >= maxPerWindow)
        None
      else {
        windowRuns.push(now)
        Some(run(a))
      }
    }
  }

  def fn0[A](run: () => A, maxPerWindow: Int, windowMs: Long, clock: Clock = realClock): () => Option[A] = {
    val f = fn[Unit, A](
      run          = _ => run(),
      windowMs     = windowMs,
      maxPerWindow = maxPerWindow,
      clock        = clock,
    )
    () => f(())
  }

}
