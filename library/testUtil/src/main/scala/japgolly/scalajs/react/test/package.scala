package japgolly.scalajs.react

import japgolly.scalajs.react.util.DefaultEffects.Sync
import scala.scalajs.js.|

package object test {

  type ReactOrDomNode = facade.ReactDOM.DomNode | facade.React.Element

  type ReactTestVar[A] = ReactTestVarF[Sync, A]

  object ReactTestVar {
    @inline def apply[A](a: A): ReactTestVar[A] =
      ReactTestVarF(a)
  }

  implicit def reactOrDomNodeFromVRE(m: vdom.VdomElement): ReactOrDomNode =
    m.rawElement

}
