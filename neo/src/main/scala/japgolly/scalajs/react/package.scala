package japgolly.scalajs

import scala.scalajs.js.|

package object react extends ReactEventTypes {

  type Callback = CallbackTo[Unit]

  type Key = String | Boolean | raw.JsNumber

  type Ref = String // TODO Ummm.....

  // TODO Rename?
  type BackendScope[P, S] = ScalaComponent.BackendScope[P, S]
}
