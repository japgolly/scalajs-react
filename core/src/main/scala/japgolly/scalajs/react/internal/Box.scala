package japgolly.scalajs.react.internal

import scalajs.js
import scalajs.js.annotation._

@js.native
trait Box[+A] extends js.Object {
  @JSName("a") val unbox: A = js.native
}

object Box {
  @inline def apply[A](value: A): Box[A] =
    js.Dynamic.literal(a = value.asInstanceOf[js.Any]).asInstanceOf[Box[A]]

  val Unit: Box[Unit] =
    Box(())
}

