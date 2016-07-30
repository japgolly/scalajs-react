package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{raw => Raw}

sealed class ReactNode(raw: Raw.ReactNode) extends TagMod {
  override def applyTo(b: Builder): Unit =
    b.appendChild(raw)
}

object ReactNode {
  @inline def apply(n: Raw.ReactNode): ReactNode =
    new ReactNode(n)

  @inline def cast(n: Any): ReactNode =
    new ReactNode(n.asInstanceOf[Raw.ReactNode])
}

// =====================================================================================================================

final class ReactElement(val raw: Raw.ReactElement) extends ReactNode(raw)

object ReactElement {
  @inline def apply(n: Raw.ReactElement): ReactElement =
    new ReactElement(n)
}