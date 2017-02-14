package japgolly.scalajs.react.internal

import scala.scalajs.js

final class Singleton[A](val value: A)(/*val*/ mutable: () => A, val mutableObj: () => js.Object)

object Singleton {

  implicit val Null: Singleton[Null] =
    new Singleton(null)(() => null, () => new js.Object)

  implicit val Unit: Singleton[Unit] =
    new Singleton(())(() => (), () => new js.Object)

  implicit val BoxUnit: Singleton[Box[Unit]] =
    new Singleton(Box.Unit)(() => Box(()), () => Box(()))

  sealed trait Not[A]
  @inline implicit def noSingletonFor[A]: Not[A] = null
  implicit def singletonFor1[A: Singleton]: Not[A] = null
  implicit def singletonFor2[A: Singleton]: Not[A] = null
}
