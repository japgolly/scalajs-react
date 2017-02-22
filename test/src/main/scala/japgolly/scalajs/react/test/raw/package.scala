package japgolly.scalajs.react.test

import scalajs.js.|
import japgolly.scalajs.react.vdom.TopNode
import japgolly.scalajs.react.raw.ReactElement

package object raw {

  type ReactOrDomNode = TopNode | ReactElement

}
