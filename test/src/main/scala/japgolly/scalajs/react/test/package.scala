package japgolly.scalajs.react

import org.scalajs.dom
import scala.scalajs.js.{Object, UndefOr}

package object test {

  final type ComponentClass = ReactComponentC_
  final type ComponentM = ReactComponentM_[TopNode]

  sealed trait ComponentOrNode extends Object
  @inline final implicit def autoComponentOrNodeN(n: dom.Node): ComponentOrNode =
    n.asInstanceOf[ComponentOrNode]
  @inline final implicit def autoComponentOrNodeU(c: ReactComponentU_): ComponentOrNode =
    c.asInstanceOf[ComponentOrNode]
  @inline final implicit def autoComponentOrNodeM[N <: TopNode](c: ReactComponentM_[N]): ComponentOrNode =
    c.getDOMNode()

  @inline final implicit def RTUSChangeEventData  (d: ChangeEventData  ): Object = d.toJs
  @inline final implicit def RTUSKeyboardEventData(d: KeyboardEventData): Object = d.toJs
  @inline final implicit def RTUSMouseEventData   (d: MouseEventData   ): Object = d.toJs

  @inline final implicit def autoUnboxRefsInTests[T <: TopNode](r: UndefOr[ReactComponentM_[T]]) = r.get
  @inline final implicit def autoUnboxRefsInTestsC[T <: TopNode](r: UndefOr[ReactComponentM_[T]]): ComponentOrNode = r.get

//  implicit final class RTUSimulateExt(val u: Simulate) extends AnyVal {
//    def change(t: ComponentOrNode, newValue: String) = u.change(t, ChangeEventData(value = newValue))
//  }
}
