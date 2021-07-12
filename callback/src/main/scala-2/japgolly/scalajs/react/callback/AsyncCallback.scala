package japgolly.scalajs.react.callback

import japgolly.scalajs.react.util.JsUtil
import japgolly.scalajs.react.util.Util.{catchAll, identityFn}
import java.time.Duration
import scala.collection.BuildFrom
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.timers
import scala.util.{Failure, Success, Try}

object AsyncCallback {

  type UnderlyingRepr[+A] = State => (Try[A] => Callback) => Callback

  final class State {
    var cancelled       = false
    val cancelCallbacks = new js.Array[js.UndefOr[UnderlyingRepr[Unit]]]

    def onCancel(f: AsyncCallback[Unit]): Int = {
      cancelCallbacks.addOne(f.underlyingRepr)
      cancelCallbacks.length - 1
    }

    def unCancel(i: Int): Unit =
      cancelCallbacks(i) = ()

    def cancelCallback: Option[Callback] = {
      val it = cancelCallbacks.iterator.filter(_.isDefined)
      Option.when(it.nonEmpty) {
        it.map(u => new AsyncCallback(u.get).toCallback.reset).reduce(_ << _)
      }
    }

    def cancelably(c: => Callback): Callback =
      Callback.suspend(Callback.unless(cancelled)(c))
  }

  def cancel: AsyncCallback[Unit] =
    new AsyncCallback[Unit](s => {
      s.cancelled = true
      unit.underlyingRepr(s)
    })

  private[AsyncCallback] val newState: CallbackTo[State] =
    CallbackTo(new State)

  private[AsyncCallback] val defaultCompleteWith: Try[Any] => Callback =
    _ => Callback.empty

  def apply[A](f: (Try[A] => Callback) => Callback): AsyncCallback[A] =
    new AsyncCallback(_ => f)

  def init[A, B](f: (Try[B] => Callback) => CallbackTo[A]): CallbackTo[(A, AsyncCallback[B])] =
    promise[B].flatMap { case (ac, c) =>
      f(c).map { a =>
        (a, ac)
      }
    }

  /** Create an AsyncCallback and separately provide the completion function.
    *
    * This is like Scala's promise, not the JS promise which is more like Scala's Future.
    */
  def promise[A]: CallbackTo[(AsyncCallback[A], Try[A] => Callback)] =
    for {
      p <- SyncPromise[A]
    } yield (AsyncCallback(p.onComplete), p.complete)

  def first[A](f: (Try[A] => Callback) => Callback): AsyncCallback[A] =
    firstS(_ => f)

  private[react] def firstS[A](f: State => (Try[A] => Callback) => Callback): AsyncCallback[A] =
    new AsyncCallback(s => g => CallbackTo {
      var first = true
      f(s)(ea => Callback.when(first)(Callback{first = false} >> g(ea)))
    }.flatten)

  /** AsyncCallback that never completes. */
  def never[A]: AsyncCallback[A] =
    apply(_ => Callback.empty)

  @deprecated("Use AsyncCallback.delay", "1.7.0")
  def point[A](a: => A): AsyncCallback[A] =
    delay(a)

  def delay[A](a: => A): AsyncCallback[A] =
    AsyncCallback(f => CallbackTo(catchAll(a)).flatMap(f))

  def pure[A](a: A): AsyncCallback[A] =
    const(Success(a))

  def throwException[A](t: => Throwable): AsyncCallback[A] =
    const {
      try
        Failure(t)
      catch {
        case t2: Throwable => Failure(t2)
      }
    }

  def throwExceptionWhenDefined(o: => Option[Throwable]): AsyncCallback[Unit] =
    suspend {
      o match {
        case None    => unit
        case Some(t) => throwException(t)
      }
    }

  def const[A](t: Try[A]): AsyncCallback[A] =
    AsyncCallback(_(t))

  val unit: AsyncCallback[Unit] =
    pure(())

  /** Callback that isn't created until the first time it is used, after which it is reused. */
  def lazily[A](f: => AsyncCallback[A]): AsyncCallback[A] = {
    lazy val g = f
    suspend(g)
  }

  /** Callback that is recreated each time it is used.
    *
    * https://en.wikipedia.org/wiki/Evaluation_strategy#Call_by_name
    */
  def suspend[A](f: => AsyncCallback[A]): AsyncCallback[A] =
    delay(f).flatten

  /** Callback that is recreated each time it is used.
    *
    * https://en.wikipedia.org/wiki/Evaluation_strategy#Call_by_name
    */
  @deprecated("Use AsyncCallback.suspend", "2.0.0")
  def byName[A](f: => AsyncCallback[A]): AsyncCallback[A] =
    suspend(f)

  @deprecated("Use c.asAsyncCallback", "")
  def fromCallback[A](c: CallbackTo[A]): AsyncCallback[A] =
    c.asAsyncCallback

