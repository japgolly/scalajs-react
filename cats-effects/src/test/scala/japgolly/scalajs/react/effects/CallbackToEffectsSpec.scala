package japgolly.scalajs.react.effects

import cats.effect.laws.discipline.SyncTests
import cats.effect.laws.util.{TestContext, TestInstances}
import cats.kernel.Eq
import cats.tests.CatsSuite
import japgolly.scalajs.react.CallbackTo
import japgolly.scalajs.react.effects.CallbackToEffects._

final class CallbackToEffectsSpec extends CatsSuite with TestInstances with CallbackToArbitraries {
  implicit val ec: TestContext = TestContext()

  implicit def eqCallback[A](implicit A: Eq[A], ec: TestContext): Eq[CallbackTo[A]] =
    new Eq[CallbackTo[A]] {
      def eqv(x: CallbackTo[A], y: CallbackTo[A]): Boolean =
        eqFuture[A].eqv(x.asAsyncCallback.unsafeToFuture(), y.asAsyncCallback.unsafeToFuture())
    }

  checkAll("Sync[CallbackTo]", SyncTests[CallbackTo].sync[Int, Int, Int])
}