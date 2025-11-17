package japgolly.scalajs.react.testing_library.dom.facade

import scala.scalajs.js
import scala.scalajs.js.annotation._

// @JSImport("@testing-library/dom", "fireEvent")
@JSGlobal("TestingLibraryDom")
@js.native
object TestingLibraryDom extends js.Object {
  val fireEvent: FireEvent = js.native
}