  /** Traverse stdlib T over AsyncCallback.
    * Distribute AsyncCallback over stdlib T.
    */
  def traverse[T[X] <: Iterable[X], A, B](ta: => T[A])(f: A => AsyncCallback[B])(implicit cbf: BuildFrom[T[A], B, T[B]]): AsyncCallback[T[B]] =
    AsyncCallback.suspend {
      val _ta = ta
      val as = _ta.iterator.to(Vector)
      if (as.isEmpty)
        AsyncCallback.pure(cbf.newBuilder(_ta).result())
      else {
        val discard = (_: Any, _: Any) => ()
        val bs = new js.Array[B](as.length)
        as
          .indices
          .iterator
          .map(i => f(as(i)).map(b => bs(i) = b))
          .reduce(_.zipWith(_)(discard))
          .map(_ => cbf.fromSpecific(_ta)(bs))
      }
    }

  /** Sequence stdlib T over AsyncCallback.
    * Co-sequence AsyncCallback over stdlib T.
    */
  def sequence[T[X] <: Iterable[X], A](tca: => T[AsyncCallback[A]])(implicit cbf: BuildFrom[T[AsyncCallback[A]], A, T[A]]): AsyncCallback[T[A]] =
    traverse(tca)(identityFn)(cbf)

  /** Traverse Option over AsyncCallback.
    * Distribute AsyncCallback over Option.
    */
  def traverseOption[A, B](oa: => Option[A])(f: A => AsyncCallback[B]): AsyncCallback[Option[B]] =
    AsyncCallback.delay(oa).flatMap {
      case Some(a) => f(a).map(Some(_))
      case None    => AsyncCallback.pure(None)
    }

  /** Sequence Option over AsyncCallback.
    * Co-sequence AsyncCallback over Option.
    */
  def sequenceOption[A](oca: => Option[AsyncCallback[A]]): AsyncCallback[Option[A]] =
    traverseOption(oca)(identityFn)

  /** Same as [[traverse()]] except avoids combining return values. */
  def traverse_[T[X] <: Iterable[X], A, B](ta: => T[A])(f: A => AsyncCallback[B]): AsyncCallback[Unit] =
    AsyncCallback.suspend {
      val as = new js.Array[A]
      for (a <- ta.iterator)
        as.push(a)

      as.length match {
        case 0 => AsyncCallback.unit
        case 1 => AsyncCallback.suspend(f(as(0))).void
        case n =>
          var error = Option.empty[Throwable]
          val latch = countDownLatch(n).runNow()

          def onTaskComplete(r: Try[B]): Callback =
            Callback {
              r match {
                case Success(_) =>
                case Failure(e) => error = Some(e)
              }
            } >> latch.countDown

          for (a <- as)
            AsyncCallback.suspend(f(a))
              .attemptTry
              .flatMapSync(onTaskComplete)
              .runNow()

          latch.await >> throwExceptionWhenDefined(error)
      }
    }

  /** Same as [[sequence()]] except avoids combining return values. */
  def sequence_[T[X] <: Iterable[X], A](tca: => T[AsyncCallback[A]]): AsyncCallback[Unit] =
    traverse_(tca)(identityFn)

  /** Same as [[traverseOption()]] except avoids combining return values. */
  def traverseOption_[A, B](oa: => Option[A])(f: A => AsyncCallback[B]): AsyncCallback[Unit] =
    AsyncCallback.delay(oa).flatMap {
      case Some(a) => f(a).void
      case None    => AsyncCallback.unit
    }

  /** Same as [[sequenceOption()]] except avoids combining return values. */
  def sequenceOption_[A](oca: => Option[AsyncCallback[A]]): AsyncCallback[Unit] =
    traverseOption_(oca)(identityFn)

  def fromFuture[A](fa: => Future[A])(implicit ec: ExecutionContext): AsyncCallback[A] =
    AsyncCallback(f => Callback {
      val future = fa
      future.value match {
        case Some(value) => f(value).runNow()
        case None => future.onComplete(f(_).runNow())
      }
    })

  def fromCallbackToFuture[A](c: CallbackTo[Future[A]])(implicit ec: ExecutionContext): AsyncCallback[A] =
    c.asAsyncCallback.flatMap(fromFuture(_))

  def fromJsPromise[A](pa: => js.Thenable[A]): AsyncCallback[A] =
    AsyncCallback(f => Callback {
      JsUtil.runPromiseAsync(pa)(f(_).toJsFn)
    })

  def fromCallbackToJsPromise[A](c: CallbackTo[js.Promise[A]]): AsyncCallback[A] =
    c.asAsyncCallback.flatMap(fromJsPromise(_))

  /** Not literally tail-recursive because AsyncCallback is continuation-based, but this utility in this shape may still
    * be useful.
    */
  def tailrec[A, B](a: A)(f: A => AsyncCallback[Either[A, B]]): AsyncCallback[B] =
    f(a).flatMap {
      case Left(a2) => tailrec(a2)(f)
      case Right(b) => pure(b)
    }

  private lazy val tryUnit: Try[Unit] =
    Try(())

  def viaCallback(onCompletion: Callback => Callback): AsyncCallback[Unit] =
    for {
      p <- SyncPromise[Unit].asAsyncCallback
      _ <- onCompletion(p.complete(tryUnit)).asAsyncCallback
      _ <- AsyncCallback(p.onComplete)
    } yield ()

  private[react] def debounce[A](delayMs: Long, self: AsyncCallback[A])(implicit timer: Timer): AsyncCallback[A] =
    if (delayMs <= 0)
      self
    else {
      val f = _debounce[Unit, A](delayMs, _ => self)
      suspend(f(()))
    }

