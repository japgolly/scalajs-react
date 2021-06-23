package japgolly.scalajs.react.util

import cats.effect.{IO, SyncIO}
import cats.effect.unsafe.IORuntime
import scala.util.Try

abstract class EffectCatsEffect extends EffectCallback {
  import Effect._

  implicit object syncIO extends Sync.WithDefaults[SyncIO] {

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
  }

  // ===================================================================================================================

  implicit lazy val io: Async[IO] =
    new AsyncIO()(IORuntime.global)

  class AsyncIO(implicit runtime: IORuntime) extends Async.WithDefaults[IO] {

    @inline override def delay[A](a: => A) =
      IO.delay(a)

    @inline override def pure[A](a: A) =
      IO.pure(a)

    @inline override def map[A, B](fa: IO[A])(f: A => B) =
      fa.map(f)

    @inline override def flatMap[A, B](fa: IO[A])(f: A => IO[B]) =
      fa.flatMap(f)

    override def finallyRun[A, B](fa: IO[A], fb: IO[B]) =
      fa.attempt.flatMap(ta =>
      fb.attempt.flatMap(tb =>
      IO.fromEither(if (ta.isRight && tb.isLeft) Left(tb.left.getOrElse(null)) else ta)))

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
      f => () => fa.unsafeRunAsync(ea => f(ea.toTry)())

    override def dispatch[A](fa: IO[A]): Unit =
      fa.unsafeRunAndForget()
  }

  private lazy val tryUnit: Try[Unit] =
    Try(())
}
