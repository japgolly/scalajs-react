package japgolly.scalajs.react.effects

import cats.effect._
import cats.effect.laws._
import cats.effect.testkit.TestInstances
import cats.kernel.Eq
import cats.tests.CatsSuite
import cats.{Order, ~>, Id}
import japgolly.scalajs.react.{AsyncCallback, Callback}
import japgolly.scalajs.react.effects.AsyncCallbackEffects._
import org.scalacheck._
import scala.concurrent.duration._
import scala.scalajs.js

final class AsyncAsyncCallbackSpec extends CatsSuite with TestInstances /*with AsyncCallbackArbitraries*/ {

  val backlog = new js.Array[Callback]

  val asyncCallbackToIO = AsyncCallbackEffects.asyncCallbackToIO(a => {
    println("-- adding")
    backlog.push(a)
  })
  // val asyncCallbackToIO = AsyncCallbackEffects.asyncCallbackToIO(backlog.push(_))
  // val asyncCallbackToIO = defaultAsyncCallbackToIO

  implicit def arbitraryAsyncCallback[A: Arbitrary: Cogen](implicit t: Ticker, f: IO ~> AsyncCallback): Arbitrary[AsyncCallback[A]] =
    Arbitrary(arbitraryIO[A].arbitrary.map(f(_)))

  implicit def eqAsyncCallback[A](implicit A: Eq[A], t: Ticker): Eq[AsyncCallback[A]] =
    eqIOA[A].contramap(asyncCallbackToIO(_))

  implicit def orderAsyncCallbackFiniteDuration(implicit t: Ticker): Order[AsyncCallback[FiniteDuration]] =
    orderIoFiniteDuration.contramap(asyncCallbackToIO(_))

  private val someK: Id ~> Option =
    new ~>[Id, Option] { def apply[A](a: A) = a.some }

  def unsafeRun2[A](ioa: IO[A])(implicit ticker: Ticker): Outcome[Option, Throwable, A] =
    try {
      var results: Outcome[Option, Throwable, A] = Outcome.Succeeded(None)

      ioa
        .flatMap(IO.pure(_))
        .handleErrorWith(IO.raiseError(_))
        .unsafeRunAsyncOutcome { oc => results = oc.mapK(someK) }(unsafe
          .IORuntime(ticker.ctx, ticker.ctx, scheduler, () => (), unsafe.IORuntimeConfig()))

      def go(i: Int): Unit = {
        ticker.ctx.tickAll(1.second)
        println(backlog.length)
        if (backlog.length > 0) {
          for (f <- backlog)
            f.reset.runNow()
          backlog.clear()
          if (i < 100)
            go(i + 1)
        }
      }
      go(0)

      /*println("====================================")
      println(s"completed ioa with $results")
      println("====================================")*/

      results
    } catch {
      case t: Throwable =>
        t.printStackTrace()
        throw t
    }



  implicit def asyncCallbackToProp(implicit t: Ticker): AsyncCallback[Boolean] => Prop =
    a => {
      val io = asyncCallbackToIO(a)
      // println(backlog.length)
      // for (f <- backlog)
      //   f.reset.runNow()
      // backlog.clear()
      // ioBooleanToProp(io)
      val x = unsafeRun2(io)
      println("x = " + x)
      Prop(x.fold(false, _ => false, _.getOrElse(false)))
    }

  // locally {
    implicit val ticker = Ticker()

    // implicit val runtime = ioToAsyncCallback
    // implicit val runtime = ioToAsyncCallback(cats.effect.unsafe.IORuntime.global)
    implicit val runtime = //: IO ~> AsyncCallback =
      new IOToAsyncCallback(cats.effect.unsafe.IORuntime.global)

    implicit val instance = new AsyncAsyncCallback(runtime, asyncCallbackToIO)

    // checkAll("Async[cats.effect.IO]", AsyncTests[cats.effect.IO].async[Int, Int, Int](10.millis))
    // checkAll("Async[AsyncCallback]", AsyncTests[AsyncCallback].async[Int, Int, Int](10.millis))

    val x = AsyncTests[AsyncCallback]
    println("1) " + x)

    type F[A] = AsyncCallback[A]
    type A = Int
    type B = Int
    type C = Int
import cats.{Eq, Group, Order}
import cats.effect.kernel._
import cats.laws.discipline.SemigroupalTests.Isomorphisms

import org.scalacheck.util.Pretty

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

println("="*100)

