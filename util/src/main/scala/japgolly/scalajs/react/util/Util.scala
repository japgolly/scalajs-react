package japgolly.scalajs.react.util

import org.scalajs.dom
import scala.collection.Factory
import scala.collection.mutable.Builder
import scala.scalajs.LinkingInfo.developmentMode
import scala.scalajs.js
import scala.util.{Failure, Success, Try}

object Util {
  @inline implicit class SJRIExt_String(private val s: String) extends AnyVal {
    def indentLines(i: String): String =
      if (i.isEmpty)
        s
      else
        i + s.replace("\n", "\n" + i)

    def indentLines(spaces: Int): String =
      if (spaces <= 0)
        s
      else
        indentLines(" " * spaces)
  }

  def catchAll[A](a: => A): Try[A] =
    try Success(a)
    catch {case t: Throwable => Failure(t) }

  val identity: Any => Nothing =
    ((a: Any) => a).asInstanceOf[Any => Nothing]

  @inline def identityFn[A]: A => A =
    identity

  val identityJs: js.Function1[Any, Nothing] = {
    val f: js.Function1[Any, Any] = a => a
    f.asInstanceOf[js.Function1[Any, Nothing]]
  }

  @inline def identityFnJs[A]: js.Function1[A, A] =
    identityJs

  def intercalateTo[F[_], A](as: Iterator[A], sep: A)(implicit cbf: Factory[A, F[A]]): F[A] = {
    val b = cbf.newBuilder
    intercalateInto(b, as, sep)
    b.result()
  }

  def intercalateInto[A](b: Builder[A, _], it: Iterator[A], sep: A): Unit =
    if (it.hasNext) {
      val first = it.next()
      b += first
      for (a <- it) {
        b += sep
        b += a
      }
    }

  @inline def devAssertWarn(test: => Boolean, msg: => String): Unit =
    if (developmentMode)
      if (!test)
        dom.console.warn(msg)

  @inline def devAssert(test: => Boolean, msg: => String): Unit =
    if (developmentMode)
      devAssert(Option.unless(test)(msg))

  @inline def devAssert(errMsg: => Option[String]): Unit =
    if (developmentMode)
      for (e <- errMsg) {
        try dom.console.error(e) catch {case _: Throwable => }
        throw new AssertionError(e)
      }

}
