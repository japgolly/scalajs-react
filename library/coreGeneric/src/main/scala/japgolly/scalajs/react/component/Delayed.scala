package japgolly.scalajs.react.component

import japgolly.scalajs.react.util.Trampoline
import japgolly.scalajs.react.vdom.VdomNode

final class Delayed[+A] private[react] (private[Delayed] val trampoline: Trampoline[A]) extends AnyVal { self =>  
  @inline def eval(): A =
    trampoline.run

  @inline def map[B](f: A => B): Delayed[B] =
    new Delayed(trampoline.map(f))

  @inline def flatMap[B](f: A => Delayed[B]): Delayed[B] =
    new Delayed(trampoline.flatMap(f.andThen(_.trampoline)))
}

object Delayed {
  @inline def apply[A](a: => A): Delayed[A] =
    new Delayed(Trampoline.delay(() => a))

  implicit class DelayedOps[I, O](private val h: I => Delayed[O]) extends AnyVal {
    @inline def contramap[A](f: A => I): A => Delayed[O] = f andThen h
  }

  @inline implicit def autoLift[A](a: => A)(implicit f: A => VdomNode): Delayed[VdomNode] =
    Delayed(f(a))

  @inline def fromFunction[O](f: () => O): Delayed[O] =
    Delayed(f())

  @inline def fromFunction[I, O](f: I => O): I => Delayed[O] =
    i => Delayed(f(i))

  @inline def fromFunction[I1, I2, O](f: (I1, I2) => O): (I1, I2) => Delayed[O] =
    (i1, i2) => Delayed(f(i1, i2))

  @inline def fromFunction[I1, I2, I3, O](f: (I1, I2, I3) => O): (I1, I2, I3) => Delayed[O] =
    (i1, i2, i3) => Delayed(f(i1, i2, i3))

  @inline def fromFunction[I1, I2, I3, I4, O](f: (I1, I2, I3, I4) => O): (I1, I2, I3, I4) => Delayed[O] =
    (i1, i2, i3, i4) => Delayed(f(i1, i2, i3, i4))

  @inline def fromFunction[I1, I2, I3, I4, I5, O](f: (I1, I2, I3, I4, I5) => O): (I1, I2, I3, I4, I5) => Delayed[O] =
    (i1, i2, i3, i4, i5) => Delayed(f(i1, i2, i3, i4, i5))

  @inline def fromFunction[I1, I2, I3, I4, I5, I6, O](f: (I1, I2, I3, I4, I5, I6) => O): (I1, I2, I3, I4, I5, I6) => Delayed[O] =
    (i1, i2, i3, i4, i5, i6) => Delayed(f(i1, i2, i3, i4, i5, i6))

  @inline def fromFunction[I1, I2, I3, I4, I5, I6, I7, O](f: (I1, I2, I3, I4, I5, I6, I7) => O): (I1, I2, I3, I4, I5, I6, I7) => Delayed[O] =
    (i1, i2, i3, i4, i5, i6, i7) => Delayed(f(i1, i2, i3, i4, i5, i6, i7))

  @inline def fromFunction[I1, I2, I3, I4, I5, I6, I7, I8, O](f: (I1, I2, I3, I4, I5, I6, I7, I8) => O): (I1, I2, I3, I4, I5, I6, I7, I8) => Delayed[O] =
    (i1, i2, i3, i4, i5, i6, i7, i8) => Delayed(f(i1, i2, i3, i4, i5, i6, i7, i8))

  @inline def fromFunction[I1, I2, I3, I4, I5, I6, I7, I8, I9, O](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9) => O): (I1, I2, I3, I4, I5, I6, I7, I8, I9) => Delayed[O] =
    (i1, i2, i3, i4, i5, i6, i7, i8, i9) => Delayed(f(i1, i2, i3, i4, i5, i6, i7, i8, i9))

  @inline def fromFunction[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10) => O): (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10) => Delayed[O] =
    (i1, i2, i3, i4, i5, i6, i7, i8, i9, i10) => Delayed(f(i1, i2, i3, i4, i5, i6, i7, i8, i9, i10))

  @inline def fromFunction[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11) => O): (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11) => Delayed[O] =
    (i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11) => Delayed(f(i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11))

  @inline def fromFunction[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12) => O): (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12) => Delayed[O] =
    (i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12) => Delayed(f(i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12))

  @inline def fromFunction[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13) => O): (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13) => Delayed[O] =
    (i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13) => Delayed(f(i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13))

  @inline def fromFunction[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14) => O): (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14) => Delayed[O] =
    (i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14) => Delayed(f(i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14))

  @inline def fromFunction[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15) => O): (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15) => Delayed[O] =
    (i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15) => Delayed(f(i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15))

  @inline def fromFunction[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16) => O): (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16) => Delayed[O] =
    (i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15, i16) => Delayed(f(i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15, i16))

  @inline def fromFunction[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, O](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17) => O): (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17) => Delayed[O] =
    (i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15, i16, i17) => Delayed(f(i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15, i16, i17))

  @inline def fromFunction[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, O](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18) => O): (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18) => Delayed[O] =
    (i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15, i16, i17, i18) => Delayed(f(i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15, i16, i17, i18))

  @inline def fromFunction[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, O](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19) => O): (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19) => Delayed[O] =
    (i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15, i16, i17, i18, i19) => Delayed(f(i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15, i16, i17, i18, i19))

  @inline def fromFunction[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, O](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20) => O): (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20) => Delayed[O] =
    (i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15, i16, i17, i18, i19, i20) => Delayed(f(i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15, i16, i17, i18, i19, i20))

  @inline def fromFunction[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, O](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21) => O): (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21) => Delayed[O] =
    (i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15, i16, i17, i18, i19, i20, i21) => Delayed(f(i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15, i16, i17, i18, i19, i20, i21))

  @inline def fromFunction[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22, O](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22) => O): (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22) => Delayed[O] =
    (i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15, i16, i17, i18, i19, i20, i21, i22) => Delayed(f(i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15, i16, i17, i18, i19, i20, i21, i22))
}

