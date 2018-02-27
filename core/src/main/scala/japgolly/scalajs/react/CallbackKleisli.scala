package japgolly.scalajs.react

import scala.scalajs.js
import scala.util.Try

object CallbackKleisli {

  def const[A, B](result: CallbackTo[B]): CallbackKleisli[A, B] =
    apply(_ => result)

  def constValue[A, B](result: B): CallbackKleisli[A, B] =
    const(CallbackTo pure result)

  def unit[A]: CallbackKleisli[A, Unit] =
    const(Callback.empty)

  def lift[A, B](f: A => B): CallbackKleisli[A, B] =
    apply(a => CallbackTo(f(a)))

  def dist[A, B](f: CallbackTo[A => B]): CallbackKleisli[A, B] =
    apply(a => f.map(_ apply a))

  def ask[A]: CallbackKleisli[A, A] =
    apply(CallbackTo.pure)

  def choice[A, B, C](fst: CallbackKleisli[A, C], snd: CallbackKleisli[B, C]): CallbackKleisli[Either[A, B], C] =
    CallbackKleisli(_.fold(fst.run, snd.run))

  def split[A, B, C, D](fst: CallbackKleisli[A, B], snd: CallbackKleisli[C, D]): CallbackKleisli[(A, C), (B, D)] =
    CallbackKleisli(x => fst.run(x._1) zip snd.run(x._2))
}

// =====================================================================================================================

/** `A => CallbackTo[B]` aka `Kleisli[CallbackTo, A, B]` aka `ReaderT[A, CallbackTo, B]`.
  *
  * Never heard of Kleisli? Basically, a "Kleisli triple" is a function with the shape `A => M[B]`.
  * In this case, the `M` is hard-coded to `CallbackTo`.
  *
  * Hardcoded to `CallbackTo` for the same reasons as [[CallbackOption]] and for the same reasons that [[CallbackTo]]
  * exists.
  *
  * @since 1.2.0
  */
final case class CallbackKleisli[A, B](run: A => CallbackTo[B]) extends AnyVal {

  @inline def apply(a: A): CallbackTo[B] =
    run(a)

  def widen[C >: B]: CallbackKleisli[A, C] =
    CallbackKleisli(run(_).widen[C])

  def narrow[C <: A]: CallbackKleisli[C, B] =
    CallbackKleisli(run)

  def contramap[C](f: C => A): CallbackKleisli[C, B] =
    CallbackKleisli(run compose f)

  def mapCB[C](f: CallbackTo[B] => CallbackTo[C]): CallbackKleisli[A, C] =
    CallbackKleisli(f compose run)

  def map[C](f: B => C): CallbackKleisli[A, C] =
    mapCB(_ map f)

  /** Alias for `map`. */
  @inline def |>[C](f: B => C): CallbackKleisli[A, C] =
    map(f)

  def flatMap[C](f: B => CallbackKleisli[A, C]): CallbackKleisli[A, C] =
    CallbackKleisli(a => run(a).flatMap(f(_).run(a)))

  /** Alias for `flatMap`. */
  @inline def >>=[C](f: B => CallbackKleisli[A, C]): CallbackKleisli[A, C] =
    flatMap(f)

  /** Sequence a [[CallbackKleisli]] to run after this, discarding any value produced by this. */
  def >>[C](next: CallbackKleisli[A, C]): CallbackKleisli[A, C] =
    CallbackKleisli(a => run(a) >> next.run(a))

  /** Alias for `>>`.
    *
    * Where `>>` is often associated with Monads, `*>` is often associated with Applicatives.
    */
  @inline def *>[C](next: CallbackKleisli[A, C]): CallbackKleisli[A, C] =
    >>(next)

  /** Sequence a [[CallbackKleisli]] to run before this, discarding any value produced by it. */
  @inline def <<[C](prev: CallbackKleisli[A, C]): CallbackKleisli[A, B] =
    prev >> this

  def zip[C](cb: CallbackKleisli[A, C]): CallbackKleisli[A, (B, C)] =
    for {
      b <- this
      c <- cb
    } yield (b, c)

  /** Discard the callback's return value, return a given value instead.
    *
    * `ret`, short for `return`.
    */
  def ret[C](c: C): CallbackKleisli[A, C] =
    map(_ => c)

  /** Discard the value produced by this callback. */
  def void: CallbackKleisli[A, Unit] =
    map(_ => ())

  /** Discard the value produced by this callback.
    *
    * This method allows you to be explicit about the type you're discarding (which may change in future).
    */
  @inline def voidExplicit[C](implicit ev: B <:< C): CallbackKleisli[A, Unit] =
    void

  def when(test: A => Boolean): CallbackKleisli[A, Option[B]] =
    CallbackKleisli(a => if (test(a)) run(a).map(Some(_)) else CallbackTo.pure(None))

  @inline def unless(test: A => Boolean): CallbackKleisli[A, Option[B]] =
    when(a => !test(a))

  def when_(test: A => Boolean): CallbackKleisli[A, Unit] =
    CallbackKleisli(a => if (test(a)) run(a).void else Callback.empty)

  @inline def unless_(test: A => Boolean): CallbackKleisli[A, Unit] =
    when_(a => !test(a))

  /**
    * Wraps this callback in a try-catch and returns either the result or the exception if one occurs.
    */
  def attempt: CallbackKleisli[A, Either[Throwable, B]] =
    mapCB(_.attempt)

  /**
    * Wraps this callback in a scala `Try` with catches what it considers non-fatal errors.
    *
    * Use [[attempt]] to catch everything.
    */
  def attemptTry: CallbackKleisli[A, Try[B]] =
    mapCB(_.attemptTry)

  /**
    * When the callback result becomes available, perform a given side-effect with it.
    */
  def tap(t: B => Any): CallbackKleisli[A, B] =
    flatTap(b => CallbackKleisli.constValue(t(b)))

  /** Alias for `tap`. */
  @inline def <|(t: B => Any): CallbackKleisli[A, B] =
    tap(t)

  def flatTap[C](t: B => CallbackKleisli[A, C]): CallbackKleisli[A, B] =
    for {
      b <- this
      _ <- t(b)
    } yield b

  /** Sequence actions, discarding the value of the second argument. */
  def <*[C](next: CallbackKleisli[A, C]): CallbackKleisli[A, B] =
    flatTap(_ => next)

  def toJsFn: js.Function1[A, B] =
    run(_).runNow()

  def =<<(a: CallbackTo[A]): CallbackTo[B] =
    a >>= run

  def toCallback(a: CallbackTo[A]): CallbackTo[B] =
    a.flatMap(run)

  def asCallback: CallbackTo[A => B] =
    CallbackTo.liftTraverse(run).id

  def >=>[C](next: CallbackKleisli[B, C]): CallbackKleisli[A, C] =
    CallbackKleisli(run(_).flatMap(next.run))

  @inline def <=<[C](prev: CallbackKleisli[C, A]): CallbackKleisli[C, B] =
    prev >=> this

  @inline def compose[C](prev: CallbackKleisli[C, A]): CallbackKleisli[C, B] =
    prev >=> this

  @inline def andThen[C](next: CallbackKleisli[B, C]): CallbackKleisli[A, C] =
    this >=> next
}