    val ArbFA               = implicitly[Arbitrary[F[A]]]
    val ArbFB               = implicitly[Arbitrary[F[B]]]
    val ArbFC               = implicitly[Arbitrary[F[C]]]
    val ArbFU               = implicitly[Arbitrary[F[Unit]]]
    val ArbFAtoB            = implicitly[Arbitrary[F[A => B]]]
    val ArbFBtoC            = implicitly[Arbitrary[F[B => C]]]
    val ArbE                = implicitly[Arbitrary[Throwable]]
    val ArbST               = implicitly[Arbitrary[Sync.Type]]
    val ArbFiniteDuration   = implicitly[Arbitrary[FiniteDuration]]
    val ArbExecutionContext = implicitly[Arbitrary[ExecutionContext]]
    val CogenA              = implicitly[Cogen[A]]
    val CogenB              = implicitly[Cogen[B]]
    val CogenC              = implicitly[Cogen[C]]
    val CogenE              = implicitly[Cogen[Throwable]]
    val EqFA                = implicitly[Eq[F[A]]]
    val EqFB                = implicitly[Eq[F[B]]]
    val EqFC                = implicitly[Eq[F[C]]]
    val EqFU                = implicitly[Eq[F[Unit]]]
    val EqE                 = implicitly[Eq[Throwable]]
    val EqFEC               = implicitly[Eq[F[ExecutionContext]]]
    val EqFAB               = implicitly[Eq[F[Either[A, B]]]]
    val EqFEitherEU         = implicitly[Eq[F[Either[Throwable, Unit]]]]
    val EqFEitherEA         = implicitly[Eq[F[Either[Throwable, A]]]]
    val EqFEitherUA         = implicitly[Eq[F[Either[Unit, A]]]]
    val EqFEitherAU         = implicitly[Eq[F[Either[A, Unit]]]]
    val EqFOutcomeEA        = implicitly[Eq[F[Outcome[F, Throwable, A]]]]
    val EqFOutcomeEU        = implicitly[Eq[F[Outcome[F, Throwable, Unit]]]]
    val EqFABC              = implicitly[Eq[F[(A, B, C)]]]
    val EqFInt              = implicitly[Eq[F[Int]]]
    val OrdFFD              = implicitly[Order[F[FiniteDuration]]]
    val GroupFD             = implicitly[Group[FiniteDuration]]
    val exec                = implicitly[F[Boolean] => Prop]
    val iso                 = implicitly[Isomorphisms[F]]
    val faPP                = implicitly[F[A] => Pretty]
    val fuPP                = implicitly[F[Unit] => Pretty]
    val ePP                 = implicitly[Throwable => Pretty]
    val foaPP               = implicitly[F[Outcome[F, Throwable, A]] => Pretty]
    val feauPP              = implicitly[F[Either[A, Unit]] => Pretty]
    val feuaPP              = implicitly[F[Either[Unit, A]] => Pretty]
    val fouPP               = implicitly[F[Outcome[F, Throwable, Unit]] => Pretty]

    val arbInt = implicitly[Arbitrary[Int]]
    val eqInt = implicitly[Eq[Int]]

    val laws = AsyncLaws[F]

    val tolerance = 10.millis

    val laws2 = laws
    trait XXX extends GenTemporalTests[F, Throwable] with SyncTests[F] {

      val laws = laws2

      def rulesSpawn: RuleSet = new RuleSet {
        val name = "concurrent"
        val bases = Nil
        // val parents = Nil
        val parents = Seq(monadCancel[A, B, C])

        val props = Seq(
          // "race derives from racePair (left)" -> Prop.forAll(laws.raceDerivesFromRacePairLeft[A, B] _),
          // "race derives from racePair (right)" -> Prop.forAll(laws.raceDerivesFromRacePairRight[A, B] _),
          "race canceled identity (left)" -> Prop.forAll(laws.raceCanceledIdentityLeft[A] _),
          "race canceled identity (right)" -> Prop.forAll(laws.raceCanceledIdentityRight[A] _),
          // "race never non-canceled identity (left)" -> Prop.forAll(laws.raceNeverNoncanceledIdentityLeft[A] _),
          // "race never non-canceled identity (right)" -> Prop.forAll(laws.raceNeverNoncanceledIdentityRight[A] _),
          "fiber pure is completed pure" -> Prop.forAll(laws.fiberPureIsOutcomeCompletedPure[A] _),
          "fiber error is errored" -> Prop.forAll(laws.fiberErrorIsOutcomeErrored _),
          // "fiber cancelation is canceled" -> laws.fiberCancelationIsOutcomeCanceled,
          "fiber canceled is canceled" -> laws.fiberCanceledIsOutcomeCanceled,
          // "fiber never is never" -> laws.fiberNeverIsNever,
          // "fiber start of never is unit" -> laws.fiberStartOfNeverIsUnit,
          // "never dominates over flatMap" -> Prop.forAll(laws.neverDominatesOverFlatMap[A] _),
          // "uncancelable race not inherited" -> laws.uncancelableRaceNotInherited,
          // "uncancelable canceled is canceled" -> laws.uncancelableCancelCancels,
          // "uncancelable start is cancelable" -> laws.uncancelableStartIsCancelable,
          // "forceR never is never" -> Prop.forAll(laws.forceRNeverIsNever[A] _)
        )
      }

