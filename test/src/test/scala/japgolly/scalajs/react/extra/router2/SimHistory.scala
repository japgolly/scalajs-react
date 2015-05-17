package japgolly.scalajs.react.extra.router2

import scalaz._
import scalaz.effect.IO

case class SimHistory(startUrl: AbsUrl) {

  var history = List(startUrl)
  var broadcasts = Vector.empty[List[AbsUrl]]

  def run[P, B](prog: RouteProg[B]): B = {
    import RouteCmd._
    type Cmd[A]  = RouteCmd[A]

    val interpretCmd: Cmd ~> IO = new (Cmd ~> IO) {
      override def apply[A](m: Cmd[A]): IO[A] = m match {
        case PushState(url)    => IO{history = url :: history}
        case ReplaceState(url) => IO{history = url :: history.tail}
        case BroadcastSync     => IO{broadcasts :+= history}
        case Return(a)         => IO(a)
        case Log(msg)          => IO(println(msg()))
      }
    }

    Free.runFC[Cmd, IO, B](prog)(interpretCmd).unsafePerformIO()
  }
}