  private def _debounce[A, B](delayMs: Long, f: A => AsyncCallback[B])(implicit timer: Timer): A => AsyncCallback[B] = {
    var prev = Option.empty[timer.Handle]
    var invocationNum = 0

    a => {
      invocationNum += 1
      val (promise, completePromise) = AsyncCallback.promise[B].runNow()

      def run(): Unit = {
        prev = None
        val curInvocationNum = invocationNum
        f(a).tap { b =>
          if (invocationNum == curInvocationNum)
            completePromise(Success(b)).runNow()
        }.toCallback.runNow()
      }

      prev.foreach(timer.cancel)
      prev = Some(timer.delay(delayMs)(run()))

      promise
    }
  }

  def awaitAll(as: AsyncCallback[Any]*): AsyncCallback[Unit] =
    if (as.isEmpty)
      unit
    else
      sequence_(as)

  // ===================================================================================================================

  final case class Forked[+A](await: AsyncCallback[A], isComplete: CallbackTo[Boolean])

  // ===================================================================================================================

  final class Barrier(val await: AsyncCallback[Unit], completePromise: Callback) {

    private var _complete = false

    def complete: Callback =
      completePromise.finallyRun(Callback { _complete = true })

    def isComplete: CallbackTo[Boolean] =
      CallbackTo(_complete)

    @inline
    @deprecated("Use .await", "1.7.7")
    def waitForCompletion: AsyncCallback[Unit] =
      await
  }

  /** A synchronisation aid that allows you to wait for another async process to complete. */
  lazy val barrier: CallbackTo[Barrier] =
    promise[Unit].map { case (promise, complete) =>
      new Barrier(promise, complete(tryUnit))
    }

  // ===================================================================================================================

  final class CountDownLatch(count: Int, barrier: Barrier) {
    private var _pending = count.max(0)

    /** Decrements the count of the latch, releasing all waiting computations if the count reaches zero. */
    val countDown: Callback =
      Callback {
        if (_pending > 0) {
          _pending -= 1
          if (_pending == 0) {
            barrier.complete.runNow()
          }
        }
      }

    @inline def await: AsyncCallback[Unit] =
      barrier.await

    @inline def isComplete: CallbackTo[Boolean] =
      barrier.isComplete

    def pending: CallbackTo[Int] =
      CallbackTo(_pending)
  }

  /** A synchronization aid that allows you to wait until a set of async processes completes. */
  def countDownLatch(count: Int): CallbackTo[CountDownLatch] =
    for {
      b <- barrier
      _ <- b.complete.when_(count <= 0)
    } yield new CountDownLatch(count, b)

  // ===================================================================================================================

  final class Mutex private[AsyncCallback]() {

    private var mutex: Option[Barrier] =
      None

    private val release: Callback =
      CallbackTo {
        val old = mutex
        mutex = None
        old
      }.flatMap(Callback.traverseOption(_)(_.complete))

    /** Wrap a [[AsyncCallback]] so that it executes in the mutex.
      *
      * Note: THIS IS NOT RE-ENTRANT. Calling this from within the mutex will block.
      */
    def apply[A](ac: AsyncCallback[A]): AsyncCallback[A] =
      suspend {

        mutex match {
          case None =>
            // Mutex empty
            val b = barrier.runNow()
            mutex = Some(b)
            ac.finallyRunSync(release)

          case Some(b) =>
            // Mutex in use
            b.await >> apply(ac)
        }
      }
  }

  /** Creates a new (non-reentrant) mutex. */
  def mutex: CallbackTo[Mutex] =
    CallbackTo(new Mutex)

  // ===================================================================================================================

  final class ReadWriteMutex private[AsyncCallback]() {

    // Whether it's a read or write mutex is determined by readers being > 0 or not
    private var mutex: Option[AsyncCallback.Barrier] =
      None

    private var readers =
      0

    private val releaseMutex: Callback =
      CallbackTo {
        if (readers == 0) {
          val old = mutex
          mutex = None
          old
        } else
          None
      }.flatMap(Callback.traverseOption(_)(_.complete))

    private val releaseReader: Callback =
      CallbackTo {
        readers -= 1
      } >> releaseMutex

    /** Wrap a [[AsyncCallback]] so that it executes in the write-mutex.
      * There can only be one writer active at one time.
      *
      * Note: THIS IS NOT RE-ENTRANT. Calling this from within the read or write mutex will block.
      */
    def write[A](ac: AsyncCallback[A]): AsyncCallback[A] =
      AsyncCallback.suspend {

        mutex match {
          case None =>
            // Mutex empty
            val b = AsyncCallback.barrier.runNow()
            mutex = Some(b)
            ac.finallyRunSync(releaseMutex)

          case Some(b) =>
            // Mutex in use
            b.await >> write(ac)
        }
      }

