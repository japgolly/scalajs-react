package japgolly.scalajs.react.test

import scalajs.js
import scalajs.js.annotation.JSImport

/**
  * Scala.JS currently doesn't support annotating facades for both script- and module-style output.
  * The default in scalajs-react remains script-style;
  * users who use module-style and webpack/scalajs-bundler should simply reference the relevant objects below so that
  * webpack 1) imports them, and 2) exposes them correctly.
  */
@deprecated("No replacement needed. Facades now support modules directly.", "1.0.1")
object WebpackRequire {

  @inline def main = japgolly.scalajs.react.WebpackRequire

  @JSImport("expose-loader?React.addons.TestUtils!react-addons-test-utils", JSImport.Namespace)
  @js.native
  object ReactTestUtils extends js.Any

}
