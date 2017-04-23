package japgolly.scalajs.react.raw

import scala.scalajs.js
import scala.scalajs.js.annotation._

@JSGlobal("React.Component")
@js.native
abstract class ReactComponentEs6[P <: js.Object, S <: js.Object](ctorProps: P = js.native) extends ReactComponent[P, S] {

  def componentWillMount       ()                                  : Unit    = js.native
  def componentWillUnmount     ()                                  : Unit    = js.native
  def componentDidMount        ()                                  : Unit    = js.native
  def componentWillReceiveProps(nextProps: Props)                  : Unit    = js.native
  def componentWillUpdate      (nextProps: Props, nextState: State): Unit    = js.native
  def componentDidUpdate       (prevProps: Props, prevState: State): Unit    = js.native
  def shouldComponentUpdate    (nextProps: Props, nextState: State): Boolean = js.native

  // These are all defined in the super class:
  //  def props: Props = js.native
  //  var state: State = js.native
  //  def render(): ReactElement
  //  override def forceUpdate(callback: js.Function0[Unit] = js.native): Unit = js.native
  //  override def replaceState(newState: State, callback: js.Function0[Unit] = js.native): Unit = js.native
  //  override def setState(partialState: js.Object, callback: js.Function0[Unit] = js.native): Unit = js.native
  //  @JSName("setState") override def modState(fn: js.Function1[State, js.Object], callback: js.Function0[Unit] = js.native): Unit = js.native
}
