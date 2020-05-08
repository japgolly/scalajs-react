package japgolly.scalajs.react.effects

import japgolly.scalajs.react.{AsyncCallback, Callback}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck._

import scala.util.Try

trait AsyncCallbackArbitraries extends CallbackToArbitraries {

  val genCallback: Gen[Callback] = Callback.empty

  implicit val arbitraryCallback: Arbitrary[Callback] = Arbitrary(genCallback)

  implicit val cogenCallback: Cogen[Callback] = Cogen[Unit].contramap(_.runNow())

  implicit def catsEffectLawsArbitraryForAsyncCallback[A: Arbitrary: Cogen]: Arbitrary[AsyncCallback[A]] =
    Arbitrary(Gen.delay(genAsyncCallback[A]))

  def genAsyncCallback[A: Arbitrary: Cogen]: Gen[AsyncCallback[A]] =
    Gen.frequency(
      1 -> genPure[A].map(_.asAsyncCallback),
      1 -> genApply[A].map(_.asAsyncCallback),
      1 -> genFail[A].map(_.asAsyncCallback),
      1 -> genAsync[A],
      1 -> genNestedAsync[A],
      1 -> getMapOne[A],
      1 -> getMapTwo[A],
      2 -> genFlatMap[A]
    )

  def genAsync[A: Arbitrary]: Gen[AsyncCallback[A]] =
    arbitrary[(Try[A] => Callback) => Callback].map(AsyncCallback.apply)

  def genNestedAsync[A: Arbitrary: Cogen]: Gen[AsyncCallback[A]] =
    arbitrary[(Try[AsyncCallback[A]] => Callback) => Callback]
      .map(k => AsyncCallback(k).flatMap(x => x))

  def genFlatMap[A: Arbitrary: Cogen]: Gen[AsyncCallback[A]] =
    for {
      ioa <- arbitrary[AsyncCallback[A]]
      f <- arbitrary[A => AsyncCallback[A]]
    } yield ioa.flatMap(f)

  def getMapOne[A: Arbitrary: Cogen]: Gen[AsyncCallback[A]] =
    for {
      ioa <- arbitrary[AsyncCallback[A]]
      f <- arbitrary[A => A]
    } yield ioa.map(f)

  def getMapTwo[A: Arbitrary: Cogen]: Gen[AsyncCallback[A]] =
    for {
      ioa <- arbitrary[AsyncCallback[A]]
      f1 <- arbitrary[A => A]
      f2 <- arbitrary[A => A]
    } yield ioa.map(f1).map(f2)
}
