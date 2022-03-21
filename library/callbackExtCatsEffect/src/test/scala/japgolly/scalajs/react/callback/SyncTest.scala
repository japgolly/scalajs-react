package japgolly.scalajs.react.callback

import cats.effect.laws.SyncTests
import cats.effect.testkit.TestInstances
import cats.kernel.Eq
import cats.tests.CatsSuite
import org.scalacheck._

final class SyncTest extends CatsSuite with TestInstances {
  import CallbackCatsEffect._

  implicit def arbCallback[A: Arbitrary: Cogen]: Arbitrary[CallbackTo[A]] =
    Arbitrary(arbitrarySyncIO[A].arbitrary.map(syncIOToCallback(_)))

  implicit def eqCallback[A](implicit A: Eq[A]): Eq[CallbackTo[A]] =
    eqSyncIOA[A].contramap(callbackToSyncIO(_))

  implicit def callbackToProp: CallbackTo[Boolean] => Prop =
    c => syncIoBooleanToProp(callbackToSyncIO(c))

  checkAll("Sync[CallbackTo]", SyncTests[CallbackTo].sync[Int, Int, Int])
}
