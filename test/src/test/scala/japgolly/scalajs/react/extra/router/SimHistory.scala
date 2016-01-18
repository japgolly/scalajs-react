package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react._

case class SimHistory(startUrl: AbsUrl) {

  var history = List(startUrl)
  var broadcasts = Vector.empty[List[AbsUrl]]

  def reset(): Unit =
    reset(startUrl)

  def reset(url: AbsUrl): Unit = {
    history = List(url)
    broadcasts = Vector.empty
  }

  override def toString =
    s"""
       |SimHistory($startUrl)
       | - history: (${history.length})
       |${history.map("     " + _) mkString "\n"}
       | - broadcasts: (${broadcasts.length})
       |${broadcasts.map("     " + _) mkString "\n"}
     """.stripMargin

  def interpret[B](cmd: RouteCmd[B]): CallbackTo[B] = {
    import RouteCmd._
    cmd match {
      case PushState(url)    => Callback{history = url :: history}
      case ReplaceState(url) => Callback{history = url :: history.tail}
      case BroadcastSync     => Callback{broadcasts :+= history}
      case Return(a)         => CallbackTo.pure(a)
      case Log(msg)          => Callback.log("[SimHistory] " + msg())
      case Sequence(a, b)    => a.foldLeft[CallbackTo[_]](Callback.empty)(_ >> interpret(_)) >> interpret(b)
    }
  }

  def run[P, B](cmd: RouteCmd[B]): B =
    interpret(cmd).runNow()
}
