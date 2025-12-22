package japgolly.scalajs.react.util

import cats.Monad
import cats.effect.unsafe.IORuntime
import cats.effect.{IO, SyncIO}
import japgolly.scalajs.react.ReactCatsEffect
import japgolly.scalajs.react.util.Util.identityFn
import scala.util.{Success, Try}

abstract class EffectFallbacks2 extends EffectFallbacks3 {
  implicit def syncIO: Effect.Sync [SyncIO] = EffectCatsEffect.syncIO
  implicit def io    : Effect.Async[IO    ] = EffectCatsEffect.io
}

object EffectCatsEffect {
  import Effect._

  implicit object syncIO extends Sync.WithDefaults[SyncIO] {
    private[this] final val M = Monad[SyncIO]

    override val empty =
      SyncIO.unit

    @inline override def isEmpty[A](f: SyncIO[A]) =
      f eq empty

    @inline override def delay[A](a: => A) =
      SyncIO(a)

    @inline override def pure[A](a: A) =
      SyncIO.pure(a)

    @inline override def map[A, B](fa: SyncIO[A])(f: A => B) =
      fa.map(f)

    @inline override def flatMap[A, B](fa: SyncIO[A])(f: A => SyncIO[B]) =
      fa.flatMap(f)

    @inline override def runSync[A](f: => SyncIO[A]) =
      f.unsafeRunSync()

    override def tailrec[A, B](a: A)(f: A => SyncIO[Either[A, B]]) =
      M.tailRecM(a)(f)
  }

  // ===================================================================================================================

  class EffectIO extends Effect[IO] {
    protected final val M = Monad[IO]

    @inline override def delay[A](a: => A) =
      IO.delay(a)

    @inline override def pure[A](a: A) =
      IO.pure(a)

    @inline override def map[A, B](fa: IO[A])(f: A => B) =
      fa.map(f)

    @inline override def flatMap[A, B](fa: IO[A])(f: A => IO[B]) =
      fa.flatMap(f)

    override def finallyRun[A, B](fa: => IO[A], fb: => IO[B]) =
      fa.attempt.flatMap(ta =>
      fb.attempt.flatMap(tb =>
      IO.fromEither(if (ta.isRight && tb.isLeft) Left(tb.left.getOrElse(null)) else ta)))

    override def tailrec[A, B](a: A)(f: A => IO[Either[A, B]]) =
      M.tailRecM(a)(f)

    override def handleError[A, AA >: A](fa: => IO[A])(f: Throwable => IO[AA]) =
      fa.handleErrorWith(f)

    override def suspend[A](fa: => IO[A]) =
      IO(fa).flatMap(identityFn)
  }

  // ===================================================================================================================

  implicit lazy val io: AsyncIO =
    new AsyncIO(ReactCatsEffect.runtimeFn)

  class AsyncIO(runtime: () => IORuntime) extends EffectIO with Async.WithDefaults[IO] {

    override def async[A](fa: Async.Untyped[A]): IO[A] =
      IO.async(f => IO.delay {
        fa(ta => () => f(ta.toEither))()
        None
      })

    override def async_(onCompletion: Sync.Untyped[Unit] => Sync.Untyped[Unit]): IO[Unit] =
      for {
        p <- IO.delay(JsUtil.newPromise[Unit]())
        _ <- IO.delay(onCompletion(p._2(tryUnit))())
        _ <- fromJsPromise(p._1)
      } yield ()

    override def runAsync[A](fa: => IO[A]): Async.Untyped[A] =
      f => () => fa.unsafeRunAsync(ea => f(ea.toTry)())(runtime())

    override def dispatch[A](fa: IO[A]): Unit =
      fa.syncStep(Int.MaxValue).unsafeRunSync().fold(_.unsafeRunAndForget()(runtime()), _ => ())
  }

  private lazy val tryUnit: Try[Unit] =
    Success(())
}
