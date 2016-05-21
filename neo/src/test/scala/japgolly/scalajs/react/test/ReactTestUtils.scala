package japgolly.scalajs.react.test

import japgolly.scalajs.react.raw._
import scalajs.js.annotation.JSName
import scalajs.js

/** https://facebook.github.io/react/docs/test-utils.html */
@js.native
@JSName("React.addons.TestUtils")
object ReactTestUtils extends ReactTestUtils

@js.native
trait ReactTestUtils extends js.Object {

  def renderIntoDocument(element: ReactElement): ReactComponent = js.native
}
