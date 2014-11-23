package japgolly.scalajs.react

package object vdom {

  @deprecated("React 0.12 has introduced ReactElement which is what ReactOutput was created to represent. Replace ReactOutput with ReactElement.", "0.6.0")
  type ReactOutput = ReactElement

  type ReactFragT = ReactNode
}
