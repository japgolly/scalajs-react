package japgolly.scalajs.react.internal

import japgolly.scalajs.react.{Callback, CallbackTo}
import scala.collection.compat._
import scala.collection.mutable.Builder
import scala.scalajs.js
import scala.scalajs.js.{Thenable, |}
import scala.util.{Failure, Success, Try}

object Util {

  def catchAll[A](a: => A): Try[A] =
    try Success(a)
    catch {case t: Throwable => Failure(t) }

  def intercalateTo[F[_], A](as: Iterator[A], sep: A)(implicit cbf: Factory[A, F[A]]): F[A] = {
    val b = cbf.newBuilder
    intercalateInto(b, as, sep)
    b.result()
  }

  def intercalateInto[A](b: Builder[A, _], it: Iterator[A], sep: A): Unit = {
    if (it.hasNext) {
      val first = it.next()
      b += first
      for (a <- it) {
        b += sep
        b += a
      }
    }
  }

  def newJsPromise[A]: CallbackTo[(js.Promise[A], Try[A] => Callback)] = CallbackTo {
    var complete: Try[A] => Callback = null
    val p = new js.Promise[A]((respond: js.Function1[A | Thenable[A], _], reject: js.Function1[Any, _]) => {
      def fail(t: Throwable) =
        reject(t match {
          case js.JavaScriptException(e) => e
          case e                         => e
        })
      complete = {
        case Success(a) => Callback(respond(a))
        case Failure(e) => Callback(fail(e))
      }
    })
    (p, complete)
  }

}