    /** Wrap a [[AsyncCallback]] so that it executes in the read-mutex.
      * There can be many readers active at one time.
      *
      * Note: Calling this from within the write-mutex will block.
      */
    def read[A](ac: AsyncCallback[A]): AsyncCallback[A] =
      AsyncCallback.suspend {

        mutex match {
          case None =>
            // Mutex empty
            val b = AsyncCallback.barrier.runNow()
            mutex = Some(b)
            assert(readers == 0)
            readers = 1
            ac.finallyRunSync(releaseReader)

          case Some(b) =>
            if (readers > 0) {
              // Read-mutex in use
              readers += 1
              ac.finallyRunSync(releaseReader)

            } else {
              // Write-mutex in use
              b.await >> read(ac)
            }
        }
      }
  }

  /** Creates a new (non-reentrant) read/write mutex. */
  def readWriteMutex: CallbackTo[ReadWriteMutex] =
    CallbackTo(new ReadWriteMutex)

  // ===================================================================================================================

  final class Ref[A] private[AsyncCallback](atomicReads: Boolean, atomicWrites: Boolean) {

    private val mutex       = readWriteMutex.runNow()
    private val initialised = barrier.runNow()
    private var _value: A   = _

    private val markInitialised =
      initialised.complete.asAsyncCallback

    /** If this hasn't been set yet, it will block until it is set. */
    val get: AsyncCallback[A] = {
      var readValue: AsyncCallback[A] =
        AsyncCallback.delay(_value)

      if (atomicReads)
        readValue = mutex.read(readValue)

      initialised.await >> readValue
    }

    /** Synchronously return whatever value is currently stored. (Ignores atomicity). */
    lazy val getIfAvailable: CallbackTo[Option[A]] =
      initialised.isComplete.map {
        case true  => Some(_value)
        case false => None
      }

    private def inWriteMutex[B](a: AsyncCallback[B]): AsyncCallback[B] =
      if (atomicWrites)
        mutex.write(a)
      else
        a

    // This must only be called within the write mutex
    private def setWithinMutex(c: AsyncCallback[A]): AsyncCallback[Unit] =
      for {
        a <- c
        _ <- AsyncCallback.delay { _value = a }
        _ <- markInitialised
      } yield ()

    def set(a: => A): AsyncCallback[Unit] =
      setAsync(AsyncCallback.delay(a))

    def setSync(c: CallbackTo[A]): AsyncCallback[Unit] =
      setAsync(c.asAsyncCallback)

    def setAsync(c: AsyncCallback[A]): AsyncCallback[Unit] =
      inWriteMutex(setWithinMutex(c))

    /** Returns whether or not the value was set. */
    def setIfUnset(a: => A): AsyncCallback[Boolean] =
      setIfUnsetAsync(AsyncCallback.delay(a))

    /** Returns whether or not the value was set. */
    def setIfUnsetSync(c: CallbackTo[A]): AsyncCallback[Boolean] =
      setIfUnsetAsync(c.asAsyncCallback)

    /** Returns whether or not the value was set. */
    def setIfUnsetAsync(c: AsyncCallback[A]): AsyncCallback[Boolean] =
      initialised.isComplete.asAsyncCallback.flatMap {
        case true => AsyncCallback.pure(false)
        case false =>
          inWriteMutex {
            AsyncCallback.suspend {
              if (initialised.isComplete.runNow())
                AsyncCallback.pure(false)
              else
                setWithinMutex(c).ret(true)
            }
          }
      }
  }

  @inline def ref[A]: CallbackTo[Ref[A]] =
    ref()

  def ref[A](allowStaleReads: Boolean = false,
             atomicWrites   : Boolean = true): CallbackTo[Ref[A]] =
    CallbackTo(new Ref(
      atomicReads   = atomicWrites && !allowStaleReads,
      atomicWrites  = atomicWrites,
    ))
}

// █████████████████████████████████████████████████████████████████████████████████████████████████████████████████████

/** Pure asynchronous callback.
  *
  * You can think of this as being similar to using `Future` - you can use it in for-comprehensions the same way -
  * except `AsyncCallback` is pure and doesn't need an `ExecutionContext`.
  *
  * When combining instances, it's good to know which methods are sequential and which are parallel
  * (or at least concurrent).
  *
  * The following methods are sequential:
  * - [[>>=()]] / [[flatMap()]]
  * - [[>>()]] & [[<<()]]
  * - [[flatTap()]]
  *
  * The following methods are effectively parallel:
  * - [[*>()]] & [[<*()]]
  * - [[race()]]
  * - [[zip()]] & [[zipWith()]]
  * - `AsyncCallback.traverse` et al
  *
  * In order to actually run this, or get it into a shape in which in can be run, use one of the following:
  * - [[toCallback]] <-- most common
  * - [[asCallbackToFuture]]
  * - [[asCallbackToJsPromise]]
  * - [[unsafeToFuture()]]
  * - [[unsafeToJsPromise()]]
  *
  * A good example is the [Ajax 2 demo](https://japgolly.github.io/scalajs-react/#examples/ajax-2).
  *
  * @tparam A The type of data asynchronously produced on success.
  */
final class AsyncCallback[+A] private[AsyncCallback] (val underlyingRepr: AsyncCallback.UnderlyingRepr[A]) extends AnyVal { self =>

  def completeWith(f: Try[A] => Callback): Callback =
    AsyncCallback.newState.flatMap(underlyingRepr(_)(f))

