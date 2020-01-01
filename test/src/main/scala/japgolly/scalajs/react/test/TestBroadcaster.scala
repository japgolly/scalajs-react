package japgolly.scalajs.react.test

import japgolly.scalajs.react.{Callback, CallbackTo}
import japgolly.scalajs.react.extra.{Broadcaster, Px}

class TestBroadcaster[I, A](initialValue: A, f: I => A) extends Broadcaster[I] {

  private var latest = initialValue

  override def broadcast(i: I): Callback =
    Callback { latest = f(i) } >> super.broadcast(i)

  def getLatestValue(): A =
    latest

  val latestValue: CallbackTo[A] =
    CallbackTo(latest)

  val px: Px[A] =
    Px(latest).withoutReuse.autoRefresh
}

object TestBroadcaster {

  def apply[A](initialValue: A): TestBroadcaster[A, A] =
    new TestBroadcaster[A, A](initialValue, identity)

  def apply[A]: TestBroadcaster[A, Option[A]] =
    new TestBroadcaster[A, Option[A]](None, Some(_))

}