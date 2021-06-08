package japgolly.scalajs.react.internal

import org.scalajs.dom
import scala.collection.compat._
import scala.collection.mutable.Builder
import scala.scalajs.LinkingInfo.developmentMode
import scala.util.{Failure, Success, Try}

object Util {
  @inline implicit class SJRIExt_String(private val s: String) extends AnyVal {
    def indent(i: String): String =
      if (i.isEmpty)
        s
      else
        i + s.replace("\n", "\n" + i)

    def indent(spaces: Int): String =
      if (spaces <= 0)
        s
      else
        indent(" " * spaces)
  }

  def catchAll[A](a: => A): Try[A] =
    try Success(a)
    catch {case t: Throwable => Failure(t) }

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
