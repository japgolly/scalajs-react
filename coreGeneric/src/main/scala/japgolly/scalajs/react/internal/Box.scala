package japgolly.scalajs.react.internal

import scala.scalajs.js

@js.native
trait Box[+A] extends js.Object {
  // TODO: Check if this breaks bincompat
  // @JSName("a") val unbox: A = js.native
}

object Box {

  // @inline def apply[A](value: A): Box[A] =
  //   js.Dynamic.literal(a = value.asInstanceOf[js.Any]).asInstanceOf[Box[A]]

  def apply[A](value: A): Box[A] = {
    // TODO: Can't get Scala.js so just generate `{a:value}`
    val box = new js.Object().asInstanceOf[js.Dynamic]
    box.a = value.asInstanceOf[js.Any]
    box.asInstanceOf[Box[A]]
  }

  @inline implicit final class Ops[A](private val box: Box[A]) extends AnyVal {
    @inline def unbox: A = box.asInstanceOf[js.Dynamic].a.asInstanceOf[A]
  }

  val Unit: Box[Unit] =
    Box(())
}
