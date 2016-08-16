package japgolly.scalajs

import scala.scalajs.js.|

package object react extends ReactEventTypes {

  type Callback = CallbackTo[Unit]

  // Same as raw.Key except its non-null
  type Key = String | Boolean | raw.JsNumber

  type Ref = String // TODO Ummm.....

  // TODO Rename?
  type BackendScope[P, S] = ScalaComponent.BackendScope[P, S]
}
