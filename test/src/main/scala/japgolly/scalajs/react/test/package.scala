package japgolly.scalajs.react

package object test {

  val Simulate = japgolly.scalajs.react.test.raw.ReactTestUtils.Simulate

  type ReactOrDomNode = japgolly.scalajs.react.test.raw.ReactOrDomNode

  implicit def reactOrDomNodeFromMounted(m: GenericComponent.MountedRaw): ReactOrDomNode =
    ReactDOM.findDOMNode(m.raw).get.raw

  implicit def reactOrDomNodeFromVRE(m: vdom.VdomElement): ReactOrDomNode =
    m.rawElement

  implicit final class ReactTestExt_MountedId(private val c: GenericComponent.MountedImpure[_, _]) extends AnyVal {
    def outerHtmlScrubbed(): String =
      c.getDOMNode.asMounted().fold(_.textContent, e => ReactTestUtils.removeReactInternals(e.outerHTML))
  }
}
