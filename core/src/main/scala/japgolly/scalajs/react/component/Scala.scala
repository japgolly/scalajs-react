package japgolly.scalajs.react.component

import scala.scalajs.js
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.{Callback, CallbackTo, Children, CtorType, raw}
import japgolly.scalajs.react.vdom.VdomElement

object Scala {

  def build[P](name: String) =
    new ScalaBuilder.Step1[P](name)

  /** Partially builds a component that always displays the same content, never needs to be redrawn, never needs vdom diffing.
    * The builder is returned and can be customised futher before finally being built.
    */
  def buildStatic(name: String, content: VdomElement): ScalaBuilder.Step4[Unit, Children.None, Unit, Unit] =
    build[Unit](name)
      .renderStatic(content)
      .shouldComponentUpdateConst(false)

  /** Create a component that always displays the same content, never needs to be redrawn, never needs vdom diffing. */
  def static(name: String, content: VdomElement): Component[Unit, Unit, Unit, CtorType.Nullary] =
    buildStatic(name, content).build

  val Lifecycle = ScalaBuilder.Lifecycle

  // ===================================================================================================================

  type Component[P, S, B, CT[-p, +u] <: CtorType[p, u]] =
    Js.BaseComponent[
      P, CT, Unmounted[P, S, B],
      Box[P], CT, JsUnmounted[P, S, B]]

//  type Component[P, S, B, CT[-p, +u] <: CtorType[p, u]] =
//    Js.MappedComponent[Effect.Id, P, S, CT, RawMounted[P, S, B], Box[P], Box[S], CT]

  type Unmounted   [P, S, B] = Js.BaseUnmounted[P, Mounted[P, S, B], Box[P], JsMounted[P, S, B]]
  type Mounted     [P, S, B] = RootMounted[Effect.Id, P, S, B]
  type MountedPure [P, S, B] = RootMounted[CallbackTo, P, S, B]
  type BackendScope[P, S]    = Generic.Mounted[CallbackTo, P, S]

  type JsComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]] = Js.ComponentWithFacade[Box[P], Box[S], CT, Vars[P, S, B]]
  type JsUnmounted[P, S, B]                               = Js.UnmountedWithFacade[Box[P], Box[S],     Vars[P, S, B]]
  type JsMounted  [P, S, B]                               = Js.MountedWithFacade  [Box[P], Box[S],     Vars[P, S, B]]

  type RawMounted[P, S, B] = Js.RawMounted with Vars[P, S, B]

  @js.native
  trait Vars[P, S, B] extends js.Object {
    var mounted    : Mounted    [P, S, B]
    var mountedPure: MountedPure[P, S, B]
    var backend    : B
  }

//  private[this] def sanityCheckCU[P, S, B](c: Component[P, S, B, CtorType.Void]): Unmounted[P, S, B] = c.ctor()
//  private[this] def sanityCheckUM[P, S, B](u: Unmounted[P, S, B]): Mounted[P, S, B] = u.renderIntoDOM(null)

  // ===================================================================================================================

  type RootMounted[F[_], P, S, B] = BaseMounted[F, P, S, B, P, S]

  sealed trait BaseMounted[F[_], P1, S1, B, P0, S0] extends Generic.BaseMounted[F, P1, S1, P0, S0] {
    override final type Root = RootMounted[F, P0, S0, B]
    override final type WithEffect[F2[_]]  = BaseMounted[F2, P1, S1, B, P0, S0]
    override final type WithMappedProps[P2] = BaseMounted[F, P2, S1, B, P0, S0]
    override final type WithMappedState[S2] = BaseMounted[F, P1, S2, B, P0, S0]

    override final type Raw = RawMounted[P0, S0, B]

    val js: JsMounted[P0, S0, B]

    override final def displayName = js.displayName

    // B instead of F[B] because
    // 1. Builder takes a MountedCB but needs immediate access to this.
    // 2. It never changes once initialised.
    // Note: Keep this is def instead of val because the builder sets it after creation.
    final def backend: B =
      js.raw.backend
  }

  def rootMounted[P, S, B](x: JsMounted[P, S, B]): RootMounted[Effect.Id, P, S, B] =
    new Template.RootMounted[Effect.Id, P, S] with RootMounted[Effect.Id, P, S, B] {
      override implicit def F    = Effect.idInstance
      override def root          = this
      override val js            = x
      override val raw           = x.raw
      override def isMounted     = x.isMounted
      override def props         = x.props.unbox
      override def propsChildren = x.propsChildren
      override def state         = x.state.unbox
      override def getDOMNode    = x.getDOMNode

      override def setState(newState: S, callback: Callback = Callback.empty) =
        x.setState(Box(newState), callback)

      override def modState(mod: S => S, callback: Callback = Callback.empty) =
        x.modState(s => Box(mod(s.unbox)), callback)

      override def forceUpdate(callback: Callback) =
        x.forceUpdate(callback)

      override type Mapped[F1[_], P1, S1] = BaseMounted[F1, P1, S1, B, P, S]
      override def mapped[F[_], P1, S1](mp: P => P1, ls: Lens[S, S1])(implicit ft: Effect.Trans[Effect.Id, F]) =
        mappedM(this)(mp, ls)
    }

  private def mappedM[F[_], P2, S2, P1, S1, B, P0, S0]
      (from: BaseMounted[Effect.Id, P1, S1, B, P0, S0])(mp: P1 => P2, ls: Lens[S1, S2])
      (implicit ft: Effect.Trans[Effect.Id, F]): BaseMounted[F, P2, S2, B, P0, S0] =
    new Template.MappedMounted[F, P2, S2, P1, S1, P0, S0](from)(mp, ls) with BaseMounted[F, P2, S2, B, P0, S0] {
      override def root = from.root.withEffect[F]
      override val js = from.js
      override val raw = from.raw
      override type Mapped[F3[_], P3, S3] = BaseMounted[F3, P3, S3, B, P0, S0]
      override def mapped[F3[_], P3, S3](mp: P1 => P3, ls: Lens[S1, S3])(implicit ft: Effect.Trans[Effect.Id, F3]) = mappedM(from)(mp, ls)(ft)
    }

  def mountRaw[P, S, B](x: RawMounted[P, S, B]): Mounted[P, S, B] =
    rootMounted(Js.rootMounted(x))

  // ===================================================================================================================

  def mutableRefTo[P, S, B, CT[-p, +u] <: CtorType[p, u]](c: Component[P, S, B, CT]): MutableRef[P, S, B, CT] =
    new MutableRef(c)

  final class MutableRef[P, S, B, CT[-p, +u] <: CtorType[p, u]](c: Component[P, S, B, CT]) {

    var value: Mounted[P, S, B] = null

    private def refSet: raw.RefFn =
      (i: js.Any) => value =
        if (i == null) null else i.asInstanceOf[RawMounted[P, S, B]].mounted

    val component: CT[P, Unmounted[P, S, B]] =
      CtorType.hackBackToSelf(c.ctor)(c.ctor.withRawProp("ref", refSet))
  }
}
