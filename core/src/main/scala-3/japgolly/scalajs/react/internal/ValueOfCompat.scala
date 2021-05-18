package japgolly.scalajs.react.internal

import scala.language.`3.0`

// This can be deleted once Scala 2.12 support is dropped
object ValueOfCompat {
  // type ValueOf[A] = scala.ValueOf[A]

  // TODO: This is a workaround for https://github.com/lampepfl/dotty/issues/12510
  @scala.annotation.implicitNotFound(msg = "No singleton value available for ${T}.")
  final class ValueOf[T](val value: T) extends AnyVal
  object ValueOf {
    implicit def unit: ValueOf[Unit] = ValueOf(())
    implicit def emptyTuple: ValueOf[EmptyTuple] = ValueOf(EmptyTuple)
  }
}
