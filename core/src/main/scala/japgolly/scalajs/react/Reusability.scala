package japgolly.scalajs.react

import japgolly.scalajs.react.internal.{Box, OptionLike}
import java.time._
import java.util.{Date, UUID}
import org.scalajs.dom.console
import scala.annotation.tailrec
import scala.concurrent.{duration => scd}
import scala.reflect.ClassTag
import scala.scalajs.js.timers.{SetIntervalHandle, SetTimeoutHandle}
import scala.scalajs.js.{Date => JsDate}

/**
 * Tests whether one instance can be used in place of another.
 * Used mostly to compare properties and state of a component to avoid unnecessary updates.
 *
 * If you imagine a class with 8 fields, equality would compare all 8 fields where as this would typically just compare
 * the ID field, the update-date, or the revision number.
 * You might think of this as a very quick version of equality.
 *
 * Don't miss `Reusability.shouldComponentUpdate` which can be applied to a component via
 * `ScalaComponent.build#configure`.
 *
 * @since 0.9.0
 */
final class Reusability[A](val test: (A, A) => Boolean) extends AnyVal {

  @inline def updateNeeded(x: A, y: A): Boolean =
    !test(x, y)

  def contramap[B](f: B => A): Reusability[B] =
    new Reusability((x, y) => test(f(x), f(y)))

  def narrow[B <: A]: Reusability[B] =
    new Reusability[B](test)

  def unsafeWiden[B >: A]: Reusability[B] =
    unsafeSubst[B]

  def unsafeSubst[B]: Reusability[B] =
    new Reusability[B](test.asInstanceOf[(B, B) => Boolean])

  def testNot: (A, A) => Boolean =
    !test(_, _)

  def ||[B <: A](tryNext: Reusability[B]): Reusability[B] =
    Reusability[B]((x, y) => test(x, y) || tryNext.test(x, y))

  def &&[B <: A](tryNext: Reusability[B]): Reusability[B] =
    Reusability[B]((x, y) => test(x, y) && tryNext.test(x, y))

  def reusable(a: A)(implicit c: ClassTag[A]): Reusable[A] =
    Reusable.explicitly(a)(this)(c)

  def logNonReusable: Reusability[A] = logNonReusable()

  def logNonReusable(show: A => String = _.toString,
                     log : String => Unit = console.warn(_),
                     title: String = "Non-reusability:",
                     fmt : (String, => String, => String) => String = (t, x, y) => s"$t\n- $x\n- $y"): Reusability[A] =
    Reusability { (a, b) =>
      val r = test(a, b)
      if (!r)
        log(fmt(title, show(a), show(b)))
      r
    }
}

object Reusability extends ReusabilityMacros with ScalaVersionSpecificReusability {

  @inline def apply[A](f: (A, A) => Boolean): Reusability[A] =
    new Reusability(f)

  def suspend[A](f: => Reusability[A]): Reusability[A] =
    new Reusability((a, b) => f.test(a, b))

  @deprecated("Use Reusability.suspend", "2.0.0")
  def byName[A](f: => Reusability[A]): Reusability[A] =
    suspend(f)

  private[this] val alwaysInstance: Reusability[Any] =
    apply((_, _) => true)

  def always[A]: Reusability[A] =
    alwaysInstance.asInstanceOf[Reusability[A]]

  private[this] val neverInstance: Reusability[Any] =
    apply((_, _) => false)

  def never[A]: Reusability[A] =
    neverInstance.asInstanceOf[Reusability[A]]

  def const[A](r: Boolean): Reusability[A] =
    if (r) always else never

  /** Compare by reference. Reuse if both values are the same instance. */
  def byRef[A <: AnyRef]: Reusability[A] =
    new Reusability((a, b) => a eq b)

  def callbackByRef[A]: Reusability[CallbackTo[A]] =
    by((_: CallbackTo[A]).underlyingRepr)(byRef)

  def callbackOptionByRef[A]: Reusability[CallbackOption[A]] =
    by((_: CallbackOption[A]).underlyingRepr)(byRef)

  def callbackKleisliByRef[A, B]: Reusability[CallbackKleisli[A, B]] =
    by((_: CallbackKleisli[A, B]).underlyingRepr)(byRef)

