package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{raw => Raw}

sealed class ReactNode(val rawReactNode: Raw.ReactNode) extends TagMod {
  override def applyTo(b: Builder): Unit =
    b.appendChild(rawReactNode)
}

object ReactNode {
  @inline def apply(n: Raw.ReactNode): ReactNode =
    new ReactNode(n)

  @inline def cast(n: Any): ReactNode =
    new ReactNode(n.asInstanceOf[Raw.ReactNode])
}

// =====================================================================================================================

// TODO ReactXxx[raw.Node], ReactXxx[raw.Element]
final class ReactElement(val rawReactElement: Raw.ReactElement) extends ReactNode(rawReactElement)

object ReactElement {
  @inline def apply(n: Raw.ReactElement): ReactElement =
    new ReactElement(n)
}