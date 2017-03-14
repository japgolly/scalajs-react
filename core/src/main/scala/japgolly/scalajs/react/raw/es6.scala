package japgolly.scalajs.react.raw

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

@JSName("React.Component")
@js.native
abstract class Es6Component[P <: js.Object, S <: js.Object](ctorProps: P = js.native) extends js.Object {

  final type Props = P with PropsWithChildren
  final type State = S

  def props: Props = js.native

  var state: State = js.native

  def render(): ReactElement

  def componentWillMount       ()                  : Unit    = js.native
  def componentWillUnmount     ()                  : Unit    = js.native
  def componentDidMount        ()                  : Unit    = js.native
  def componentWillReceiveProps(p: Props)          : Unit    = js.native
  def componentWillUpdate      (p: Props, s: State): Unit    = js.native
  def componentDidUpdate       (p: Props, s: State): Unit    = js.native
  def shouldComponentUpdate    (p: Props, s: State): Boolean = js.native

  def forceUpdate(callback: js.Function0[Unit] = js.native): Unit = js.native
  def replaceState(newState: State, callback: js.Function0[Unit] = js.native): Unit = js.native
  def setState(partialState: js.Object, callback: js.Function0[Unit] = js.native): Unit = js.native
  @JSName("setState") def modState(fn: js.Function1[State, js.Object], callback: js.Function0[Unit] = js.native): Unit = js.native
}
