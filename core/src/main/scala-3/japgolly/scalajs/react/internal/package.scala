package japgolly.scalajs.react.internal

private val identityFnInstance: Any => Any =
  a => a

inline def identityFn[A]: A => A =
  identityFnInstance.asInstanceOf[A => A]