      def rulesTemporal: RuleSet = new RuleSet {
        val name = "temporal"
        val bases = Nil
        val parents = Nil
        // val parents = Seq(rulesSpawn, clock)
        // val parents = Seq(spawn[A, B, C], clock)

        val props = Seq(
          "monotonic sleep sum identity" -> Prop.forAll(laws.monotonicSleepSumIdentity _),
          "sleep race minimum" -> Prop.forAll(laws.sleepRaceMinimum _),
          "start sleep maximum" -> Prop.forAll(laws.startSleepMaximum _),
        )
      }

      def rules: RuleSet = RuleSet1
      object RuleSet1 extends RuleSet {
          val name = "async"
          val bases = Nil
          // val parents = Nil
          val parents = Seq(rulesTemporal, sync[A, B, C])
          // val parents = Seq(temporal[A, B, C](tolerance), sync[A, B, C])

          // temporal[A, B, C](tolerance)
          // implicit val t = Tolerance(tolerance)
          // val parents2 = Seq(spawn[A, B, C], clock)
          // val props2 = Seq(
          //   "monotonic sleep sum identity" -> Prop.forAll(laws.monotonicSleepSumIdentity _),
          //   "sleep race minimum" -> Prop.forAll(laws.sleepRaceMinimum _),
          //   "start sleep maximum" -> Prop.forAll(laws.startSleepMaximum _)
          // )

          // spawn[A, B, C]
          // clock

          val props = Seq(
            "never is derived from async" -> laws.neverIsDerivedFromAsync[A],
            // "evalOn never identity" -> Prop.forAll(laws.evalOnNeverIdentity _),
            /*
            "async right is uncancelable sequenced pure" -> Prop.forAll(laws.asyncRightIsUncancelableSequencedPure[A] _),
            "async left is uncancelable sequenced raiseError" -> Prop.forAll(laws.asyncLeftIsUncancelableSequencedRaiseError[A] _),
            "async repeated callback is ignored" -> Prop.forAll(laws.asyncRepeatedCallbackIgnored[A] _),
            "async cancel token is unsequenced on complete" -> Prop.forAll(laws.asyncCancelTokenIsUnsequencedOnCompletion[A] _),
            "async cancel token is unsequenced on error" -> Prop.forAll(laws.asyncCancelTokenIsUnsequencedOnError[A] _),
            // "never is derived from async" -> laws.neverIsDerivedFromAsync[A],
            "executionContext commutativity" -> Prop.forAll(laws.executionContextCommutativity[A] _),
            "evalOn local pure" -> Prop.forAll(laws.evalOnLocalPure _),
            "evalOn pure identity" -> Prop.forAll(laws.evalOnPureIdentity[A] _),
            "evalOn raiseError identity" -> Prop.forAll(laws.evalOnRaiseErrorIdentity _),
            "evalOn canceled identity" -> Prop.forAll(laws.evalOnCanceledIdentity _),
            // "evalOn never identity" -> Prop.forAll(laws.evalOnNeverIdentity _),
            */
          )
        }
    }

    val y = new XXX{}
    println("2) " + y)
    // checkAll("x", y.rules)

    // println(AsyncTests[AsyncCallback].async[Int, Int, Int](10.millis))
    checkAll("Async[AsyncCallback]", AsyncTests[AsyncCallback].async[Int, Int, Int](10.millis))

    // println("starting")
    // checkAll("tmp", ClockTests2[AsyncCallback].clock2)
    // println("done")



    // checkAll("tmp", SyncTests[AsyncCallback].sync[Int, Int, Int])
  // }
}

/*
async cancel token is unsequenced on complete
async cancel token is unsequenced on error
evalOn pure identity
evalOn raiseError identity
fiber cancelation is canceled
fiber canceled is canceled
fiber error is errored
fiber pure is completed pure
fiber start of never is unit
uncancelable canceled is canceled
uncancelable start is cancelable
*/