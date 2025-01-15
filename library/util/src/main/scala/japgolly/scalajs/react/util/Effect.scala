package japgolly.scalajs.react.util

import japgolly.scalajs.react.util.Util.identityFn
import scala.annotation.tailrec
import scala.scalajs.js
import scala.util.Try

trait Effect[F[_]] extends Monad[F] {
  def delay      [A]         (a: => A)                           : F[A]
  def handleError[A, AA >: A](fa: => F[A])(f: Throwable => F[AA]): F[AA]
  def suspend    [A]         (fa: => F[A])                       : F[A]

  /** Wraps this callback in a `try-finally` block and runs the given callback in the `finally` clause, after the
    * current callback completes, be it in error or success.
    */
  def finallyRun[A, B](fa: => F[A], runFinally: => F[B]): F[A]

  @inline final def throwException[A](t: Throwable): F[A] =
    delay(throw t)
}

object Effect extends EffectFallbacks {
  @inline def apply[F[_]](implicit F: Effect[F]): Effect[F] = F

  trait WithDefaults[F[_]] extends Effect[F] {
  }

  trait Dispatch[F[_]] extends Effect[F] {
    /** Fire and forget, could be sync or async */
    def dispatch[A](fa: F[A]): Unit
    def dispatchFn[A](fa: => F[A]): js.Function0[Unit]
  }

  object Dispatch {
    trait WithDefaults[F[_]] extends Dispatch[F] with Effect.WithDefaults[F] {
      override def dispatchFn[A](fa: => F[A]): js.Function0[Unit] =
        () => dispatch(fa)
    }
  }

  // ===================================================================================================================

  type Id[A] = A

  trait UnsafeSync[F[_]] extends Dispatch[F] {
    def runSync     [A]   (fa: => F[A])                     : A
    def sequence_   [A]   (fas: => Iterable[F[A]])          : F[Unit]
    def sequenceList[A]   (fas: => List[F[A]])              : F[List[A]]
    def toJsFn      [A]   (fa: => F[A])                     : js.Function0[A]
    def traverse_   [A, B](as: => Iterable[A])(f: A => F[B]): F[Unit]
    def traverseList[A, B](as: => List[A])(f: A => F[B])    : F[List[B]]
    def when_       [A]   (cond: => Boolean)(fa: => F[A])   : F[Unit]

    @inline final def unless_[A](cond: => Boolean)(fa: => F[A]): F[Unit] =
      when_(!cond)(fa)

    final def subst[G[_], X[_[_]]](xg: X[G])(xf: => X[F])(implicit g: UnsafeSync[G]): X[F] =
      if (this eq g) xg.asInstanceOf[X[F]] else xf

    final def transSync[G[_], A](ga: => G[A])(implicit g: UnsafeSync[G]): F[A] =
      if (this eq g)
        ga.asInstanceOf[F[A]]
      else
        delay(g.runSync(ga))

    final def transSyncFn1[G[_], A, B](f: A => G[B])(implicit g: UnsafeSync[G]): A => F[B] =
      if (this eq g)
        f.asInstanceOf[A => F[B]]
      else
        a => delay(g.runSync(f(a)))

    final def transSyncFn2[G[_], A, B, C](f: (A, B) => G[C])(implicit g: UnsafeSync[G]): (A, B) => F[C] =
      if (this eq g)
        f.asInstanceOf[(A, B) => F[C]]
      else
        (a, b) => delay(g.runSync(f(a, b)))

    final def transSyncFn2C[G[_], A, B, C](f: (A, G[B]) => G[C])(implicit g: UnsafeSync[G]): (A, F[B]) => F[C] =
      if (this eq g)
        f.asInstanceOf[(A, F[B]) => F[C]]
      else
        (a, gb) => delay(g.runSync(f(a, g.transSync(gb)(this))))

    final def transDispatch[G[_]](f: => G[Unit])(implicit g: Dispatch[G]): F[Unit] =
      if (this eq g)
        f.asInstanceOf[F[Unit]]
      else
        delay(g.dispatch(f))

    final def transDispatchFn1[G[_], A](f: A => G[Unit])(implicit g: Dispatch[G]): A => F[Unit] =
      if (this eq g)
        f.asInstanceOf[A => F[Unit]]
      else
        a => delay(g.dispatch(f(a)))

    final def transDispatchFn3[G[_], A, B, C](f: (A, B, C) => G[Unit])(implicit g: Dispatch[G]): (A, B, C) => F[Unit] =
      if (this eq g)
        f.asInstanceOf[(A, B, C) => F[Unit]]
      else
        (a, b, c) => delay(g.dispatch(f(a, b, c)))
  }

  object UnsafeSync {

    trait WithDefaults[F[_]] extends UnsafeSync[F] with Dispatch.WithDefaults[F] {
      override def toJsFn[A](f: => F[A]): js.Function0[A] =
        () => runSync(f)

      override def suspend[A](fa: => F[A]): F[A] =
        flatMap(delay(fa))(identityFn)
    }