  def asyncCallbackByRef[A]: Reusability[AsyncCallback[A]] =
    by((_: AsyncCallback[A]).underlyingRepr)(byRef)

  /** Compare using universal equality (Scala's == operator). */
  def by_==[A]: Reusability[A] =
    new Reusability((a, b) => a == b)

  /** Compare by reference and if different, compare using universal equality (Scala's == operator). */
  def byRefOr_==[A <: AnyRef]: Reusability[A] =
    byRef[A] || by_==[A]

  def by[A, B](f: A => B)(implicit r: Reusability[B]): Reusability[A] =
    r contramap f

  def byIterator[I[X] <: Iterable[X], A: Reusability]: Reusability[I[A]] =
    apply { (x, y) =>
      val i = x.iterator
      val j = y.iterator
      @tailrec
      def go: Boolean = {
        val hasNext = i.hasNext
        if (hasNext != j.hasNext)
          false
        else if (!hasNext)
          true
        else if (i.next() ~/~ j.next())
          false
        else
          go
      }
      go
    }

  def indexedSeq[S[X] <: IndexedSeq[X], A: Reusability]: Reusability[S[A]] =
    apply((x, y) =>
      (x.length == y.length) && x.indices.forall(i => x(i) ~=~ y(i)))

  def double(tolerance: Double): Reusability[Double] =
    apply((x, y) => (x - y).abs <= tolerance)

  def float(tolerance: Float): Reusability[Float] =
    apply((x, y) => (x - y).abs <= tolerance)

  def byJavaDuration[A](dur: (A, A) => Duration, tolerance: Duration): Reusability[A] =
    apply { (x, y) =>
      val d = dur(x, y).abs
      d.compareTo(tolerance) <= 0
    }

  def javaDuration(tolerance: Duration): Reusability[Duration] =
    byRef || byJavaDuration[Duration](_ minus _, tolerance)

  def instant(tolerance: Duration): Reusability[Instant] =
    byRef || byJavaDuration[Instant](Duration.between, tolerance)

  def localDateTime(tolerance: Duration): Reusability[LocalDateTime] =
    byRef || byJavaDuration[LocalDateTime](Duration.between, tolerance)

  def localDate(tolerance: Duration): Reusability[LocalDate] =
    byRef || byJavaDuration[LocalDate](Duration.between, tolerance)

  def offsetDateTime(tolerance: Duration): Reusability[OffsetDateTime] =
    byRef || byJavaDuration[OffsetDateTime](Duration.between, tolerance)

  def offsetTime(tolerance: Duration): Reusability[OffsetTime] =
    byRef || byJavaDuration[OffsetTime](Duration.between, tolerance)

  def zonedDateTime(tolerance: Duration): Reusability[ZonedDateTime] =
    byRef || byJavaDuration[ZonedDateTime](Duration.between, tolerance)

  def byScalaDuration[A](dur: (A, A) => scd.Duration, tolerance: scd.Duration): Reusability[A] =
    apply { (x, y) =>
      var d = dur(x, y)
      if (d.length < 0)
        d = scd.FiniteDuration(d.length.abs, d.unit)
      d <= tolerance
    }

  def scalaDuration(tolerance: scd.Duration): Reusability[scd.Duration] =
    byRef || byScalaDuration[scd.Duration](_ - _, tolerance)

  def finiteDuration(tolerance: scd.Duration): Reusability[scd.FiniteDuration] =
    scalaDuration(tolerance).narrow

  def deadline(tolerance: scd.Duration): Reusability[scd.Deadline] =
    finiteDuration(tolerance).contramap(_.time)

