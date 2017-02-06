package japgolly.scalajs.react.component

import scala.scalajs.js
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.vdom
import japgolly.scalajs.react.{raw => Raw}
import japgolly.scalajs.react.{Callback, CtorType, Key, PropsChildren}
import org.scalajs.dom

object Js {

  type MountedBasic[F[+_], P <: js.Object, S <: js.Object, R <: RawMounted] = Mounted0[F, P, S, R, P, S]

  type RawMounted = Raw.ReactComponent

  // ===================================================================================================================


  // ===================================================================================================================

  sealed trait Mounted0[F[+_], P, S, R <: RawMounted, P0 <: js.Object, S0 <: js.Object]
      extends Generic.Mounted0[F, P, S, P0, S0] {

    override def underlying: MountedBasic[F, P0, S0, RawMounted]

    val raw: R

    final def addRawType[T <: js.Object]: Mounted0[F, P, S, R with T, P0, S0] =
      this.asInstanceOf[Mounted0[F, P, S, R with T, P0, S0]]
  }

  // ===================================================================================================================

  def mount[P <: js.Object, S <: js.Object](r: RawMounted): MountedBasic[Effect.Id, P, S, RawMounted] =
    new MountedBasic[Effect.Id, P, S, RawMounted] {

    override def underlying = this

    override val raw = r

    override protected implicit def F = Effect.idInstance

    override def props: P =
      raw.props.asInstanceOf[P]

    override def propsChildren =
      PropsChildren(raw.props.children)

    override def state: S =
      raw.state.asInstanceOf[S]

    override def setState(state: S, callback: Callback = Callback.empty): Unit =
      raw.setState(state, callback.toJsFn)

    override def modState(mod: S => S, callback: Callback = Callback.empty): Unit =
      raw.modState(mod.asInstanceOf[js.Object => js.Object], callback.toJsFn)

    override def isMounted =
      raw.isMounted()

    override def getDOMNode: dom.Element =
      Raw.ReactDOM.findDOMNode(raw)

    override def forceUpdate(callback: Callback = Callback.empty): Unit =
      raw.forceUpdate(callback.toJsFn)

//      def getDefaultProps: Props
//      def getInitialState: js.Object | Null
//      def render(): ReactElement
  }

}
