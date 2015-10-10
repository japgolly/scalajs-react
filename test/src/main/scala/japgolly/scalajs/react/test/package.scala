package japgolly.scalajs.react

import org.scalajs.dom
import scala.scalajs.js.{Object, UndefOr}

package object test {

  sealed trait ComponentClass extends Object
  @inline final implicit def autoComponentClassFromScalaComponent(c: ReactComponentC[_, _, Any, TopNode]): ComponentClass =
    c.reactClass.asInstanceOf[ComponentClass]

  final type ComponentM = ReactComponentM_[TopNode]

  sealed trait ReactOrDomNode extends Object
  @inline final implicit def autoReactOrDomNodeN(n: TopNode): ReactOrDomNode =
    n.asInstanceOf[ReactOrDomNode]
  @inline final implicit def autoReactOrDomNodeU(c: ReactElement): ReactOrDomNode =
    c.asInstanceOf[ReactOrDomNode]
  @inline final implicit def autoReactOrDomNodeM[N <: TopNode](c: ReactComponentM_[N]): ReactOrDomNode =
    c.getDOMNode()

  @inline final implicit def RTUSChangeEventData  (d: ChangeEventData  ): Object = d.toJs
  @inline final implicit def RTUSKeyboardEventData(d: KeyboardEventData): Object = d.toJs
  @inline final implicit def RTUSMouseEventData   (d: MouseEventData   ): Object = d.toJs

  @inline final implicit def autoUnboxRefsInTests[T <: TopNode](r: UndefOr[ReactComponentM_[T]]) = r.get
  @inline final implicit def autoUnboxRefsInTestsC[T <: TopNode](r: UndefOr[ReactComponentM_[T]]): ReactOrDomNode = r.get

//  implicit final class RTUSimulateExt(private val u: Simulate) extends AnyVal {
//    def change(t: ReactOrDomNode, newValue: String) = u.change(t, ChangeEventData(value = newValue))
//  }
}
