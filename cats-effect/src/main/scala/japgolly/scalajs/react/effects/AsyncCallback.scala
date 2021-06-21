package japgolly.scalajs.react

import japgolly.scalajs.react.internal.Util.catchAll
import java.time.Duration
import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js.timers
import scala.util.{Failure, Success, Try}

object AsyncCallback {

  class State {
    var cancelled = false
    val cancels = collection.mutable.ArrayBuffer.empty[Option[AsyncCallback[Unit]]]

    def onCancel(f: AsyncCallback[Unit]): Int = {
      cancels.addOne(Some(f))
      cancels.length - 1
    }

    def onCancelOption: Option[Callback] =
      cancels.iterator.flatMap(_.toList).toList match {
        case Nil => None
        case xs => Some(xs.reverse.iterator.map(_.toCallback.reset).reduce(_ >> _))
      }
  }

  private[AsyncCallback] val defaultCompleteWith: Try[Any] => Callback =
    _ => Callback.empty

  def apply[A](f: (Try[A] => Callback) => Callback): AsyncCallback[A] =
    new AsyncCallback(_ => f)

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

  def const[A](t: Try[A]): AsyncCallback[A] =
    AsyncCallback(_(t))

  val unit: AsyncCallback[Unit] =
    pure(())

  /** Not literally tail-recursive because AsyncCallback is continuation-based, but this utility in this shape may still
    * be useful.
    */
  def tailrec[A, B](a: A)(f: A => AsyncCallback[Either[A, B]]): AsyncCallback[B] =
    f(a).flatMap {
      case Left(a2) => tailrec(a2)(f)
      case Right(b) => pure(b)
    }

  def omg[A](f: State => (Try[A] => Callback) => Callback): AsyncCallback[A] =
    new AsyncCallback(f)

  def canceled: AsyncCallback[Unit] =
    omg { s =>
      s.cancelled = true
      unit.completeWith(s)
    }
}

// █████████████████████████████████████████████████████████████████████████████████████████████████████████████████████

import AsyncCallback.State

final class AsyncCallback[+A] (val completeWith: State => (Try[A] => Callback) => Callback) { self =>
  var name = ""

  @inline def underlyingRepr = completeWith

  def map[B](f: A => B): AsyncCallback[B] =
    // flatMap(a => AsyncCallback.pure(f(a)))
    flatMap(a => AsyncCallback.delay {
      val b = f(a)
      println(s" ------ $a ==> $b")
      b
    })

  /** Alias for `map`. */
  @inline def |>[B](f: A => B): AsyncCallback[B] =
    map(f)

  def flatMap[B](f: A => AsyncCallback[B]): AsyncCallback[B] = {
    val x =
    AsyncCallback.omg[B] { s => g =>
      Callback.suspend {
        completeWith(s) {
          case Success(a) =>
            catchAll(f(a)) match {
              case Success(next) => val z = next.completeWith(s); Callback.suspend(z(g)).unless_(s.cancelled)
              case Failure(e)    => g(Failure(e))
            }
          case Failure(e) => g(Failure(e))
        }
      }.unless_(s.cancelled)
    }
    x.name = self.name + "+"
    x
  }

  /** Alias for `flatMap`. */
  @inline def >>=[B](g: A => AsyncCallback[B]): AsyncCallback[B] =
    flatMap(g)

  def flatten[B](implicit ev: A => AsyncCallback[B]): AsyncCallback[B] =
    flatMap(ev)

  /** Sequence the argument a callback to run after this, discarding any value produced by this. */
  def >>[B](runNext: AsyncCallback[B]): AsyncCallback[B] =
    flatMap(_ => runNext)

  def handleError[AA >: A](f: Throwable => AsyncCallback[AA]): AsyncCallback[AA] =
    AsyncCallback.omg(s => g => completeWith(s) {
      case r@ Success(_) => g(r)
      case Failure(t)    => f(t).completeWith(s)(g).unless_(s.cancelled)
    }.unless_(s.cancelled))

  def delay(dur: Duration): AsyncCallback[A] =
    delayMs(dur.toMillis.toDouble)

  def delay(dur: FiniteDuration): AsyncCallback[A] =
    delayMs(dur.toMillis.toDouble)

  def delayMs(milliseconds: Double): AsyncCallback[A] =
    if (milliseconds <= 0)
      this
    else
      AsyncCallback.omg(s => f => Callback {
        timers.setTimeout(milliseconds) {
          completeWith(s)(f).runNow()
        }
      })

  def attempt: AsyncCallback[Either[Throwable, A]] =
    AsyncCallback.omg(s => f => completeWith(s)(e => f(Success(e match {
      case Success(a) => Right(a)
      case Failure(t) => Left(t)
    })).unless_(s.cancelled)))

  def toCallback: Callback =
    Callback.suspend {
      val s = new State
      completeWith(s)(AsyncCallback.defaultCompleteWith)
    }

  def onCancel(f: AsyncCallback[Unit]): AsyncCallback[A] =
    AsyncCallback.omg { s =>
      val i = s.onCancel(f)
      self
        .map {a => s.cancels(i) = None; a}
        .completeWith(s)
    }

  def run2(): Option[Callback] = {
    val s = new State
    completeWith(s)(AsyncCallback.defaultCompleteWith).runNow()
    s.onCancelOption
  }
}
