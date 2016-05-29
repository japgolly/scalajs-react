package japgolly.scalajs.react

final class Singleton[A](val value: A)(val mutable: () => A)

object Singleton {
  implicit val Null    = new Singleton[Null](null)(() => null)
  implicit val Unit    = new Singleton(())(() => ())
  implicit val BoxUnit = new Singleton(Box.Unit)(() => Box(()))

  sealed trait Not[A]
  @inline implicit def noSingletonFor[A]: Not[A] = null
  implicit def singletonFor1[A: Singleton]: Not[A] = null
  implicit def singletonFor2[A: Singleton]: Not[A] = null
}
