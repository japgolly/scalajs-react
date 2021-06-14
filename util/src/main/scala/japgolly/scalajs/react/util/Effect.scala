package japgolly.scalajs.react.util

import japgolly.scalajs.react.userdefined
import japgolly.scalajs.react.util.OptionLike
import japgolly.scalajs.react.util.Util.identityFn
import scala.scalajs.js
import scala.util.Try

object Effect
    extends EffectCallback
       with EffectCatsEffect
       with userdefined.Effects {

  // ===================================================================================================================

  type Id[A] = A

  trait UnsafeSync[F[_]] extends Monad[F] {
    def runSync[A]      (fa: => F[A])                               : A
    def option_[O[_], A](ofa: => O[F[A]])(implicit O: OptionLike[O]): F[Unit]

    def toJsFn0[A](f: => F[A]): js.Function0[A] =
      () => runSync(f)

    def toJsFn1[A, Z](f: A => F[Z]): js.Function1[A, Z] =
      a => runSync(f(a))

    def suspend[A](fa: => F[A]): F[A] =
      flatMap(delay(fa))(identityFn)

    /** Wraps this callback in a `try-finally` block and runs the given callback in the `finally` clause, after the
      * current callback completes, be it in error or success.
      */
    def finallyRun[A, B](fa: => F[A], runFinally: => F[B]): F[A] =
      delay { try runSync(fa) finally runSync(runFinally) }

    final def transSync[G[_], A](ga: => G[A])(implicit g: UnsafeSync[G]): F[A] =
      if (this eq g)
        ga.asInstanceOf[F[A]]
      else
        delay(g.runSync(ga))
  }

  object UnsafeSync {
    implicit lazy val id: UnsafeSync[Id] = new UnsafeSync[Id] {
      override def delay  [A]      (a: => A)                                = a
      override def pure   [A]      (a: A)                                   = a
      override def map    [A, B]   (a: A)(f: A => B)                        = f(a)
      override def flatMap[A, B]   (a: A)(f: A => B)                        = f(a)
      override def runSync[A]      (a: => A)                                = a
      override def option_[O[_], A](oa: => O[A])(implicit O: OptionLike[O]) = O.foreach(oa)(_ => ())
    }
  }

  // ===================================================================================================================

  trait Sync[F[_]] extends UnsafeSync[F] {

    val empty: F[Unit]
    def isEmpty(f: F[Unit]): Boolean
    def fromJsFn0[A](f: js.Function0[A]): F[A]

    def reset[A](fa: F[A]): F[Unit] =
      delay(
        try
          runSync(fa): Unit
        catch {
          case t: Throwable =>
            t.printStackTrace()
        }
      )

    def runAll(callbacks: F[_]*): F[Unit] =
      callbacks.foldLeft(empty)((x, y) => chain(x, reset(y)))

    def traverse_[A, B](as: Iterable[A])(f: A => F[B]): F[Unit] =
      delay {
        for (a <- as)
          runSync(f(a))
      }

    def sequence_[A](fas: Iterable[F[A]]): F[Unit] =
      traverse_(fas)(identityFn)

    def traverseList[A, B](as: List[A])(f: A => F[B]): F[List[B]] =
      delay {
        val l = List.newBuilder[B]
        for (a <- as)
          l += runSync(f(a))
        l.result()
      }

    def sequenceList[A](fas: List[F[A]]): F[List[A]] =
      traverseList(fas)(identityFn)

    implicit val semigroupSyncUnit: Semigroup[F[Unit]] =
      Semigroup((f, g) => flatMap(f)(_ => g))

    val semigroupSyncOr: Semigroup[F[Boolean]] =
      Semigroup((f, g) => delay(runSync(f) || runSync(g)))

    def when_[A](cond: Boolean)(fa: => F[A]): F[Unit] =
      if (cond) map(fa)(_ => ()) else empty

    @inline final def unless_[A](cond: Boolean)(fa: => F[A]): F[Unit] =
      when_(!cond)(fa)

    def handleError[A, AA >: A](fa: F[A])(f: Throwable => F[AA]): F[AA] =
      delay[AA](
        try
          runSync(fa)
        catch {
          case t: Throwable => runSync(f(t))
        }
      )
  }

  object Sync {
    type Untyped[A] = js.Function0[A]

    val empty: Untyped[Unit] =
      () => ()
  }

  // ===================================================================================================================

  trait Async[F[_]] {
    def async[A](f: Async.Untyped[A]): F[A]
    def runAsync[A](fa: => F[A]): Async.Untyped[A]
    def toJsPromise[A](fa: => F[A]): () => js.Promise[A]

    def first[A](f: Async.Untyped[A]): F[A] =
      async(g => () => {
        var first = true
        f(ea => () => {
          if (first) {
            first = false
            g(ea).apply()
          }
        }).apply()
      })

    // TODO: FX: Confirm this works. If it does then why does AsyncCallback.viaCallback use a promise?
    final def async_(onCompletion: Sync.Untyped[Unit] => Sync.Untyped[Unit]): F[Unit] =
      async[Unit](f => onCompletion(f(Try(()))))

    final def transAsync[G[_], A](ga: => G[A])(implicit g: Async[G]): F[A] =
      if (this eq g)
        ga.asInstanceOf[F[A]]
      else
        async(g.runAsync(ga)(_))
  }

  object Async {
    type Untyped[A] = (Try[A] => Sync.Untyped[Unit]) => Sync.Untyped[Unit]
  }
}

// =====================================================================================================================

trait EffectCallback
trait EffectCatsEffect
