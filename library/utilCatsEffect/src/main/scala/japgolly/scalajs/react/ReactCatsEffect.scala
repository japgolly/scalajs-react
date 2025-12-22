package japgolly.scalajs.react

import cats.effect.unsafe.IORuntime

object ReactCatsEffect {

  val runtimeFn: () => IORuntime =
    () => runtime

  var runtime: IORuntime =
    IORuntime.global

  def withRuntime[A](r: IORuntime)(a: => A): A = {
    val prev = runtime
    try {
      runtime = r
      a
    } finally
      runtime = prev
  }
}
