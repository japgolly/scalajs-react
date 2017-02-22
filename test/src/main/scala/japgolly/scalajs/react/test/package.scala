package japgolly.scalajs.react

import japgolly.scalajs.react.internal.Effect

package object test {

  val Simulate = japgolly.scalajs.react.test.raw.ReactTestUtils.Simulate

  type ReactOrDomNode = japgolly.scalajs.react.test.raw.ReactOrDomNode

  implicit def reactOrDomNodeFromMounted(m: GenericComponent.RawAccessMounted): ReactOrDomNode =
    ReactDOM.raw.findDOMNode(m.raw)

  implicit def reactOrDomNodeFromVRE(m: vdom.ReactElement): ReactOrDomNode =
    m.rawReactElement

  implicit final class ReactTestExt_MountedId(private val c: GenericComponent.BaseMounted[Effect.Id, _, _, _, _]) extends AnyVal {
    def outerHtmlWithoutReactDataAttr(): String =
      ReactTestUtils.removeReactDataAttr(c.getDOMNode.outerHTML)
  }
}
