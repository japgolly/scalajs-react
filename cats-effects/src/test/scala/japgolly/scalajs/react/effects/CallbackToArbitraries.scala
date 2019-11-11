package japgolly.scalajs.react.effects

import japgolly.scalajs.react.CallbackTo
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck._

trait CallbackToArbitraries {

  implicit def catsEffectLawsArbitraryForCallbackTo[A: Arbitrary: Cogen]: Arbitrary[CallbackTo[A]] =
    Arbitrary(Gen.delay(genCallbackTo[A]))

  def genCallbackTo[A: Arbitrary: Cogen]: Gen[CallbackTo[A]] =
    Gen.frequency(5 -> genPure[A], 5 -> genApply[A], 1 -> genFail[A], 5 -> genBindSuspend[A])

  def genPure[A: Arbitrary]: Gen[CallbackTo[A]] =
    arbitrary[A].map(CallbackTo.pure)

  def genApply[A: Arbitrary]: Gen[CallbackTo[A]] =
    arbitrary[A].map(CallbackTo.apply(_))

  def genFail[A]: Gen[CallbackTo[A]] =
    arbitrary[Throwable].map(CallbackTo.throwException)

  def genBindSuspend[A: Arbitrary: Cogen]: Gen[CallbackTo[A]] =
    arbitrary[A].map(CallbackTo.apply(_).flatMap(CallbackTo.pure))
}