  def map[B](f: A => B): AsyncCallback[B] =
    flatMap(a => AsyncCallback.pure(f(a)))

  /** Alias for `map`. */
  @inline def |>[B](f: A => B): AsyncCallback[B] =
    map(f)

  def flatMap[B](f: A => AsyncCallback[B]): AsyncCallback[B] =
    new AsyncCallback(s => g =>
      s.cancelably {
        underlyingRepr(s) {
          case Success(a) =>
            catchAll(f(a)) match {
              case Success(next) => s.cancelably(next.underlyingRepr(s)(g))
              case Failure(e)    => g(Failure(e))
            }
          case Failure(e) => g(Failure(e))
        }
      }
    )

  /** Alias for `flatMap`. */
  @inline def >>=[B](g: A => AsyncCallback[B]): AsyncCallback[B] =
    flatMap(g)

  def flatten[B](implicit ev: A => AsyncCallback[B]): AsyncCallback[B] =
    flatMap(ev)

  /** Sequence the argument a callback to run after this, discarding any value produced by this. */
  def >>[B](runNext: AsyncCallback[B]): AsyncCallback[B] =
    flatMap(_ => runNext)

  /** Sequence a callback to run before this, discarding any value produced by it. */
  @inline def <<[B](runBefore: AsyncCallback[B]): AsyncCallback[A] =
    runBefore >> this

  /** Convenient version of `<<` that accepts an Option */
  def <<?[B](prev: Option[AsyncCallback[B]]): AsyncCallback[A] =
    prev.fold(this)(_ >> this)

  /** When the callback result becomes available, perform a given side-effect with it. */
  def tap(t: A => Any): AsyncCallback[A] =
    flatTap(a => AsyncCallback.delay(t(a)))

  /** Alias for `tap`. */
  @inline def <|(t: A => Any): AsyncCallback[A] =
    tap(t)

  def flatTap[B](t: A => AsyncCallback[B]): AsyncCallback[A] =
    for {
      a <- this
      _ <- t(a)
    } yield a

  def zip[B](that: AsyncCallback[B]): AsyncCallback[(A, B)] =
    zipWith(that)((_, _))

  def zipWith[B, C](that: AsyncCallback[B])(f: (A, B) => C): AsyncCallback[C] =
    new AsyncCallback(s => cc => s.cancelably(CallbackTo {
      var ra: Option[Try[A]] = None
      var rb: Option[Try[B]] = None
      var r: Option[Try[C]] = None

      val respond = Callback {
        if (r.isEmpty) {
          (ra, rb) match {
            case (Some(Success(a)), Some(Success(b))) => r = Some(catchAll(f(a, b)))
            case (Some(Failure(e)), _               ) => r = Some(Failure(e))
            case (_               , Some(Failure(e))) => r = Some(Failure(e))
            case (Some(Success(_)), None            )
               | (None            , Some(Success(_)))
               | (None            , None            ) => ()
          }
          r.foreach(cc(_).runNow())
        }
      }

      this.underlyingRepr(s)(e => Callback {ra = Some(e)} >> respond) >>
      that.underlyingRepr(s)(e => Callback {rb = Some(e)} >> respond)
    }.flatten))

  /** Start both this and the given callback at once and when both have completed successfully,
    * discard the value produced by this.
    */
  def *>[B](next: AsyncCallback[B]): AsyncCallback[B] =
    zipWith(next)((_, b) => b)

  /** Start both this and the given callback at once and when both have completed successfully,
    * discard the value produced by the given callback.
    */
  def <*[B](next: AsyncCallback[B]): AsyncCallback[A] =
    zipWith(next)((a, _) => a)

  /** Discard the callback's return value, return a given value instead.
    *
    * `ret`, short for `return`.
    */
  def ret[B](b: B): AsyncCallback[B] =
    map(_ => b)

  /** Discard the value produced by this callback. */
  def void: AsyncCallback[Unit] =
    map(_ => ())

  /** Discard the value produced by this callback.
    *
    * This method allows you to be explicit about the type you're discarding (which may change in future).
    */
  @inline def voidExplicit[B](implicit ev: A <:< B): AsyncCallback[Unit] =
    void

  /** Wraps this callback in a try-catch and returns either the result or the exception if one occurs. */
  def attempt: AsyncCallback[Either[Throwable, A]] =
    new AsyncCallback(s => f => underlyingRepr(s)(e => f(Success(e match {
      case Success(a) => Right(a)
      case Failure(t) => Left(t)
    }))))

  def attemptTry: AsyncCallback[Try[A]] =
    new AsyncCallback(s => f => underlyingRepr(s)(e => f(Success(e))))

  /** If this completes successfully, discard the result.
    * If any exception occurs, call `printStackTrace` and continue.
    *
    * @since 2.0.0
    */
  def reset: AsyncCallback[Unit] =
    new AsyncCallback(s => f => underlyingRepr(s)(e => f(Success(e match {
      case _: Success[A] => ()
      case Failure(t)    => t.printStackTrace()
    }))))

  def handleError[AA >: A](f: Throwable => AsyncCallback[AA]): AsyncCallback[AA] =
    new AsyncCallback(s => g => underlyingRepr(s) {
      case r@ Success(_) => g(r)
      case Failure(t)    => f(t).underlyingRepr(s)(g)
    })

