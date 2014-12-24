package japgolly.scalajs.react.extras.router

sealed trait RouteCmd[+P, A]

object RouteCmd {

  /**
   * Pushes the given data onto the session history stack with the specified title and, if provided, URL.
   */
  case class PushState[P](url: AbsUrl) extends RouteCmd[P, Unit]

  /**
   * Updates the most recent entry on the history stack to have the specified data, title, and, if provided, URL.
   */
  case class ReplaceState[P](url: AbsUrl) extends RouteCmd[P, Unit]

  /**
   * Broadcast to listeners (the Router) that the location (URL) has changed, and the current route must be
   * recalculated and potentially changed.
   */
  case object BroadcastLocChange extends RouteCmd[Nothing, Unit]

  case class ReturnLoc[P](loc: Location[P]) extends RouteCmd[P, Location[P]]
}