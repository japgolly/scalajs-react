package japgolly.scalajs.react.test

import japgolly.scalajs.react.facade.{React, ReactDOM}
import scala.scalajs.js.|

package object facade {

  type ReactOrDomNode = ReactDOM.DomNode | React.Element

}
