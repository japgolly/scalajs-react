package japgolly.scalajs.react.facade

import scala.scalajs.js

@js.native
trait SecretInternals extends js.Object {

  @deprecated("Removed in React 18", "3.0.0")
  final val SchedulerTracing: SchedulerTracing = js.native
}

@deprecated("Removed in React 18", "3.0.0")
@js.native
trait SchedulerTracing extends js.Object {

  def unstable_trace[A](name: String, timestamp: Double, callback: js.Function0[A]): A
}
