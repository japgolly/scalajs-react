package japgolly.scalajs.react.facade

import scala.scalajs.js

@deprecated("Removed in React 18", "2.2.0")
@js.native
trait SecretInternals extends js.Object {
  final val SchedulerTracing: SchedulerTracing = js.native
}

@deprecated("Removed in React 18", "2.2.0")
@js.native
trait SchedulerTracing extends js.Object {

  def unstable_trace[A](name: String, timestamp: Double, callback: js.Function0[A]): A
}
