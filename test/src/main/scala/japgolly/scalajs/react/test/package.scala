package japgolly.scalajs.react

import scala.scalajs.js.Object

package object test {

  final type ComponentClass = ComponentConstructor_
  final type ComponentM = ReactComponentM_[TopNode]

  @inline final implicit def RTUSChangeEventData  (d: ChangeEventData  ): Object = d.toJs
  @inline final implicit def RTUSKeyboardEventData(d: KeyboardEventData): Object = d.toJs
  @inline final implicit def RTUSMouseEventData   (d: MouseEventData   ): Object = d.toJs

//  implicit final class RTUSimulateExt(val u: Simulate) extends AnyVal {
//    def change(t: ComponentOrNode, newValue: String) = u.change(t, ChangeEventData(value = newValue))
//  }
}
