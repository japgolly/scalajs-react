package japgolly.scalajs.react

package object test {

  val Simulate = japgolly.scalajs.react.test.raw.ReactAddonsTestUtils.Simulate

  type ReactOrDomNode = japgolly.scalajs.react.test.raw.ReactOrDomNode

  implicit def reactOrDomNodeFromMounted(m: GenericComponent.MountedRaw): ReactOrDomNode =
    ReactDOM.raw.findDOMNode(m.raw)

  implicit def reactOrDomNodeFromVRE(m: vdom.VdomElement): ReactOrDomNode =
    m.rawElement

  implicit final class ReactTestExt_MountedId(private val c: GenericComponent.MountedImpure[_, _]) extends AnyVal {
    def outerHtmlScrubbed(): String =
      ReactTestUtils.removeReactInternals(c.getDOMNode.outerHTML)
  }
}
