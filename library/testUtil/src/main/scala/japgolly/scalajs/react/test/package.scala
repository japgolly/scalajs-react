package japgolly.scalajs.react

import japgolly.scalajs.react.internal.CoreGeneral._
import japgolly.scalajs.react.util.DefaultEffects.Sync

package object test
    extends japgolly.scalajs.react.test.internal.ReactTestUtilExtensions {

  type ReactOrDomNode = japgolly.scalajs.react.test.facade.ReactOrDomNode

  type ReactTestVar[A] = ReactTestVarF[Sync, A]

  object ReactTestVar {
    @inline def apply[A](a: A): ReactTestVar[A] =
      ReactTestVarF(a)
  }

  @deprecated("Class-based components are now a legacy feature. Upgrade to hook-based components.", "3.0.0 / React v18")
  implicit def reactOrDomNodeFromMounted(m: GenericComponent.MountedRaw): ReactOrDomNode =
    ReactDOM.findDOMNode(m.raw).get.raw

  implicit def reactOrDomNodeFromVRE(m: vdom.VdomElement): ReactOrDomNode =
    m.rawElement

}
