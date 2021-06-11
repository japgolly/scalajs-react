package japgolly.scalajs.react.component

import japgolly.scalajs.react.internal.{Iso, Lens}
import japgolly.scalajs.react.util.Util.identityFn
import japgolly.scalajs.react.util.DefaultEffects.{Async => DefaultA}
import japgolly.scalajs.react.util.SafeEffect
import japgolly.scalajs.react.util.UnsafeEffect
import japgolly.scalajs.react.util.UnsafeEffect.Id

object Template {

  abstract class MountedWithRoot[F[_], A[_], P0, S0]
      (implicit ft: UnsafeEffect.Sync.Trans[Id, F], at: SafeEffect.Async.Trans[DefaultA, A])
      extends Generic.MountedWithRoot[F, A, P0, S0, P0, S0] {

    type Mapped[F1[_], A1[_], P1, S1] <: Generic.MountedWithRoot[F1, A1, P1, S1, P0, S0]
    protected def mapped[F1[_], A1[_], P1, S1](mp: P0 => P1, ls: Lens[S0, S1])
                                              (implicit ft: UnsafeEffect.Sync.Trans[Id, F1], at: SafeEffect.Async.Trans[DefaultA, A1]): Mapped[F1, A1, P1, S1]

    override type WithEffect[F1[_]]      = Mapped[F1, A, P0, S0]
    override type WithAsyncEffect[A1[_]] = Mapped[F, A1, P0, S0]
    override type WithMappedProps[P1]    = Mapped[F, A, P1, S0]
    override type WithMappedState[S1]    = Mapped[F, A, P0, S1]

    override def mapProps[P1](f: P0 => P1) =
      mapped(f, Lens.id)

    override def xmapState[S1](f: S0 => S1)(g: S1 => S0) =
      mapped(identityFn, Iso(f)(g).toLens)

    override def zoomState[S1](get: S0 => S1)(set: S1 => S0 => S0) =
      mapped(identityFn, Lens(get)(set))

    override def withEffect[F1[_]](implicit f1: UnsafeEffect.Sync.Trans[F, F1]) =
      mapped(identityFn, Lens.id)(ft compose f1, at)

    override def withAsyncEffect[A1[_]](implicit a1: SafeEffect.Async.Trans[A, A1]) =
      mapped(identityFn, Lens.id)(ft, at compose a1)
  }

  abstract class MountedMapped[F[_], A[_], P2, S2, P1, S1, P0, S0]
      (from: Generic.MountedWithRoot[Id, DefaultA, P1, S1, P0, S0])(mp: P1 => P2, ls: Lens[S1, S2])
      (implicit ft: UnsafeEffect.Sync.Trans[Id, F], at: SafeEffect.Async.Trans[DefaultA, A])
      extends Generic.MountedWithRoot[F, A, P2, S2, P0, S0] {

    type Mapped[F3[_], A3[_], P3, S3] <: Generic.MountedWithRoot[F3, A3, P3, S3, P0, S0]
    protected def mapped[F3[_], A3[_], P3, S3](mp: P1 => P3, ls: Lens[S1, S3])(implicit ft: UnsafeEffect.Sync.Trans[Id, F3], at: SafeEffect.Async.Trans[DefaultA, A3]): Mapped[F3, A3, P3, S3]

    override type WithEffect[F3[_]]      = Mapped[F3, A, P2, S2]
    override type WithAsyncEffect[A3[_]] = Mapped[F, A3, P2, S2]
    override type WithMappedProps[P3]    = Mapped[F, A, P3, S2]
    override type WithMappedState[S3]    = Mapped[F, A, P2, S3]

    override implicit def F    = ft.to
    override implicit def A    = at.to
    override def getDOMNode    = ft apply from.getDOMNode
    override def propsChildren = ft apply from.propsChildren
    override def props         = ft apply mp(from.props)
    override def state         = ft apply ls.get(from.state)

    override def forceUpdate[G[_], B](callback: => G[B])(implicit G: SafeEffect.Sync[G]) =
      ft apply from.forceUpdate(callback)

    override def setState[G[_], B](s: State, callback: => G[B])(implicit G: SafeEffect.Sync[G]) =
      ft apply from.modState(ls set s, callback)

    override def modState[G[_], B](f: State => State, callback: => G[B])(implicit G: SafeEffect.Sync[G]) =
      ft apply from.modState(ls mod f, callback)

    override def modState[G[_], B](f: (State, Props) => State, callback: => G[B])(implicit G: SafeEffect.Sync[G]) =
      ft apply from.modState((s1, p1) => ls.set(f(ls.get(s1), mp(p1)))(s1), callback)

    override def setStateOption[G[_], B](o: Option[State], callback: => G[B])(implicit G: SafeEffect.Sync[G]) =
      o.fold(ft apply from.setStateOption(None, callback))(setState(_, callback))

    override def modStateOption[G[_], B](f: State => Option[State], callback: => G[B])(implicit G: SafeEffect.Sync[G]) =
      ft apply from.modStateOption(ls modO f, callback)

    override def modStateOption[G[_], B](f: (State, Props) => Option[State], callback: => G[B])(implicit G: SafeEffect.Sync[G]) =
      ft apply from.modStateOption((s1, p1) => f(ls.get(s1), mp(p1)).map(ls.set(_)(s1)), callback)

    override def mapProps[P3](f: P2 => P3) =
      mapped(f compose mp, ls)

    override def xmapState[S3](f: S2 => S3)(g: S3 => S2) =
      mapped(mp, ls --> Iso(f)(g))

    override def zoomState[S3](get: S2 => S3)(set: S3 => S2 => S2) =
      mapped(mp, ls --> Lens(get)(set))

    override def withEffect[F2[_]](implicit t: UnsafeEffect.Sync.Trans[F, F2]) =
      mapped(mp, ls)(ft compose t, at)

    override def withAsyncEffect[A2[_]](implicit t: SafeEffect.Async.Trans[A, A2]) =
      mapped(mp, ls)(ft, at compose t)
  }

}
