package japgolly.scalajs.react

import org.scalajs.dom
import japgolly.scalajs.react.internal._

trait Component[-P, CT[-p, +u] <: CtorType[p, u], +Unmounted] {
  val ctor: CT[P, Unmounted]

  // TODO Morph P
  // TODO Morph CT
  // TODO Morph U
}

object Component {

  @inline implicit def toCtorOps[P, CT[-p, +u] <: CtorType[p, u], U](base: Component[P, CT, U]) =
    base.ctor

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  trait Unmounted[+P, +Mounted] {
    def key: Option[Key]
    def ref: Option[String]
    def props: P
    def propsChildren: PropsChildren
    def renderIntoDOM(container: raw.ReactDOM.Container, callback: Callback = Callback.empty): Mounted

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

  trait HasEffect[F[+_]] {
    def withEffect[G[+_]](implicit t: Effect.Trans[F, G]): HasEffect[G]

    def withUntypedEffects(implicit t: Effect.Trans[F, Effect.Id]): HasEffect[Effect.Id] =
      withEffect
  }

  trait Props[F[+_], +P] extends HasEffect[F] {
    def props: F[P]
    def propsChildren: F[PropsChildren]
    def mapProps[X](f: P => X): Props[F, X]
    def withEffect[G[+_]](implicit t: Effect.Trans[F, G]): Props[G, P]
  }

  trait State[F[+_], S] extends HasEffect[F] {
    def state: F[S]
    def setState(newState: S, callback: Callback = Callback.empty): F[Unit]
    def modState(mod: S => S, callback: Callback = Callback.empty): F[Unit]
    def xmapState[X](f: S => X)(g: X => S): State[F, X]
    def zoomState[X](get: S => X)(set: (S, X) => S): State[F, X]
    def withEffect[G[+_]](implicit t: Effect.Trans[F, G]): State[G, S]
  }

  trait Mounted[F[+_], +P, S] extends Props[F, P] with State[F, S] {
    protected implicit def F: Effect[F]

    def isMounted: F[Boolean]
    def getDOMNode: F[dom.Element]
    def forceUpdate(callback: Callback = Callback.empty): F[Unit]

    override def mapProps[X](f: P => X): Mounted[F, X, S] =
      Mounted.Mapped(this)(f, identity)((_, s) => s)

    override def xmapState[X](f: S => X)(g: X => S): Mounted[F, P, X] =
      zoomState(f)((_, s) => g(s))

    override def zoomState[X](get: S => X)(set: (S, X) => S): Mounted[F, P, X] =
      Mounted.Mapped(this)(identity, get)(set)

    override def withEffect[G[+_]](implicit t: Effect.Trans[F, G]): Mounted[G, P, S] =
      Mounted.WithEffect(this)(t)
  }

  object Mounted {

    @inline def WithEffect[F0[+_], F[+_], P, S](delegate: Mounted[F0, P, S])
                                             (implicit t: Effect.Trans[F0, F]): WithEffect[F0, F, P, S] =
      // TODO Could check if trans is Trans.Id and if so, just return delegate
      new WithEffect(delegate, t)

    final class WithEffect[F0[+_], F[+_], P, S](val delegate: Mounted[F0, P, S],
                                              t: Effect.Trans[F0, F]) extends Mounted[F, P, S] {
      protected implicit def F = t.to
      override def props                                             = t(delegate.props)
      override def propsChildren                                     = t(delegate.propsChildren)
      override def state                                             = t(delegate.state)
      override def setState(s: S, c: Callback = Callback.empty)      = t(delegate.setState(s, c))
      override def modState(f: S => S, c: Callback = Callback.empty) = t(delegate.modState(f, c))
      override def isMounted                                         = t(delegate.isMounted)
      override def getDOMNode                                        = t(delegate.getDOMNode)
      override def forceUpdate(c: Callback = Callback.empty)         = t(delegate.forceUpdate(c))

      override def withEffect[G[+_]](implicit t2: Effect.Trans[F, G]): Mounted[G, P, S] =
        Mounted.WithEffect(delegate)(t compose t2)
    }


    @inline def Mapped[F[+_], P0, P, S0, S](delegate: Mounted[F, P0, S0])
                                          (pMap: P0 => P, sGet: S0 => S)
                                          (sSet: (S0, S) => S0): Mapped[F, P0, P, S0, S] =
      new Mapped(delegate, pMap, sGet, sSet)

    final class Mapped[F[+_], P0, P, S0, S](val delegate: Mounted[F, P0, S0],
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

      override def xmapState[X](f: S => X)(g: X => S): Mounted[F, P, X] =
        Mapped(delegate)(pMap, f compose sGet)((s0, x) => sSet(s0, g(x)))

      override def zoomState[X](get: S => X)(set: (S, X) => S) =
        Mapped(delegate)(pMap, get compose sGet)((s0, x) => sMod(s0, set(_, x)))
    }
  }
}