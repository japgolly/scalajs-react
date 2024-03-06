package japgolly.scalajs.react.util

sealed trait ImplicitUnit

object ImplicitUnit {
  @inline implicit def instance: ImplicitUnit = null
}
