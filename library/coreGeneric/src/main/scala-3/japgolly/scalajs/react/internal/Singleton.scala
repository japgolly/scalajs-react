package japgolly.scalajs.react.internal

import scala.language.`3.0`
import scala.scalajs.js
import scala.util.NotGiven

final class Singleton[A](val value: A)(val mutableObj: () => js.Object)

object Singleton {

  implicit val Null: Singleton[Null] =
    new Singleton(null)(() => new js.Object)

  implicit val Unit: Singleton[Unit] =
    new Singleton(())(() => new js.Object)

  implicit val BoxUnit: Singleton[Box[Unit]] =
    new Singleton(Box.Unit)(() => Box(()))

  // TODO: use erased class
  sealed trait Not[A]
  inline given noSingletonFor[A](using inline ev: NotGiven[Singleton[A]]): Not[A] = null
}
