package japgolly.scalajs.react.effects

import cats.effect.laws.discipline.EffectTests
import cats.effect.laws.discipline.arbitrary._
import cats.effect.laws.util.{TestContext, TestInstances}
import cats.kernel.Eq
import cats.tests.CatsSuite
import japgolly.scalajs.react.effects.AsyncCallbackEffects._
import japgolly.scalajs.react.AsyncCallback

final class AsyncCallbackEffectsSpec extends CatsSuite with TestInstances with AsyncCallbackArbitraries {
  implicit val ec: TestContext = TestContext()

  implicit def eqCallback[A](implicit A: Eq[A], ec: TestContext): Eq[AsyncCallback[A]] =
    new Eq[AsyncCallback[A]] {
      def eqv(x: AsyncCallback[A], y: AsyncCallback[A]): Boolean =
        eqFuture[A].eqv(x.unsafeToFuture(), y.unsafeToFuture())
    }

  checkAll("Effect[AsyncCallback]", EffectTests[AsyncCallback].effect[Int, Int, Int])
}