  def maybeHandleError[AA >: A](f: PartialFunction[Throwable, AsyncCallback[AA]]): AsyncCallback[AA] =
    new AsyncCallback(s => g => underlyingRepr(s) {
      case r@ Success(_) => g(r)
      case l@ Failure(t) => f.lift(t) match {
        case Some(n) => n.underlyingRepr(s)(g)
        case None    => g(l)
      }
    })

  /** Return a version of this callback that will only execute once, and reuse the result for all
    * other invocations.
    */
  def memo(): AsyncCallback[A] = {
    var result: Option[AsyncCallback[A]] = None
    def set(r: AsyncCallback[A]) = {result = Some(r); r}
    AsyncCallback.suspend {
      result.getOrElse {
        val first = attemptTry.flatMap(t => AsyncCallback.suspend(set(AsyncCallback.const(t))))
        val promise = first.unsafeToJsPromise()
        result getOrElse set(AsyncCallback.fromJsPromise(promise))
      }
    }
  }

  /** Conditional execution of this callback.
    *
    * @param cond The condition required to be `true` for this callback to execute.
    * @return `Some` result of the callback executed, else `None`.
    */
  def when(cond: => Boolean): AsyncCallback[Option[A]] =
    new AsyncCallback(s => f => if (cond) underlyingRepr(s)(ea => f(ea.map(Some(_)))) else f(Success(None)))

  /** Conditional execution of this callback.
    * Reverse of [[when()]].
    *
    * @param cond The condition required to be `false` for this callback to execute.
    * @return `Some` result of the callback executed, else `None`.
    */
  def unless(cond: => Boolean): AsyncCallback[Option[A]] =
    when(!cond)

  /** Conditional execution of this callback.
    * Discards the result.
    *
    * @param cond The condition required to be `true` for this callback to execute.
    */
  def when_(cond: => Boolean): AsyncCallback[Unit] =
    when(cond).void

  /** Conditional execution of this callback.
    * Discards the result.
    * Reverse of [[when_()]].
    *
    * @param cond The condition required to be `false` for the callback to execute.
    */
  def unless_(cond: => Boolean): AsyncCallback[Unit] =
    when_(!cond)

  /** Limits the number of invocations in a given amount of time.
    *
    * @return Some if invocation was allowed, None if rejected/rate-limited
    */
  def rateLimit(window: Duration): AsyncCallback[Option[A]] =
    rateLimitMs(window.toMillis)

  /** Limits the number of invocations in a given amount of time.
    *
    * @return Some if invocation was allowed, None if rejected/rate-limited
    */
  def rateLimit(window: FiniteDuration): AsyncCallback[Option[A]] =
    rateLimitMs(window.toMillis)

  /** Limits the number of invocations in a given amount of time.
    *
    * @return Some if invocation was allowed, None if rejected/rate-limited
    */
  def rateLimit(window: Duration, maxPerWindow: Int): AsyncCallback[Option[A]] =
    rateLimitMs(window.toMillis, maxPerWindow)

  /** Limits the number of invocations in a given amount of time.
    *
    * @return Some if invocation was allowed, None if rejected/rate-limited
    */
  def rateLimit(window: FiniteDuration, maxPerWindow: Int): AsyncCallback[Option[A]] =
    rateLimitMs(window.toMillis, maxPerWindow)

  /** Limits the number of invocations in a given amount of time.
    *
    * @return Some if invocation was allowed, None if rejected/rate-limited
    */
  def rateLimitMs(windowMs: Long, maxPerWindow: Int = 1): AsyncCallback[Option[A]] =
    _rateLimitMs(windowMs, maxPerWindow, RateLimit.realClock)

  private[react] def _rateLimitMs(windowMs: Long, maxPerWindow: Int, clock: RateLimit.Clock): AsyncCallback[Option[A]] =
    if (windowMs <= 0 || maxPerWindow <= 0)
      AsyncCallback.pure(None)
    else {
      val limited =
        RateLimit.fn(
          run          = (f: Try[A] => Callback) => AsyncCallback.newState.flatMap(underlyingRepr(_)(f)),
          windowMs     = windowMs,
          maxPerWindow = maxPerWindow,
          clock        = clock,
        )
      val miss = Try(None)
      AsyncCallback { f =>
        Callback {
          limited(ta => f(ta.map(Some(_)))) match {
            case Some(cb) => cb.runNow()
            case None     => f(miss)
          }
        }
      }
    }

  def debounce(delay: Duration): AsyncCallback[A] =
    debounceMs(delay.toMillis)

  def debounce(delay: FiniteDuration): AsyncCallback[A] =
    debounceMs(delay.toMillis)

  def debounceMs(delayMs: Long): AsyncCallback[A] =
    AsyncCallback.debounce(delayMs, this)

  /** Log to the console before this callback starts, and after it completes.
    *
    * Does not change the result.
    */
  def logAround(message: Any, optionalParams: Any*): AsyncCallback[A] = {
    def log(prefix: String) = Callback.log(prefix + message, optionalParams: _*).asAsyncCallback
    log("→  Starting: ") *> this <* log(" ← Finished: ")
  }

