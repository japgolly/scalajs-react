package japgolly.scalajs.react.test

import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.util.DefaultEffects

abstract class TestBroadcasterF[F[_], I, A](initialValue: A, f: I => A) extends BroadcasterF[F, I] { self =>
  import self.{listenableEffect => F}

  private var latest = initialValue

  override def broadcast(i: I): F[Unit] =
    F.chain(F.delay { latest = f(i) }, super.broadcast(i))

  def getLatestValue(): A =
    latest

  val latestValue: F[A] =
    F.delay(latest)

  val px: Px[A] =
    Px(latest).withoutReuse.autoRefresh
}

// =====================================================================================================================

class TestBroadcaster[I, A](initialValue: A, f: I => A)
  extends TestBroadcasterF[DefaultEffects.Sync, I, A](initialValue, f)
     with Broadcaster[I]

object TestBroadcaster {

  def apply[A](initialValue: A): TestBroadcaster[A, A] =
    new TestBroadcaster[A, A](initialValue, identity)

  def apply[A]: TestBroadcaster[A, Option[A]] =
    new TestBroadcaster[A, Option[A]](None, Some(_))

}