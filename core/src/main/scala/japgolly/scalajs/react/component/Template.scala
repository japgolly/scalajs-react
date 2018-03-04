package japgolly.scalajs.react.component

import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.Callback

object Template {

  abstract class MountedWithRoot[F[_], P0, S0]
      (implicit ft: Effect.Trans[Effect.Id, F])
      extends Generic.MountedWithRoot[F, P0, S0, P0, S0] {

    type Mapped[F1[_], P1, S1] <: Generic.MountedWithRoot[F1, P1, S1, P0, S0]
    protected def mapped[F1[_], P1, S1](mp: P0 => P1, ls: Lens[S0, S1])(implicit ft: Effect.Trans[Effect.Id, F1]): Mapped[F1, P1, S1]

    override type WithEffect[F1[_]]  = Mapped[F1, P0, S0]
    override type WithMappedProps[P1] = Mapped[F, P1, S0]
    override type WithMappedState[S1] = Mapped[F, P0, S1]

    override def mapProps[P1](f: P0 => P1) =
      mapped(f, Lens.id)

    override def xmapState[S1](f: S0 => S1)(g: S1 => S0) =
      mapped(identityFn, Iso(f)(g).toLens)

    override def zoomState[S1](get: S0 => S1)(set: S1 => S0 => S0) =
      mapped(identityFn, Lens(get)(set))

    override def withEffect[F1[_]](implicit t: Effect.Trans[F, F1]) =
      mapped(identityFn, Lens.id)(ft compose t)
  }

  abstract class MountedMapped[F[_], P2, S2, P1, S1, P0, S0]
      (from: Generic.MountedWithRoot[Effect.Id, P1, S1, P0, S0])(mp: P1 => P2, ls: Lens[S1, S2])
      (implicit ft: Effect.Trans[Effect.Id, F])
      extends Generic.MountedWithRoot[F, P2, S2, P0, S0] {

    type Mapped[F3[_], P3, S3] <: Generic.MountedWithRoot[F3, P3, S3, P0, S0]
    protected def mapped[F3[_], P3, S3](mp: P1 => P3, ls: Lens[S1, S3])(implicit ft: Effect.Trans[Effect.Id, F3]): Mapped[F3, P3, S3]

    override type WithEffect[F3[_]]  = Mapped[F3, P2, S2]
    override type WithMappedProps[P3] = Mapped[F, P3, S2]
    override type WithMappedState[S3] = Mapped[F, P2, S3]

    override implicit def F    = ft.to
    override def getDOMNode    = ft apply from.getDOMNode
    override def propsChildren = ft apply from.propsChildren
    override def props         = ft apply mp(from.props)
    override def state         = ft apply ls.get(from.state)

    override def forceUpdate(callback: Callback) =
      ft apply from.forceUpdate(callback)

    override def setState(s: State, callback: Callback) =
      ft apply from.modState(ls set s, callback)

    override def modState(f: State => State, callback: Callback) =
      ft apply from.modState(ls mod f, callback)

    override def modState(f: (State, Props) => State, callback: Callback) =
      ft apply from.modState((s1, p1) => ls.set(f(ls.get(s1), mp(p1)))(s1), callback)

    override def setStateOption(o: Option[State], callback: Callback) =
      o.fold(ft apply from.setStateOption(None, callback))(setState(_, callback))

    override def modStateOption(f: State => Option[State], callback: Callback) =
      ft apply from.modStateOption(ls modO f, callback)

    override def modStateOption(f: (State, Props) => Option[State], callback: Callback) =
      ft apply from.modStateOption((s1, p1) => f(ls.get(s1), mp(p1)).map(ls.set(_)(s1)), callback)

    override def mapProps[P3](f: P2 => P3) =
      mapped(f compose mp, ls)

    override def xmapState[S3](f: S2 => S3)(g: S3 => S2) =
      mapped(mp, ls --> Iso(f)(g))

    override def zoomState[S3](get: S2 => S3)(set: S3 => S2 => S2) =
      mapped(mp, ls --> Lens(get)(set))

    override def withEffect[F2[_]](implicit t: Effect.Trans[F, F2]) =
      mapped(mp, ls)(ft compose t)
  }

}