  /** Logs the result of this callback as it completes. */
  def logResult(msg: A => String): AsyncCallback[A] =
    flatTap(a => Callback.log(msg(a)).asAsyncCallback)

  /** Logs the result of this callback as it completes.
    *
    * @param name Prefix to appear the log output.
    */
  def logResult(name: String): AsyncCallback[A] =
    logResult(a => s"$name: $a")

  /** Logs the result of this callback as it completes. */
  def logResult: AsyncCallback[A] =
    logResult(_.toString)

  def delay(dur: Duration): AsyncCallback[A] =
    delayMs(dur.toMillis.toDouble)

  def delay(dur: FiniteDuration): AsyncCallback[A] =
    delayMs(dur.toMillis.toDouble)

  def delayMs(milliseconds: Double): AsyncCallback[A] =
    if (milliseconds <= 0)
      this
    else
      new AsyncCallback(s => f => Callback {
        timers.setTimeout(milliseconds) {
          underlyingRepr(s)(f).runNow()
        }
      })

  /** Limit the amount of time you're prepared to wait for a computation.
    *
    * Note: there's no built-in concept of cancellation here.
    * If your procedure doesn't finish in time, this will return `None` when the time limit is reached however, the
    * underlying procedure will become orphaned and continue to run in the background until complete.
    */
  def timeout(limit: Duration): AsyncCallback[Option[A]] =
    timeoutMs(limit.toMillis.toDouble)

  /** Limit the amount of time you're prepared to wait for a computation.
    *
    * Note: there's no built-in concept of cancellation here.
    * If your procedure doesn't finish in time, this will return `None` when the time limit is reached however, the
    * underlying procedure will become orphaned and continue to run in the background until complete.
    */
  def timeout(limit: FiniteDuration): AsyncCallback[Option[A]] =
    timeoutMs(limit.toMillis.toDouble)

  /** Limit the amount of time you're prepared to wait for a computation.
    *
    * Note: there's no built-in concept of cancellation here.
    * If your procedure doesn't finish in time, this will return `None` when the time limit is reached however, the
    * underlying procedure will become orphaned and continue to run in the background until complete.
    */
  def timeoutMs(milliseconds: Double): AsyncCallback[Option[A]] =
    AsyncCallback.unit.delayMs(milliseconds).race(this).map(_.toOption)

  /** Schedule for repeated execution every `dur`. */
  @inline def setInterval(dur: Duration): CallbackTo[Callback.SetIntervalResult] =
    toCallback.setInterval(dur)

  /** Schedule for repeated execution every `dur`. */
  @inline def setInterval(dur: FiniteDuration): CallbackTo[Callback.SetIntervalResult] =
    toCallback.setInterval(dur)

  /** Schedule for repeated execution every x milliseconds. */
  @inline def setIntervalMs(milliseconds: Double): CallbackTo[Callback.SetIntervalResult] =
    toCallback.setIntervalMs(milliseconds)

  /** Schedule for execution after `dur`.
    *
    * Note: it most cases [[delay()]] is a better alternative.
    */
  @inline def setTimeout(dur: Duration): CallbackTo[Callback.SetTimeoutResult] =
    toCallback.setTimeout(dur)

  /** Schedule for execution after `dur`.
    *
    * Note: it most cases [[delay()]] is a better alternative.
    */
  @inline def setTimeout(dur: FiniteDuration): CallbackTo[Callback.SetTimeoutResult] =
    toCallback.setTimeout(dur)

  /** Schedule for execution after x milliseconds.
    *
    * Note: it most cases [[delayMs()]] is a better alternative.
    */
  @inline def setTimeoutMs(milliseconds: Double): CallbackTo[Callback.SetTimeoutResult] =
    toCallback.setTimeoutMs(milliseconds)

  /** Wraps this callback in a `try-finally` block and runs the given callback in the `finally` clause, after the
    * current callback completes, be it in error or success.
    */
  def finallyRun[B](runFinally: AsyncCallback[B]): AsyncCallback[A] =
    attempt.flatMap {
      case Right(a) => runFinally.ret(a)
      case Left(e)  => runFinally.attempt >> AsyncCallback.throwException(e)
    }

  /** Function distribution. See `AsyncCallback.liftTraverse(f).id` for the dual. */
  def distFn[B, C](implicit ev: AsyncCallback[A] <:< AsyncCallback[B => C]): B => AsyncCallback[C] = {
    val bc = ev(this)
    b => bc.map(_(b))
  }

  /** Start both this and the given callback at once use the first result to become available,
    * regardless of whether it's a success or failure.
    */
  def race[B](that: AsyncCallback[B]): AsyncCallback[Either[A, B]] =
    AsyncCallback.firstS(s => f =>
      this.underlyingRepr(s)(e => f(e.map(Left(_)))) >>
      that.underlyingRepr(s)(e => f(e.map(Right(_)))))

  def toCallback: Callback =
    AsyncCallback.newState.flatMap(underlyingRepr(_)(AsyncCallback.defaultCompleteWith))

  def asCallbackToFuture: CallbackTo[Future[A]] =
    AsyncCallback.newState.flatMap(s => CallbackTo {
      val p = scala.concurrent.Promise[A]()
      underlyingRepr(s)(t => Callback(p.tryComplete(t))).runNow()
      p.future
    })

