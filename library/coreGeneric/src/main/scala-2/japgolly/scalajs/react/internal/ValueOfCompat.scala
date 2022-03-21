package japgolly.scalajs.react.internal

// This can be deleted once https://github.com/lampepfl/dotty/issues/12510 is resolved
object ValueOfCompat {
  type ValueOf[A] = scala.ValueOf[A]
}