  /**
   * This is not implicit because the point of Reusability is to be fast, where as full comparison of all keys and
   * values in a map, is usually not desirable; in some cases it will probably even be faster just rerender and have
   * React determine that nothing has changed.
   *
   * Nonetheless, there are cases where a full comparison is desired and so use this as needed. `Reusability[K]` isn't
   * needed because its existence in the map (and thus universal equality) is all that's necessary.
   * Time is O(|m₁|+|m₂|).
   */
  def map[K, V](implicit rv: Reusability[V]): Reusability[Map[K, V]] =
    byRef[Map[K, V]] || apply((m, n) =>
      if (m.isEmpty)
        n.isEmpty
      else if (n.isEmpty)
        false
      else {
        var ok = true
        var msize = 0

        val mi = m.iterator
        while (ok && mi.hasNext) {
          val (k, v) = mi.next()
          msize += 1
          ok = n.get(k).exists(rv.test(v, _))
        }

        ok && msize == n.size
      }
    )

  /** Declare a type reusable when both values pass a given predicate. */
  def when[A](f: A => Boolean): Reusability[A] =
    apply((a, b) => f(a) && f(b))

  /** Declare a type reusable when both values fail a given predicate. */
  def unless[A](f: A => Boolean): Reusability[A] =
    when(!f(_))

  // -------------------------------------------------------------------------------------------------------------------
  // Implicit Instances

  // Prohibited:
  // ===========
  // Array  - it's mutable. Reusability & mutability are incompatible.
  // Stream - it's lazy. Reusability & non-strictness are incompatible.

  @inline implicit def unit   : Reusability[Unit   ] = always
  @inline implicit def boolean: Reusability[Boolean] = by_==
  @inline implicit def byte   : Reusability[Byte   ] = by_==
  @inline implicit def char   : Reusability[Char   ] = by_==
  @inline implicit def short  : Reusability[Short  ] = by_==
  @inline implicit def int    : Reusability[Int    ] = by_==
  @inline implicit def long   : Reusability[Long   ] = by_==
  @inline implicit def string : Reusability[String ] = by_==
  @inline implicit def date   : Reusability[Date   ] = by_==
  @inline implicit def uuid   : Reusability[UUID   ] = by_==

  implicit def jsDate: Reusability[JsDate] =
    apply((x, y) => x.getTime() == y.getTime())

  @inline implicit def option[A: Reusability]: Reusability[Option[A]] =
    optionLike

  implicit def optionLike[O[_], A](implicit o: OptionLike[O], r: Reusability[A]): Reusability[O[A]] =
    apply((x, y) =>
      o.fold(x, o isEmpty y)(xa =>
        o.fold(y, false)(ya =>
          xa ~=~ ya)))

  implicit def either[A: Reusability, B: Reusability]: Reusability[Either[A, B]] =
    apply((x, y) =>
      x.fold[Boolean](
        a => y.fold(a ~=~ _, _ => false),
        b => y.fold(_ => false, b ~=~ _)))

  implicit def list[A: Reusability]: Reusability[List[A]] =
    byRef[List[A]] || byIterator[List, A]

  implicit def vector[A: Reusability]: Reusability[Vector[A]] =
    byRef[Vector[A]] || indexedSeq[Vector, A]

  implicit def set[A]: Reusability[Set[A]] =
    byRefOr_== // universal equality must hold for Sets

  implicit def box[A: Reusability]: Reusability[Box[A]] =
    by(_.unbox)

  implicit def range: Reusability[Range] =
    byRefOr_==

  implicit lazy val setIntervalHandle: Reusability[SetIntervalHandle] =
    by_==

  implicit lazy val setTimeoutHandle: Reusability[SetTimeoutHandle] =
    by_==

  implicit lazy val callbackSetIntervalResult: Reusability[Callback.SetIntervalResult] =
    byRef || by(_.handle)

  implicit lazy val callbackSetTimeoutResult: Reusability[Callback.SetTimeoutResult] =
    byRef || by(_.handle)

  // java.time._

  implicit def clock: Reusability[Clock] =
    Reusability.byRefOr_==

  implicit def dayOfWeek: Reusability[DayOfWeek] =
    Reusability.by_==

  implicit def localDate: Reusability[LocalDate] =
    Reusability.byRefOr_==

  implicit def month: Reusability[Month] =
    Reusability.by_==

  implicit def monthDay: Reusability[MonthDay] =
    Reusability.byRefOr_==

  implicit def period: Reusability[Period] =
    Reusability.byRefOr_==

  implicit def year: Reusability[Year] =
    Reusability.by_==

