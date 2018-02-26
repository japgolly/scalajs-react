package japgolly.scalajs.react

import japgolly.scalajs.react.component.Generic.MountedDomNode

package object test {

  val Simulate = japgolly.scalajs.react.test.raw.ReactTestUtils.Simulate

  type ReactOrDomNode = japgolly.scalajs.react.test.raw.ReactOrDomNode

  implicit def reactOrDomNodeFromMounted(m: GenericComponent.MountedRaw): ReactOrDomNode =
    MountedDomNode(ReactDOM.raw.findDOMNode(m.raw)).asElement

  implicit def reactOrDomNodeFromVRE(m: vdom.VdomElement): ReactOrDomNode =
    m.rawElement

  implicit final class ReactTestExt_MountedId(private val c: GenericComponent.MountedImpure[_, _]) extends AnyVal {
    def outerHtmlScrubbed(): String =
      ReactTestUtils.removeReactInternals(c.getDOMNode.asElement.outerHTML)
  }
}
