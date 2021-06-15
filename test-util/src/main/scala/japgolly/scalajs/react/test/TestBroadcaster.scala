package japgolly.scalajs.react.test

import japgolly.scalajs.react.extra.{Broadcaster, Px}
import japgolly.scalajs.react.util.DefaultEffects.Sync

class TestBroadcaster[I, A](initialValue: A, f: I => A) extends Broadcaster[I] {

  private var latest = initialValue

  override def broadcast(i: I): Sync[Unit] =
    Sync.chain(Sync.delay { latest = f(i) }, super.broadcast(i))

  def getLatestValue(): A =
    latest

  val latestValue: Sync[A] =
    Sync.delay(latest)

  val px: Px[A] =
    Px(latest).withoutReuse.autoRefresh
}

object TestBroadcaster {

  def apply[A](initialValue: A): TestBroadcaster[A, A] =
    new TestBroadcaster[A, A](initialValue, identity)

  def apply[A]: TestBroadcaster[A, Option[A]] =
    new TestBroadcaster[A, Option[A]](None, Some(_))

}