    implicit lazy val id: UnsafeSync[Id] = new WithDefaults[Id] {

      override def delay       [A]   (a: => A)                       = a
      override def dispatch    [A]   (a: A)                          = ()
      override def finallyRun  [A, B](a: => A, runFinally: => B)     = try a finally runFinally
      override def flatMap     [A, B](a: A)(f: A => B)               = f(a)
      override def map         [A, B](a: A)(f: A => B)               = f(a)
      override def pure        [A]   (a: A)                          = a
      override def runSync     [A]   (a: => A)                       = a
      override def sequenceList[A]   (as: => List[A])                = as
      override def traverse_   [A, B](as: => Iterable[A])(f: A => B) = as.foreach(f)
      override def traverseList[A, B](as: => List[A])(f: A => B)     = as.map(f)
      override def when_       [A]   (cond: => Boolean)(a: => A)     = if (cond) a

      override def tailrec[A, B](a: A)(f: A => Either[A, B]): B = {
        @tailrec
        def go(a: A): B =
          f(a) match {
            case Left(n)  => go(n)
            case Right(b) => b
          }
        go(a)
      }

      override def handleError[A, AA >: A](a: => A)(f: Throwable => AA): AA =
        try a catch { case t: Throwable => f(t) }

      override def sequence_[A](as: => Iterable[A]): Unit = {
        val i = as.iterator
        while (i.hasNext)
          i.next()
      }
    }
  }

  // ===================================================================================================================

  trait Sync[F[_]] extends UnsafeSync[F] {

    val empty: F[Unit]
    val semigroupSyncOr: Semigroup[F[Boolean]]
    implicit val semigroupSyncUnit: Semigroup[F[Unit]]

    def fromJsFn0[A](f: js.Function0[A]): F[A]
    def isEmpty  [A](f: F[A])           : Boolean
    def reset    [A](fa: F[A])          : F[Unit]
    def runAll   [A](callbacks: F[A]*)  : F[Unit]
  }

  object Sync {
    type Untyped[A] = js.Function0[A]

    trait WithDefaultDispatch[F[_]] extends Sync[F] with Dispatch.WithDefaults[F] {
      override def dispatch[A](fa: F[A]): Unit =
        if (!isEmpty(fa))
          runSync(fa)
    }

    trait WithDefaults[F[_]] extends WithDefaultDispatch[F] with UnsafeSync.WithDefaults[F] {

      override def fromJsFn0[A](f: js.Function0[A]) =
        delay(f())

      override def runAll[A](callbacks: F[A]*): F[Unit] =
        callbacks.foldLeft(empty)((x, y) => chain(x, reset(y)))

      /** Wraps this callback in a `try-finally` block and runs the given callback in the `finally` clause, after the
        * current callback completes, be it in error or success.
        */
      override def finallyRun[A, B](fa: => F[A], runFinally: => F[B]): F[A] =
        delay { try runSync(fa) finally runSync(runFinally) }

      override def reset[A](fa: F[A]): F[Unit] =
        delay(
          try
            runSync(fa): Unit
          catch {
            case t: Throwable =>
              t.printStackTrace()
          }
        )

      override def traverse_[A, B](as: => Iterable[A])(f: A => F[B]): F[Unit] =
        delay {
          for (a <- as)
            runSync(f(a))
        }

      override def sequence_[A](fas: => Iterable[F[A]]): F[Unit] =
        traverse_(fas)(identityFn)

      override def traverseList[A, B](as: => List[A])(f: A => F[B]): F[List[B]] =
        delay {
          val l = List.newBuilder[B]
          for (a <- as)
            l += runSync(f(a))
          l.result()
        }

      override def sequenceList[A](fas: => List[F[A]]): F[List[A]] =
        traverseList(fas)(identityFn)

      implicit val semigroupSyncUnit: Semigroup[F[Unit]] =
        Semigroup((f, g) => flatMap(f)(_ => g))

      val semigroupSyncOr: Semigroup[F[Boolean]] = {
        val True = pure(true)
        Semigroup((x, y) => flatMap(x)(b => if (b) True else y))
      }

      override def when_[A](cond: => Boolean)(fa: => F[A]): F[Unit] =
        delay {
          if (cond)
            runSync(fa)
        }

      override def handleError[A, AA >: A](fa: => F[A])(f: Throwable => F[AA]): F[AA] =
        delay[AA](
          try
            runSync(fa)
          catch {
            case t: Throwable => runSync(f(t))
          }
        )
    }
  }

  // ===================================================================================================================

  trait Async[F[_]] extends Dispatch[F] {
    def async        [A](f: Async.Untyped[A])  : F[A]
    def first        [A](f: Async.Untyped[A])  : F[A]
    def runAsync     [A](fa: => F[A])          : Async.Untyped[A]
    def toJsPromise  [A](fa: => F[A])          : () => js.Promise[A]
    def fromJsPromise[A](pa: => js.Thenable[A]): F[A]

    def async_(onCompletion: Sync.Untyped[Unit] => Sync.Untyped[Unit]): F[Unit]

    final def subst[G[_], X[_[_]]](xg: X[G])(xf: => X[F])(implicit g: Async[G]): X[F] =
      if (this eq g) xg.asInstanceOf[X[F]] else xf

    final def transAsync[G[_], A](ga: => G[A])(implicit g: Async[G]): F[A] =
      if (this eq g)
        ga.asInstanceOf[F[A]]
      else
        async(g.runAsync(ga)(_))
  }

  object Async {
    type Untyped[A] = (Try[A] => Sync.Untyped[Unit]) => Sync.Untyped[Unit]

    trait WithDefaults[F[_]] extends Async[F] with Dispatch.WithDefaults[F] {
      override def first[A](f: Async.Untyped[A]): F[A] =
        async(g => () => {
          var first = true
          f(ea => () => {
            if (first) {
              first = false
              g(ea).apply()
            }
          }).apply()
        })

      override def dispatch[A](fa: F[A]): Unit =
        toJsPromise(fa).apply(): Unit

      override def toJsPromise[A](fa: => F[A]): () => js.Promise[A] =
        JsUtil.asyncToPromise(runAsync(fa))

      override def fromJsPromise[A](pa: => js.Thenable[A]): F[A] =
        async(f => () => JsUtil.runPromiseAsync(pa)(f))
    }
  }

}
