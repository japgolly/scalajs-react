package japgolly.scalajs.react.internal

final class Singleton[A](val value: A)(val mutable: () => A)

object Singleton {

  implicit val Null: Singleton[Null] =
    new Singleton(null)(() => null)

  implicit val Unit: Singleton[Unit] =
    new Singleton(())(() => ())

  implicit val BoxUnit: Singleton[Box[Unit]] =
    new Singleton(Box.Unit)(() => Box(()))

  sealed trait Not[A]
  @inline implicit def noSingletonFor[A]: Not[A] = null
  implicit def singletonFor1[A: Singleton]: Not[A] = null
  implicit def singletonFor2[A: Singleton]: Not[A] = null
}
