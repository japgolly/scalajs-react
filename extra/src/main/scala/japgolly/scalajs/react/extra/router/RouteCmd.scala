package japgolly.scalajs.react.extra.router

import RouteCmd._

sealed abstract class RouteCmd[A] {

  final def >>[B](next: RouteCmd[B]): RouteCmd[B] = {
    val init = this match {
      case Sequence(x, y) => x :+ y
      case _              => Vector.empty :+ this
    }
    Sequence(init, next)
  }

  @inline final def <<[B](prev: RouteCmd[B]): RouteCmd[A] =
    prev >> this
}

object RouteCmd {

  /**
   * Pushes the given data onto the session history stack with the specified title and, if provided, URL.
   */
  final case class PushState(url: AbsUrl) extends RouteCmd[Unit]

  /**
   * Updates the most recent entry on the history stack to have the specified data, title, and, if provided, URL.
   */
  final case class ReplaceState(url: AbsUrl) extends RouteCmd[Unit]

  /**
   * Sets `window.location.href` to a provided URL.
   */
  final case class SetWindowLocation(url: AbsUrl) extends RouteCmd[Unit]

  /**
   * Broadcast a message to Router component telling it to re-synchronise itself with the current window URL,
   * recalculating the route and content to render.
   */
  case object BroadcastSync extends RouteCmd[Unit]

  /**
   * Return an arbitrary value.
   */
  final case class Return[A](a: A) extends RouteCmd[A]

  final case class Log(msg: () => String) extends RouteCmd[Unit]

  final case class Sequence[A](init: Vector[RouteCmd[_]], last: RouteCmd[A]) extends RouteCmd[A]
}