  implicit def yearMonth: Reusability[YearMonth] =
    Reusability.byRefOr_==

  implicit def zoneId: Reusability[ZoneId] =
    Reusability.by_==

  implicit def zoneOffset: Reusability[ZoneOffset] =
    Reusability.by_==

  // Generated by bin/gen-reusable
  implicit def tuple2[A:Reusability, B:Reusability]: Reusability[(A,B)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2))

  implicit def tuple3[A:Reusability, B:Reusability, C:Reusability]: Reusability[(A,B,C)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3))

  implicit def tuple4[A:Reusability, B:Reusability, C:Reusability, D:Reusability]: Reusability[(A,B,C,D)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3) && (x._4 ~=~ y._4))

  implicit def tuple5[A:Reusability, B:Reusability, C:Reusability, D:Reusability, E:Reusability]: Reusability[(A,B,C,D,E)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3) && (x._4 ~=~ y._4) && (x._5 ~=~ y._5))

  implicit def tuple6[A:Reusability, B:Reusability, C:Reusability, D:Reusability, E:Reusability, F:Reusability]: Reusability[(A,B,C,D,E,F)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3) && (x._4 ~=~ y._4) && (x._5 ~=~ y._5) && (x._6 ~=~ y._6))

  implicit def tuple7[A:Reusability, B:Reusability, C:Reusability, D:Reusability, E:Reusability, F:Reusability, G:Reusability]: Reusability[(A,B,C,D,E,F,G)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3) && (x._4 ~=~ y._4) && (x._5 ~=~ y._5) && (x._6 ~=~ y._6) && (x._7 ~=~ y._7))

  implicit def tuple8[A:Reusability, B:Reusability, C:Reusability, D:Reusability, E:Reusability, F:Reusability, G:Reusability, H:Reusability]: Reusability[(A,B,C,D,E,F,G,H)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3) && (x._4 ~=~ y._4) && (x._5 ~=~ y._5) && (x._6 ~=~ y._6) && (x._7 ~=~ y._7) && (x._8 ~=~ y._8))

  implicit def tuple9[A:Reusability, B:Reusability, C:Reusability, D:Reusability, E:Reusability, F:Reusability, G:Reusability, H:Reusability, I:Reusability]: Reusability[(A,B,C,D,E,F,G,H,I)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3) && (x._4 ~=~ y._4) && (x._5 ~=~ y._5) && (x._6 ~=~ y._6) && (x._7 ~=~ y._7) && (x._8 ~=~ y._8) && (x._9 ~=~ y._9))

  implicit def tuple10[A:Reusability, B:Reusability, C:Reusability, D:Reusability, E:Reusability, F:Reusability, G:Reusability, H:Reusability, I:Reusability, J:Reusability]: Reusability[(A,B,C,D,E,F,G,H,I,J)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3) && (x._4 ~=~ y._4) && (x._5 ~=~ y._5) && (x._6 ~=~ y._6) && (x._7 ~=~ y._7) && (x._8 ~=~ y._8) && (x._9 ~=~ y._9) && (x._10 ~=~ y._10))

  implicit def tuple11[A:Reusability, B:Reusability, C:Reusability, D:Reusability, E:Reusability, F:Reusability, G:Reusability, H:Reusability, I:Reusability, J:Reusability, K:Reusability]: Reusability[(A,B,C,D,E,F,G,H,I,J,K)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3) && (x._4 ~=~ y._4) && (x._5 ~=~ y._5) && (x._6 ~=~ y._6) && (x._7 ~=~ y._7) && (x._8 ~=~ y._8) && (x._9 ~=~ y._9) && (x._10 ~=~ y._10) && (x._11 ~=~ y._11))

  implicit def tuple12[A:Reusability, B:Reusability, C:Reusability, D:Reusability, E:Reusability, F:Reusability, G:Reusability, H:Reusability, I:Reusability, J:Reusability, K:Reusability, L:Reusability]: Reusability[(A,B,C,D,E,F,G,H,I,J,K,L)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3) && (x._4 ~=~ y._4) && (x._5 ~=~ y._5) && (x._6 ~=~ y._6) && (x._7 ~=~ y._7) && (x._8 ~=~ y._8) && (x._9 ~=~ y._9) && (x._10 ~=~ y._10) && (x._11 ~=~ y._11) && (x._12 ~=~ y._12))

  implicit def tuple13[A:Reusability, B:Reusability, C:Reusability, D:Reusability, E:Reusability, F:Reusability, G:Reusability, H:Reusability, I:Reusability, J:Reusability, K:Reusability, L:Reusability, M:Reusability]: Reusability[(A,B,C,D,E,F,G,H,I,J,K,L,M)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3) && (x._4 ~=~ y._4) && (x._5 ~=~ y._5) && (x._6 ~=~ y._6) && (x._7 ~=~ y._7) && (x._8 ~=~ y._8) && (x._9 ~=~ y._9) && (x._10 ~=~ y._10) && (x._11 ~=~ y._11) && (x._12 ~=~ y._12) && (x._13 ~=~ y._13))

  implicit def tuple14[A:Reusability, B:Reusability, C:Reusability, D:Reusability, E:Reusability, F:Reusability, G:Reusability, H:Reusability, I:Reusability, J:Reusability, K:Reusability, L:Reusability, M:Reusability, N:Reusability]: Reusability[(A,B,C,D,E,F,G,H,I,J,K,L,M,N)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3) && (x._4 ~=~ y._4) && (x._5 ~=~ y._5) && (x._6 ~=~ y._6) && (x._7 ~=~ y._7) && (x._8 ~=~ y._8) && (x._9 ~=~ y._9) && (x._10 ~=~ y._10) && (x._11 ~=~ y._11) && (x._12 ~=~ y._12) && (x._13 ~=~ y._13) && (x._14 ~=~ y._14))

  implicit def tuple15[A:Reusability, B:Reusability, C:Reusability, D:Reusability, E:Reusability, F:Reusability, G:Reusability, H:Reusability, I:Reusability, J:Reusability, K:Reusability, L:Reusability, M:Reusability, N:Reusability, O:Reusability]: Reusability[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3) && (x._4 ~=~ y._4) && (x._5 ~=~ y._5) && (x._6 ~=~ y._6) && (x._7 ~=~ y._7) && (x._8 ~=~ y._8) && (x._9 ~=~ y._9) && (x._10 ~=~ y._10) && (x._11 ~=~ y._11) && (x._12 ~=~ y._12) && (x._13 ~=~ y._13) && (x._14 ~=~ y._14) && (x._15 ~=~ y._15))

  implicit def tuple16[A:Reusability, B:Reusability, C:Reusability, D:Reusability, E:Reusability, F:Reusability, G:Reusability, H:Reusability, I:Reusability, J:Reusability, K:Reusability, L:Reusability, M:Reusability, N:Reusability, O:Reusability, P:Reusability]: Reusability[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3) && (x._4 ~=~ y._4) && (x._5 ~=~ y._5) && (x._6 ~=~ y._6) && (x._7 ~=~ y._7) && (x._8 ~=~ y._8) && (x._9 ~=~ y._9) && (x._10 ~=~ y._10) && (x._11 ~=~ y._11) && (x._12 ~=~ y._12) && (x._13 ~=~ y._13) && (x._14 ~=~ y._14) && (x._15 ~=~ y._15) && (x._16 ~=~ y._16))

  implicit def tuple17[A:Reusability, B:Reusability, C:Reusability, D:Reusability, E:Reusability, F:Reusability, G:Reusability, H:Reusability, I:Reusability, J:Reusability, K:Reusability, L:Reusability, M:Reusability, N:Reusability, O:Reusability, P:Reusability, Q:Reusability]: Reusability[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3) && (x._4 ~=~ y._4) && (x._5 ~=~ y._5) && (x._6 ~=~ y._6) && (x._7 ~=~ y._7) && (x._8 ~=~ y._8) && (x._9 ~=~ y._9) && (x._10 ~=~ y._10) && (x._11 ~=~ y._11) && (x._12 ~=~ y._12) && (x._13 ~=~ y._13) && (x._14 ~=~ y._14) && (x._15 ~=~ y._15) && (x._16 ~=~ y._16) && (x._17 ~=~ y._17))

  implicit def tuple18[A:Reusability, B:Reusability, C:Reusability, D:Reusability, E:Reusability, F:Reusability, G:Reusability, H:Reusability, I:Reusability, J:Reusability, K:Reusability, L:Reusability, M:Reusability, N:Reusability, O:Reusability, P:Reusability, Q:Reusability, R:Reusability]: Reusability[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3) && (x._4 ~=~ y._4) && (x._5 ~=~ y._5) && (x._6 ~=~ y._6) && (x._7 ~=~ y._7) && (x._8 ~=~ y._8) && (x._9 ~=~ y._9) && (x._10 ~=~ y._10) && (x._11 ~=~ y._11) && (x._12 ~=~ y._12) && (x._13 ~=~ y._13) && (x._14 ~=~ y._14) && (x._15 ~=~ y._15) && (x._16 ~=~ y._16) && (x._17 ~=~ y._17) && (x._18 ~=~ y._18))

  implicit def tuple19[A:Reusability, B:Reusability, C:Reusability, D:Reusability, E:Reusability, F:Reusability, G:Reusability, H:Reusability, I:Reusability, J:Reusability, K:Reusability, L:Reusability, M:Reusability, N:Reusability, O:Reusability, P:Reusability, Q:Reusability, R:Reusability, S:Reusability]: Reusability[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3) && (x._4 ~=~ y._4) && (x._5 ~=~ y._5) && (x._6 ~=~ y._6) && (x._7 ~=~ y._7) && (x._8 ~=~ y._8) && (x._9 ~=~ y._9) && (x._10 ~=~ y._10) && (x._11 ~=~ y._11) && (x._12 ~=~ y._12) && (x._13 ~=~ y._13) && (x._14 ~=~ y._14) && (x._15 ~=~ y._15) && (x._16 ~=~ y._16) && (x._17 ~=~ y._17) && (x._18 ~=~ y._18) && (x._19 ~=~ y._19))

  implicit def tuple20[A:Reusability, B:Reusability, C:Reusability, D:Reusability, E:Reusability, F:Reusability, G:Reusability, H:Reusability, I:Reusability, J:Reusability, K:Reusability, L:Reusability, M:Reusability, N:Reusability, O:Reusability, P:Reusability, Q:Reusability, R:Reusability, S:Reusability, T:Reusability]: Reusability[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3) && (x._4 ~=~ y._4) && (x._5 ~=~ y._5) && (x._6 ~=~ y._6) && (x._7 ~=~ y._7) && (x._8 ~=~ y._8) && (x._9 ~=~ y._9) && (x._10 ~=~ y._10) && (x._11 ~=~ y._11) && (x._12 ~=~ y._12) && (x._13 ~=~ y._13) && (x._14 ~=~ y._14) && (x._15 ~=~ y._15) && (x._16 ~=~ y._16) && (x._17 ~=~ y._17) && (x._18 ~=~ y._18) && (x._19 ~=~ y._19) && (x._20 ~=~ y._20))

  implicit def tuple21[A:Reusability, B:Reusability, C:Reusability, D:Reusability, E:Reusability, F:Reusability, G:Reusability, H:Reusability, I:Reusability, J:Reusability, K:Reusability, L:Reusability, M:Reusability, N:Reusability, O:Reusability, P:Reusability, Q:Reusability, R:Reusability, S:Reusability, T:Reusability, U:Reusability]: Reusability[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3) && (x._4 ~=~ y._4) && (x._5 ~=~ y._5) && (x._6 ~=~ y._6) && (x._7 ~=~ y._7) && (x._8 ~=~ y._8) && (x._9 ~=~ y._9) && (x._10 ~=~ y._10) && (x._11 ~=~ y._11) && (x._12 ~=~ y._12) && (x._13 ~=~ y._13) && (x._14 ~=~ y._14) && (x._15 ~=~ y._15) && (x._16 ~=~ y._16) && (x._17 ~=~ y._17) && (x._18 ~=~ y._18) && (x._19 ~=~ y._19) && (x._20 ~=~ y._20) && (x._21 ~=~ y._21))

  implicit def tuple22[A:Reusability, B:Reusability, C:Reusability, D:Reusability, E:Reusability, F:Reusability, G:Reusability, H:Reusability, I:Reusability, J:Reusability, K:Reusability, L:Reusability, M:Reusability, N:Reusability, O:Reusability, P:Reusability, Q:Reusability, R:Reusability, S:Reusability, T:Reusability, U:Reusability, V:Reusability]: Reusability[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V)] =
    apply((x,y) => (x._1 ~=~ y._1) && (x._2 ~=~ y._2) && (x._3 ~=~ y._3) && (x._4 ~=~ y._4) && (x._5 ~=~ y._5) && (x._6 ~=~ y._6) && (x._7 ~=~ y._7) && (x._8 ~=~ y._8) && (x._9 ~=~ y._9) && (x._10 ~=~ y._10) && (x._11 ~=~ y._11) && (x._12 ~=~ y._12) && (x._13 ~=~ y._13) && (x._14 ~=~ y._14) && (x._15 ~=~ y._15) && (x._16 ~=~ y._16) && (x._17 ~=~ y._17) && (x._18 ~=~ y._18) && (x._19 ~=~ y._19) && (x._20 ~=~ y._20) && (x._21 ~=~ y._21) && (x._22 ~=~ y._22))

  // ===================================================================================================================

  object MapImplicits {
    implicit def reusabilityMap[K, V](implicit rv: Reusability[V]): Reusability[Map[K, V]] =
      Reusability.byRef || Reusability.map
  }

  object TemporalImplicitsWithoutTolerance {

    implicit lazy val reusabilityJavaDuration: Reusability[Duration] =
      byRefOr_==

    implicit lazy val reusabilityInstant: Reusability[Instant] =
      byRefOr_==

    implicit lazy val reusabilityLocalDateTime: Reusability[LocalDateTime] =
      byRefOr_==

    implicit lazy val reusabilityLocalDate: Reusability[LocalDate] =
      byRefOr_==

    implicit lazy val reusabilityOffsetDateTime: Reusability[OffsetDateTime] =
      byRefOr_==

    implicit lazy val reusabilityOffsetTime: Reusability[OffsetTime] =
      byRefOr_==

    implicit lazy val reusabilityZonedDateTime: Reusability[ZonedDateTime] =
      byRefOr_==

    implicit lazy val reusabilityScalaDuration: Reusability[scd.Duration] =
      byRefOr_==

    implicit lazy val reusabilityFiniteDuration: Reusability[scd.FiniteDuration] =
      byRefOr_==

    implicit lazy val reusabilityDeadline: Reusability[scd.Deadline] =
      byRefOr_==
  }

  // ===================================================================================================================

  def shouldComponentUpdateAnd[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot](f: ShouldComponentUpdateResult[P, S, B] => Callback): ScalaComponent.Config[P, C, S, B, U, U] =
    _.shouldComponentUpdate { i =>
      val r = ShouldComponentUpdateResult(i)
      f(r).map(_ => r.update)
    }

  def shouldComponentUpdateAndLog[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot](name: String): ScalaComponent.Config[P, C, S, B, U, U] =
    shouldComponentUpdateAnd(_ log name)

  final case class ShouldComponentUpdateResult[P: Reusability, S: Reusability, B](self: ScalaComponent.Lifecycle.ShouldComponentUpdate[P, S, B]) {
    def mounted       = self.mountedImpure
    def backend       = self.backend
    def propsChildren = self.propsChildren
    def currentProps  = self.currentProps
    def currentState  = self.currentState
    def nextProps     = self.nextProps
    def nextState     = self.nextState
    def getDOMNode    = self.getDOMNode

    val updateProps: Boolean = currentProps ~/~ nextProps
    val updateState: Boolean = currentState ~/~ nextState
    val update     : Boolean = updateProps || updateState

    def log(name: String): Callback =
      Callback.log(
        s"""
           |s"$name.shouldComponentUpdate = $update
           |  Props: $updateProps. [$currentProps] ⇒ [$nextProps]
           |  State: $updateState. [$currentState] ⇒ [$nextState]
         """.stripMargin)
  }
}
