package japgolly.scalajs.react.component

import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.vdom
import japgolly.scalajs.react.{raw => Raw}
import japgolly.scalajs.react.{Callback, CtorType, Key, PropsChildren}
import org.scalajs.dom

// TODO Type variance later

object Generic {

  type Component[P, CT[-p, +u] <: CtorType[p, u], U] = Component0[P, CT, U, P, CT, U]
  type Unmounted[P, M]                               = Unmounted0[P, M, P, M]
  type Mounted[F[+_], P, S]                          = Mounted0[F, P, S, P, S]

  trait Component0[
      P1, CT1[-p, +u] <: CtorType[p, u], U1,
      P0, CT0[-p, +u] <: CtorType[p, u], U0] {

    final type Props = P1
    final type Unmounted = U1

    def underlying: Component[P0, CT0, U0]
    val ctor: CT1[P1, U1]

    // map P CT U
    // def mapCtor[P2, CT2[-p, +u] <: CtorType[p, u], U2](f: CT1[P1, U1] => CT2[P2, U2]): Component0[P2, CT2, U2, P0, CT0, U0]
  }

  trait Unmounted0[
      P1, M1,
      P0, M0] {

    final type Props = P1
    final type Mounted = M1

    def underlying: Unmounted[P0, M0]

    def reactElement: vdom.ReactElement
    def key: Option[Key]
    def ref: Option[String]
    def props: Props
    def propsChildren: PropsChildren

    def renderIntoDOM(container: Raw.ReactDOM.Container, callback: Callback = Callback.empty): Mounted
  }

  trait Mounted0[
      F[+_],
      P1, S1,
      P0, S0] {

    final type Props = P1
    final type State = S1

    def underlying: Mounted[F, P0, S0]

    protected implicit def F: Effect[F]

    def isMounted: F[Boolean]
    def getDOMNode: F[dom.Element]
    def forceUpdate(callback: Callback = Callback.empty): F[Unit]

    def props: F[Props]
    def propsChildren: F[PropsChildren]

    def state: F[State]
    def setState(newState: State, callback: Callback = Callback.empty): F[Unit]
    def modState(mod: State => State, callback: Callback = Callback.empty): F[Unit]
  }

}

