package japgolly.scalajs.react.effects

import cats.effect.SyncIO
import cats.effect.laws.SyncTests
import cats.effect.testkit.TestInstances
import cats.kernel.Eq
import cats.tests.CatsSuite
import japgolly.scalajs.react.CallbackTo
import japgolly.scalajs.react.effects.CallbackToEffects._
import org.scalacheck.Prop

final class SyncCallbackSpec extends CatsSuite with TestInstances with CallbackToArbitraries {

  implicit def eqCallback[A](implicit A: Eq[A]): Eq[CallbackTo[A]] =
    implicitly[Eq[SyncIO[A]]].contramap(callbackToSyncIO(_))

  implicit def callbackToProp: CallbackTo[Boolean] => Prop =
    c => implicitly[SyncIO[Boolean] => Prop].apply(callbackToSyncIO(c))

  checkAll("Sync[CallbackTo]", SyncTests[CallbackTo].sync[Int, Int, Int])
}
