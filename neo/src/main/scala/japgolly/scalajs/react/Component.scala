package japgolly.scalajs.react

import org.scalajs.dom
import japgolly.scalajs.react.internal._

trait Component[P, CT[_, _] <: CtorType[_, _], Unmounted] {
  val ctor: CT[P, Unmounted]

  // TODO Morph P
  // TODO Morph CT
  // TODO Morph U
}

object Component {

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  trait Unmounted[P, Mounted] {
    def key: Option[Key]
    def ref: Option[String]
    def props: P
    def propsChildren: PropsChildren
    def renderIntoDOM(container: raw.ReactDOM.Container, callback: Callback = Callback.empty): Mounted

    // TODO This should map in mounted too
    def mapProps[PP](f: P => PP): Unmounted[PP, Mounted] =
      Unmounted.Mapped(this)(f,  identity)

    def mapMounted[M](f: Mounted => M): Unmounted[P, M] =
      Unmounted.Mapped(this)(identity, f)
  }

  object Unmounted {
    @inline def Mapped[P0, M0, P, M](delegate: Unmounted[P0, M0])(pMap: P0 => P, mMap: M0 => M): Mapped[P0, M0, P, M] =
      new Mapped(delegate, pMap, mMap)

    final class Mapped[P0, M0, P, M](val delegate: Unmounted[P0, M0], pMap: P0 => P, mMap: M0 => M) extends Unmounted[P, M] {
      override def key =
        delegate.key

      override def ref =
        delegate.ref

      override def props =
        pMap(delegate.props)

      override def propsChildren =
        delegate.propsChildren

      override def renderIntoDOM(container: raw.ReactDOM.Container, callback: Callback = Callback.empty) =
        mMap(delegate.renderIntoDOM(container, callback))

      override def mapProps[PP](f: P => PP): Unmounted[PP, M] =
        Unmounted.Mapped(delegate)(f compose pMap, mMap)

      override def mapMounted[MM](f: M => MM): Unmounted[P, MM] =
        Unmounted.Mapped(delegate)(pMap, f compose mMap)
    }
  }

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  trait Props[F[_], P] {
    def props: F[P]
    def propsChildren: F[PropsChildren]
    def mapProps[X](f: P => X): Props[F, X]
    // TODO Morph F
  }

  trait State[F[_], S] {
    def state: F[S]
    def setState(newState: S, callback: Callback = Callback.empty): F[Unit]
    def modState(mod: S => S, callback: Callback = Callback.empty): F[Unit]
    def zoomState[X](get: S => X)(set: (S, X) => S): State[F, X]
    // TODO Morph F
  }

  trait Mounted[F[_], P, S] extends Props[F, P] with State[F, S] {
    protected implicit def F: Effect[F]

    def isMounted: F[Boolean]
    def getDOMNode: F[dom.Element]
    def forceUpdate(callback: Callback = Callback.empty): F[Unit]

    override def mapProps[X](f: P => X): Mounted[F, X, S] =
      Mounted.Mapped(this)(f, identity)((_, s) => s)

    override def zoomState[X](get: S => X)(set: (S, X) => S): Mounted[F, P, X] =
      Mounted.Mapped(this)(identity, get)(set)

    // TODO Morph F
  }

  object Mounted {

    @inline def Mapped[F[_], P0, P, S0, S](delegate: Mounted[F, P0, S0])
                                          (pMap: P0 => P, sGet: S0 => S)
                                          (sSet: (S0, S) => S0): Mapped[F, P0, P, S0, S] =
      new Mapped(delegate, pMap, sGet, sSet)


    final class Mapped[F[_], P0, P, S0, S](val delegate: Mounted[F, P0, S0],
                                           pMap: P0 => P,
                                           sGet: S0 => S,
                                           sSet: (S0, S) => S0) extends Mounted[F, P, S] {
      override protected implicit def F = delegate.F

      protected def sMod(s0: S0, f: S => S): S0 =
        sSet(s0, f(sGet(s0)))

      override def props =
        F.map(delegate.props)(pMap)

      override def propsChildren =
        delegate.propsChildren

      override def state =
        F.map(delegate.state)(sGet)

      override def setState(newState: S, callback: Callback = Callback.empty) =
        delegate.modState(sSet(_, newState), callback)

      override def modState(mod: S => S, callback: Callback = Callback.empty) =
        delegate.modState(sMod(_, mod), callback)

      override def isMounted =
        delegate.isMounted

      override def getDOMNode =
        delegate.getDOMNode

      override def forceUpdate(callback: Callback = Callback.empty) =
        delegate.forceUpdate(callback)

      override def mapProps[X](f: P => X) =
        Mapped(delegate)(f compose pMap, sGet)(sSet)

      override def zoomState[X](get: S => X)(set: (S, X) => S) =
        Mapped(delegate)(pMap, get compose sGet)((s0, x) => sMod(s0, set(_, x)))
    }
  }
}