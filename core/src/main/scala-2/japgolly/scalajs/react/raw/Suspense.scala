package japgolly.scalajs.react.raw

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("react", "Suspense", "React.Suspense")
@js.native
object Suspense extends js.Any

@js.native
trait SuspenseProps extends js.Object {
  var fallback: React.Node
}
