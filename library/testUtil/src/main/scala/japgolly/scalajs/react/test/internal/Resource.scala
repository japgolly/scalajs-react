package japgolly.scalajs.react.test.internal

import japgolly.scalajs.react.util.Effect

/** 
  * Managed resource.
  *
  * @since 3.0.0
  */
class Resource[F[_]: Effect, A](private val acquire: F[A], private val release: A => F[Unit]) {
  private val F = implicitly[Effect[F]]

  def use[B](f: A => F[B]): F[B] = 
    F.flatMap(acquire)(a => F.finallyRun(f(a), release(a)))

  def use_[B](f: A => B): F[B] = 
    use(a => F.pure(f(a)))

  def flatMap[B](f: A => Resource[F, B]): Resource[F, B] = {
    var aOpt: Option[A] = None
    var bReleaseOpt: Option[B => F[Unit]] = None

    Resource.make(
      F.flatMap(acquire){ a =>
        aOpt = Some(a)
        val other: Resource[F, B] = f(a) 
        bReleaseOpt = Some(other.release)
        other.acquire
      },
      b => 
        (aOpt, bReleaseOpt) match {
          case (Some(a), Some(bRelease)) => F.finallyRun(bRelease(b), release(a))
          case _ => F.throwException(new IllegalStateException("Resource.flatMap: release attempted without acquire being invoked") )
        }
    )
  }

  def map[B](f: A => B): Resource[F, B] =
    flatMap(a => Resource.pure(f(a)))
}

object Resource {
  @inline def make[F[_]: Effect, A](acquire: F[A], release: A => F[Unit]): Resource[F, A] = 
    new Resource(acquire, release)
  
  @inline def make_[F[_]: Effect, A](acquire: => A, release: A => F[Unit]): Resource[F, A] = 
    make(Effect[F].delay(acquire), release)

  @inline def eval[F[_]: Effect, A](a: F[A]): Resource[F, A] = 
    Resource.make(a, _ => Effect[F].unit)

  @inline def pure[F[_]: Effect, A](a: A): Resource[F, A] =
    Resource.eval(Effect[F].pure(a))

  @inline def unit[F[_]: Effect]: Resource[F, Unit] = pure(())
}
