package japgolly.scalajs.react.extra.router2

sealed trait RouteCmd[A]

object RouteCmd {

  /**
   * Pushes the given data onto the session history stack with the specified title and, if provided, URL.
   */
  case class PushState(url: AbsUrl) extends RouteCmd[Unit]

  /**
   * Updates the most recent entry on the history stack to have the specified data, title, and, if provided, URL.
   */
  case class ReplaceState(url: AbsUrl) extends RouteCmd[Unit]

  /**
   * Broadcast a message to Router component telling it to re-synchronise itself with the current window URL,
   * recalculating the route and content to render.
   */
  case object BroadcastSync extends RouteCmd[Unit]

  /**
   * Return an arbitrary value.
   */
  case class Return[A](a: A) extends RouteCmd[A]

  case class Log(msg: () => String) extends RouteCmd[Unit]
}