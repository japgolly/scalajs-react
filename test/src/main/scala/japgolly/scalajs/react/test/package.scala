package japgolly.scalajs.react

import org.scalajs.dom.HTMLElement

package object test {
  @inline final def ReactTestUtils = React.addons.TestUtils.asInstanceOf[ReactTestUtils]

  final type ComponentClass = ComponentConstructor_
  final type ComponentM = ReactComponentM[HTMLElement]
}
