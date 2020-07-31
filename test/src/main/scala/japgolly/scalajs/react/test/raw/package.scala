package japgolly.scalajs.react.test

import japgolly.scalajs.react.raw.{React, ReactDOM}
import scala.scalajs.js.|

package object raw {

  type ReactOrDomNode = ReactDOM.DomNode | React.Element

}
