package japgolly.scalajs.react.internal

import scala.scalajs.js
// import scala.scalajs.js.annotation._

@js.native
trait Box[+A] extends js.Object {
  // @JSName("a") val unbox: A = js.native
}

object Box {
  @inline def apply[A](value: A): Box[A] =
    js.Dynamic.literal(a = value.asInstanceOf[js.Any]).asInstanceOf[Box[A]]

  val Unit: Box[Unit] =
    Box(())

  @inline implicit final class Ops[A](private val box: Box[A]) extends AnyVal {
    @inline def unbox: A = box.asInstanceOf[js.Dynamic].a.asInstanceOf[A]
  }
}
