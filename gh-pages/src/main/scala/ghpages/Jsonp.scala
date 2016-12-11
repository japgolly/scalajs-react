package ghpages

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("./jsonp.js", JSImport.Namespace)
@js.native
object Jsonp extends js.Function2[String, js.Function1[js.Dynamic, Unit], Unit] {
  def apply(url: String, callback: js.Function1[js.Dynamic, Unit]): Unit = js.native
}