  def asCallbackToJsPromise: CallbackTo[js.Promise[A]] =
    AsyncCallback.newState.flatMap(s =>
      CallbackTo.newJsPromise[A].flatMap { case (p, pc) =>
        underlyingRepr(s)(pc).map { _ =>
          p
        }
      }
    )

  def unsafeToFuture(): Future[A] =
    asCallbackToFuture.runNow()

  def unsafeToJsPromise(): js.Promise[A] =
    asCallbackToJsPromise.runNow()

  /** Returns a synchronous [[Callback]] that when run, returns the result on the [[Right]] if this [[AsyncCallback]]
    * is actually synchronous, else returns a new [[AsyncCallback]] on the [[Left]] that waits for the async computation
    * to complete.
    */
  def sync: CallbackTo[Either[AsyncCallback[A], A]] =
    CallbackTo {
      var result = Option.empty[A]
      val promise = tap(a => result = Some(a)).asCallbackToJsPromise.runNow()
      result match {
        case Some(a) => Right(a)
        case None    => Left(AsyncCallback.fromJsPromise(promise))
      }
    }

  def runNow(): Unit =
    toCallback.runNow()

  /** THIS IS VERY CONVENIENT IN UNIT TESTS BUT DO NOT RUN THIS IN PRODUCTION CODE.
    *
    * Executes this now, expecting and returning a synchronous result.
    * If there are any asynchronous computations this will throw an exception.
    */
  def unsafeRunNowSync(): A =
    sync.runNow() match {
      case Right(a) => a
      case Left(_)  => throw new RuntimeException(
        "AsyncCallback#unsafeRunNowSync() failed! The AsyncCallback contains at least one asynchronous computation.")
    }

  def flatMapSync[B](f: A => CallbackTo[B]): AsyncCallback[B] =
    flatMap(f(_).asAsyncCallback)

  def flattenSync[B](implicit ev: A => CallbackTo[B]): AsyncCallback[B] =
    flatten(ev(_).asAsyncCallback)

  def flatTapSync[B](t: A => CallbackTo[B]): AsyncCallback[A] =
    flatTap(t(_).asAsyncCallback)

  def handleErrorSync[AA >: A](f: Throwable => CallbackTo[AA]): AsyncCallback[AA] =
    handleError(f(_).asAsyncCallback)

  def maybeHandleErrorSync[AA >: A](f: PartialFunction[Throwable, CallbackTo[AA]]): AsyncCallback[AA] =
    maybeHandleError(f.andThen(_.asAsyncCallback))

  /** Wraps this callback in a `try-finally` block and runs the given callback in the `finally` clause, after the
    * current callback completes, be it in error or success.
    */
  @inline def finallyRunSync[B](runFinally: CallbackTo[B]): AsyncCallback[A] =
    finallyRun(runFinally.asAsyncCallback)

  /** Runs this async computation in the background.
    *
    * Returns the ability for you to await/join the forked computation.
    */
  def fork: CallbackTo[AsyncCallback.Forked[A]] =
    AsyncCallback.promise[A].flatMap { case (promise, completePromise) =>
      var _complete    = false
      val isComplete   = CallbackTo(_complete)
      val markComplete = CallbackTo { _complete = true }
      val runInBg      = self.attemptTry.finallyRunSync(markComplete).flatMapSync(completePromise).fork_
      val forked       = AsyncCallback.Forked(promise, isComplete)
      runInBg.ret(forked)
    }

  /** Runs this async computation in the background.
    *
    * Unlike [[fork]] this returns nothing, meaning this is like fire-and-forget.
    */
  def fork_ : Callback =
    delayMs(1).toCallback

  /** Record the duration of this callback's execution. */
  def withDuration[B](f: (A, FiniteDuration) => AsyncCallback[B]): AsyncCallback[B] = {
    val nowMS: AsyncCallback[Long] = CallbackTo.currentTimeMillis.asAsyncCallback
    for {
      s <- nowMS
      a <- self
      e <- nowMS
      b <- f(a, FiniteDuration(e - s, MILLISECONDS))
    } yield b
  }

  /** Log the duration of this callback's execution. */
  def logDuration(fmt: FiniteDuration => String): AsyncCallback[A] =
    withDuration((a, d) =>
      Callback.log(fmt(d)).asAsyncCallback ret a)

  /** Log the duration of this callback's execution.
    *
    * @param name Prefix to appear the log output.
    */
  def logDuration(name: String): AsyncCallback[A] =
    logDuration(d => s"$name completed in $d.")

  /** Log the duration of this callback's execution. */
  def logDuration: AsyncCallback[A] =
    logDuration("AsyncCallback")

  def withFilter(f: A => Boolean): AsyncCallback[A] =
    map[A](a => if (f(a)) a else
      // This is what scala.Future does
      throw new NoSuchElementException("AsyncCallback.withFilter predicate is not satisfied"))

  def onCancel(f: AsyncCallback[Unit]): AsyncCallback[A] =
    new AsyncCallback(s => {
      val i = s.onCancel(f)
      self
        .map { a => s.unCancel(i); a }
        .underlyingRepr(s)
    })
}
