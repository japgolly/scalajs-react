package japgolly.scalajs.react

import scalajs.js
import scalajs.js.annotation._

@ScalaJSDefined
trait Box[+A] extends js.Object {
  val a: A
}

object Box {
  @inline def apply[A](value: A): Box[A] =
    new Box[A] { override val a = value }

  val Unit: Box[Unit] =
    Box(())
}

