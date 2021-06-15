package japgolly.scalajs.react.component

import japgolly.scalajs.react.internal.{Iso, Lens}
import japgolly.scalajs.react.util.DefaultEffects.{Async => DefaultA}
import japgolly.scalajs.react.util.Effect._
import japgolly.scalajs.react.util.Util.identityFn

object Template {

  abstract class MountedWithRoot[F[_], A[_], P0, S0]
      (implicit ft: UnsafeSync[F], at: Async[A])
      extends Generic.MountedWithRoot[F, A, P0, S0, P0, S0] {

    type Mapped[F1[_], A1[_], P1, S1] <: Generic.MountedWithRoot[F1, A1, P1, S1, P0, S0]
    protected def mapped[F1[_], A1[_], P1, S1](mp: P0 => P1, ls: Lens[S0, S1])
                                              (implicit ft: UnsafeSync[F1], at: Async[A1]): Mapped[F1, A1, P1, S1]

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

    override def withEffect[F1[_]](implicit f1: UnsafeSync[F1]) =
      mapped(identityFn, Lens.id)(f1, at)

    override def withAsyncEffect[A1[_]](implicit a1: Async[A1]) =
      mapped(identityFn, Lens.id)(ft, a1)
  }

  abstract class MountedMapped[F[_], A[_], P2, S2, P1, S1, P0, S0]
      (from: Generic.MountedWithRoot[Id, DefaultA, P1, S1, P0, S0])
      (mp: P1 => P2, ls: Lens[S1, S2])
      (implicit ft: UnsafeSync[F], at: Async[A])
      extends Generic.MountedWithRoot[F, A, P2, S2, P0, S0] {

    type Mapped[F3[_], A3[_], P3, S3] <: Generic.MountedWithRoot[F3, A3, P3, S3, P0, S0]
    protected def mapped[F3[_], A3[_], P3, S3](mp: P1 => P3, ls: Lens[S1, S3])(implicit ft: UnsafeSync[F3], at: Async[A3]): Mapped[F3, A3, P3, S3]

    override type WithEffect[F3[_]]      = Mapped[F3, A, P2, S2]
    override type WithAsyncEffect[A3[_]] = Mapped[F, A3, P2, S2]
    override type WithMappedProps[P3]    = Mapped[F, A, P3, S2]
    override type WithMappedState[S3]    = Mapped[F, A, P2, S3]

    override implicit def F    = ft
    override implicit def A    = at
    override def getDOMNode    = ft.transSync(from.getDOMNode)
    override def propsChildren = ft.transSync(from.propsChildren)
    override def props         = ft.transSync[Id, P2](mp(from.props))
    override def state         = ft.transSync[Id, S2](ls.get(from.state))

    override def forceUpdate[G[_], B](callback: => G[B])(implicit G: Dispatch[G]) =
      ft.transSync(from.forceUpdate(callback))

    override def setState[G[_], B](s: State, callback: => G[B])(implicit G: Dispatch[G]) =
      ft.transSync(from.modState(ls set s, callback))

    override def modState[G[_], B](f: State => State, callback: => G[B])(implicit G: Dispatch[G]) =
      ft.transSync(from.modState(ls mod f, callback))

    override def modState[G[_], B](f: (State, Props) => State, callback: => G[B])(implicit G: Dispatch[G]) =
      ft.transSync(from.modState((s1, p1) => ls.set(f(ls.get(s1), mp(p1)))(s1), callback))

    override def setStateOption[G[_], B](o: Option[State], callback: => G[B])(implicit G: Dispatch[G]) =
      o.fold(ft.transSync(from.setStateOption(None, callback)))(setState(_, callback))

    override def modStateOption[G[_], B](f: State => Option[State], callback: => G[B])(implicit G: Dispatch[G]) =
      ft.transSync(from.modStateOption(ls modO f, callback))

    override def modStateOption[G[_], B](f: (State, Props) => Option[State], callback: => G[B])(implicit G: Dispatch[G]) =
      ft.transSync(from.modStateOption((s1, p1) => f(ls.get(s1), mp(p1)).map(ls.set(_)(s1)), callback))

    override def mapProps[P3](f: P2 => P3) =
      mapped(f compose mp, ls)(ft, at)

    override def xmapState[S3](f: S2 => S3)(g: S3 => S2) =
      mapped(mp, ls --> Iso(f)(g))(ft, at)

    override def zoomState[S3](get: S2 => S3)(set: S3 => S2 => S2) =
      mapped(mp, ls --> Lens(get)(set))(ft, at)

    override def withEffect[F2[_]](implicit t: UnsafeSync[F2]) =
      mapped(mp, ls)(t, at)

    override def withAsyncEffect[A2[_]](implicit t: Async[A2]) =
      mapped(mp, ls)(ft, t)
  }

}
