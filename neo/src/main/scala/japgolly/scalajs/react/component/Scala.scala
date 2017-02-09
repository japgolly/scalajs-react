package japgolly.scalajs.react.component

import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.{Callback, CallbackTo, ChildrenArg, CtorType, PropsChildren, raw => Raw}
import scala.scalajs.js

object Scala {

  type Component[P, S, B, CT[-p, +u] <: CtorType[p, u]] =
    Js.MappedComponent[Effect.Id, P, S, CT, Js.RawMounted with Vars[P, S, B], Box[P], Box[S], CT]

  type Unmounted   [P, S, B] = Generic.Unmounted[P, Mounted[P, S, B]]
  type Mounted     [P, S, B] = RootMounted[Effect.Id, P, S, B]
  type MountedCB   [P, S, B] = RootMounted[CallbackTo, P, S, B]
  type BackendScope[P, S]    = Generic.Mounted[CallbackTo, P, S]

  type JsComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]] =
    Js.ComponentPlusFacade[Box[P], Box[S], CT, Vars[P, S, B]]

  type JsMounted[P, S, B] =
    Js.MountedPlusFacade[Box[P], Box[S], Vars[P, S, B]]

  @js.native
  trait Vars[P, S, B] extends js.Object {
    var mounted  : Mounted[P, S, B]
    var mountedCB: MountedCB[P, S, B]
    var backend  : B
  }

  def apply[P, S, B, CT[-p, +u] <: CtorType[p, u]](js: JsComponent[P, S, B, CT]): Component[P, S, B, CT] =
    js.xmapProps(_.unbox)(Box(_))
      .xmapState(_.unbox)(Box(_))

  // ===================================================================================================================

  type RootMounted[F[+_], P, S, B] = BaseMounted[F, P, S, B, P, S]

  sealed trait BaseMounted[F[+_], P1, S1, B, P0, S0] extends Generic.BaseMounted[F, P1, S1, P0, S0] {
    override final type Root = RootMounted[F, P0, S0, B]
    override def mapProps[P2](f: P1 => P2): BaseMounted[F, P2, S1, B, P0, S0]
    override def xmapState[S2](f: S1 => S2)(g: S2 => S1): BaseMounted[F, P1, S2, B, P0, S0]
    override def zoomState[S2](get: S1 => S2)(set: S2 => S1 => S1): BaseMounted[F, P1, S2, B, P0, S0]
    override def withEffect[F2[+_]](implicit t: Effect.Trans[F, F2]): BaseMounted[F2, P1, S1, B, P0, S0]

    val js: JsMounted[P0, S0, B]

    // B instead of F[B] because
    // 1. Builder takes a MountedCB but needs immediate access to this.
    // 2. It never changes once initialised.
    // Note: Keep this is def instead of val because the builder sets it after creation.
    final def backend: B =
      js.raw.backend
  }

  // TODO so much copy-paste, maybe use type families again?
  def rootMounted[P, S, B](x: JsMounted[P, S, B]): RootMounted[Effect.Id, P, S, B] =
    new RootMounted[Effect.Id, P, S, B] {

      override def root = this

      override val js = x

      override implicit def F = Effect.idInstance

      override def isMounted =
        x.isMounted

      override def props =
        x.props.unbox

      override def propsChildren =
        x.propsChildren

      override def state: S =
        x.state.unbox

      override def setState(newState: S, callback: Callback = Callback.empty) =
        x.setState(Box(newState), callback)

      override def modState(mod: S => S, callback: Callback = Callback.empty) =
        x.modState(s => Box(mod(s.unbox)), callback)

      override def getDOMNode =
        x.getDOMNode

      override def forceUpdate(callback: Callback = Callback.empty) =
        x.forceUpdate(callback)

      override def mapProps[P2](f: P => P2) =
        mappedM(this)(f, Lens.id)

      override def xmapState[S2](f: S => S2)(g: S2 => S) =
        mappedM(this)(identity, Iso(f)(g).toLens)

      override def zoomState[S2](get: S => S2)(set: S2 => S => S) =
        mappedM(this)(identity, Lens(get)(set))

      override def withEffect[F[+_]](implicit t: Effect.Trans[Effect.Id, F]) =
        mappedM(this)(identity, Lens.id)
    }

  // TODO so much copy-paste
  private def mappedM[F[+_], P2, S2, P1, S1, B, P0, S0]
      (from: BaseMounted[Effect.Id, P1, S1, B, P0, S0])
      (mp: P1 => P2, ls: Lens[S1, S2])
      (implicit ft: Effect.Trans[Effect.Id, F])
      : BaseMounted[F, P2, S2, B, P0, S0] =
    new BaseMounted[F, P2, S2, B, P0, S0] {
      override implicit def F    = ft.to
      override def root          = from.root.withEffect[F]
      override val js            = from.js
      override def isMounted     = ft apply from.isMounted
      override def getDOMNode    = ft apply from.getDOMNode
      override def propsChildren = ft apply from.propsChildren
      override def props         = ft apply mp(from.props)
      override def state         = ft apply ls.get(from.state)

      override def forceUpdate(callback: Callback = Callback.empty) =
        ft apply from.forceUpdate(callback)

      override def setState(s: State, callback: Callback = Callback.empty) =
        ft apply from.modState(ls set s, callback)

      override def modState(f: State => State, callback: Callback = Callback.empty) =
        ft apply from.modState(ls mod f, callback)

      override def mapProps[P3](f: P2 => P3) =
        mappedM(from)(f compose mp, ls)

      override def xmapState[S3](f: S2 => S3)(g: S3 => S2) =
        mappedM(from)(mp, ls --> Iso(f)(g))

      override def zoomState[S3](get: S2 => S3)(set: S3 => S2 => S2) =
        mappedM(from)(mp, ls --> Lens(get)(set))

      override def withEffect[F2[+_]](implicit t: Effect.Trans[F, F2]) =
        mappedM(from)(mp, ls)(ft compose t)
    }
}
