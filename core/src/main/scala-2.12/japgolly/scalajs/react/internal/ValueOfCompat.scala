package japgolly.scalajs.react.internal

object ValueOfCompat {
  @scala.annotation.implicitNotFound(msg = "No singleton value available for ${T}.")
  final class ValueOf[T](val value: T) extends AnyVal

  object ValueOf {
    implicit def unit: ValueOf[Unit] =
      new ValueOf(())
  }
}
