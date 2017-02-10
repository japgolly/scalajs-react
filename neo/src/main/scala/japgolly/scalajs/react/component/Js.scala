package japgolly.scalajs.react.component

import org.scalajs.dom
import scala.scalajs.js
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.{Callback, ChildrenArg, CtorType, Key, PropsChildren, vdom, raw => Raw}

object Js extends TemplateForJsBaseComponent[Raw.ReactClass] {

  def apply[P <: js.Object, C <: ChildrenArg, S <: js.Object](name: String)
                                                             (implicit s: CtorType.Summoner[P, C]): Component[P, S, s.CT] = {
    val reactClass = findInScope(name.split('.').toList) match {
      case Some(x: js.Function) => x
      case Some(_)              => throw new IllegalArgumentException(s"React constructor $name is not a function")
      case None                 => throw new IllegalArgumentException(s"React constructor $name is not defined")
    }
    component[P, C, S](reactClass.asInstanceOf[Raw.ReactClass])(s)
  }

  private[this] def findInScope(path: List[String], scope: js.Dynamic = js.Dynamic.global): Option[js.Dynamic] = {
    path match {
      case Nil => Some(scope)
      case name :: tail =>
        val nextScope = scope.selectDynamic(name).asInstanceOf[js.UndefOr[js.Dynamic]].toOption
        nextScope.flatMap(findInScope(tail, _))
    }
  }

  // ===================================================================================================================

  type RawMounted = Raw.ReactComponent

  type Component[P <: js.Object, S <: js.Object, CT[-p, +u] <: CtorType[p, u]] = ComponentWithRawType[P, S, CT, RawMounted]
  type Unmounted[P <: js.Object, S <: js.Object]                               = UnmountedWithRawType[P, S,     RawMounted]
  type Mounted  [P <: js.Object, S <: js.Object]                               = MountedWithRawType  [P, S,     RawMounted]

  type ComponentPlusFacade[P <: js.Object, S <: js.Object, CT[-p, +u] <: CtorType[p, u], R <: js.Object] = ComponentWithRawType[P, S, CT, RawMounted with R]
  type UnmountedPlusFacade[P <: js.Object, S <: js.Object, R <: js.Object]                               = UnmountedWithRawType[P, S,     RawMounted with R]
  type   MountedPlusFacade[P <: js.Object, S <: js.Object, R <: js.Object]                               =   MountedWithRawType[P, S,     RawMounted with R]

  type ComponentWithRawType[P <: js.Object, S <: js.Object, CT[-p, +u] <: CtorType[p, u], R <: RawMounted] = RootComponent[P, CT, UnmountedWithRawType[P, S, R]]
  type UnmountedWithRawType[P <: js.Object, S <: js.Object, R <: RawMounted]                               = RootUnmounted[P,       MountedWithRawType[P, S, R]]
  type   MountedWithRawType[P <: js.Object, S <: js.Object, R <: RawMounted]                               = RootMounted[Effect.Id, P, S, R]

  // ===================================================================================================================

  type RootUnmounted[P <: js.Object, M] = BaseUnmounted[P, M, P, M]

  sealed trait BaseUnmounted[P1, M1, P0 <: js.Object, M0]
      extends Generic.BaseUnmounted[P1, M1, P0, M0] {

    override final type Root = RootUnmounted[P0, M0]
    override def mapUnmountedProps[P2](f: P1 => P2): BaseUnmounted[P2, M1, P0, M0]
    override def mapMounted[M2](f: M1 => M2): BaseUnmounted[P1, M2, P0, M0]

    val raw: Raw.ReactComponentElement
  }

  def rootUnmounted[P <: js.Object, M](r: Raw.ReactComponentElement, m: Raw.ReactComponent => M): RootUnmounted[P, M] =
    new RootUnmounted[P, M] {

      override def root = this

      override val raw = r

      override val reactElement =
        vdom.ReactElement(raw)

      override def key: Option[Key] =
        jsNullToOption(raw.key)

      override def ref: Option[String] =
        jsNullToOption(raw.ref)

      override def props: P =
        raw.props.asInstanceOf[P]

      override def propsChildren: PropsChildren =
        PropsChildren(raw.props.children)

      override def renderIntoDOM(container: Raw.ReactDOM.Container, callback: Callback = Callback.empty): M =
        m(Raw.ReactDOM.render(raw, container, callback.toJsFn))

      override def mapUnmountedProps[P2](f: P => P2) =
        mappedU(this)(f, identity)

      override def mapMounted[M2](f: M => M2) =
        mappedU(this)(identity, f)
    }

  private def mappedU[P2, M2, P1, M1, P0 <: js.Object, M0]
      (from: BaseUnmounted[P1, M1, P0, M0])
      (mp: P1 => P2, mm: M1 => M2)
      : BaseUnmounted[P2, M2, P0, M0] =
    new BaseUnmounted[P2, M2, P0, M0] {
      override def root          = from.root
      override def props         = mp(from.props)
      override val raw           = from.raw
      override def reactElement  = from.reactElement
      override def key           = from.key
      override def ref           = from.ref
      override def propsChildren = from.propsChildren

      override def mapUnmountedProps[P3](f: P2 => P3) =
        mappedU(from)(f compose mp, mm)

      override def mapMounted[M3](f: M2 => M3) =
        mappedU(from)(mp, f compose mm)

      override def renderIntoDOM(container: Raw.ReactDOM.Container, callback: Callback = Callback.empty) =
        mm(from.renderIntoDOM(container, callback))
    }

  // ===================================================================================================================

  type RootMounted[F[+_], P <: js.Object, S <: js.Object, R <: RawMounted] = BaseMounted[F, P, S, R, P, S]

  sealed trait BaseMounted[F[+_], P1, S1, R <: RawMounted, P0 <: js.Object, S0 <: js.Object]
      extends Generic.BaseMounted[F, P1, S1, P0, S0] {

    override final type Root = RootMounted[F, P0, S0, R]
    override def mapProps[P2](f: P1 => P2): BaseMounted[F, P2, S1, R, P0, S0]
    override def xmapState[S2](f: S1 => S2)(g: S2 => S1): BaseMounted[F, P1, S2, R, P0, S0]
    override def zoomState[S2](get: S1 => S2)(set: S2 => S1 => S1): BaseMounted[F, P1, S2, R, P0, S0]
    override def withEffect[F2[+_]](implicit t: Effect.Trans[F, F2]): BaseMounted[F2, P1, S1, R, P0, S0]

    val raw: R

    final def withRawType[R2 <: RawMounted]: BaseMounted[F, P1, S1, R2, P0, S0] =
      this.asInstanceOf[BaseMounted[F, P1, S1, R2, P0, S0]]

    final def addFacade[T <: js.Object]: BaseMounted[F, P1, S1, R with T, P0, S0] =
      withRawType[R with T]

    // def getDefaultProps: Props
    // def getInitialState: js.Object | Null
    // def render(): ReactElement
  }

  def rootMounted[P <: js.Object, S <: js.Object, R <: RawMounted](r: R): RootMounted[Effect.Id, P, S, R] =
    new RootMounted[Effect.Id, P, S, R] {

      override def root = this

      override val raw = r

      override implicit def F = Effect.idInstance

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

      override def mapProps[P2](f: P => P2) =
        mappedM(this)(f, Lens.id)

      override def xmapState[S2](f: S => S2)(g: S2 => S) =
        mappedM(this)(identity, Iso(f)(g).toLens)

      override def zoomState[S2](get: S => S2)(set: S2 => S => S) =
        mappedM(this)(identity, Lens(get)(set))

      override def withEffect[F[+_]](implicit t: Effect.Trans[Effect.Id, F]) =
        mappedM(this)(identity, Lens.id)
    }

  private def mappedM[F[+_], P2, S2, P1, S1, R <: RawMounted, P0 <: js.Object, S0 <: js.Object]
      (from: BaseMounted[Effect.Id, P1, S1, R, P0, S0])
      (mp: P1 => P2, ls: Lens[S1, S2])
      (implicit ft: Effect.Trans[Effect.Id, F])
      : BaseMounted[F, P2, S2, R, P0, S0] =
    new BaseMounted[F, P2, S2, R, P0, S0] {
      override implicit def F    = ft.to
      override def root          = from.root.withEffect[F]
      override val raw           = from.raw
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

  // ===================================================================================================================

  def component[P <: js.Object, C <: ChildrenArg, S <: js.Object](rc: Raw.ReactClass)
                                                                 (implicit s: CtorType.Summoner[P, C]): Component[P, S, s.CT] =
    rootComponent[P, s.CT, Unmounted[P, S]](rc, s.pf.rmap(s.summon(rc))(unmounted))(s.pf)

  def unmounted[P <: js.Object, S <: js.Object](r: Raw.ReactComponentElement): Unmounted[P, S] =
    rootUnmounted(r, mounted)

  def mounted[P <: js.Object, S <: js.Object](r: RawMounted): Mounted[P, S] =
    rootMounted(r)

  // ===================================================================================================================

  type MappedComponent[F[+_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: RawMounted, P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]] =
    BaseComponent[P1, CT1, MappedUnmounted[F, P1, S1, R, P0, S0], P0, CT0, UnmountedWithRawType[P0, S0, R]]

  type MappedUnmounted[F[+_], P1, S1, R <: RawMounted, P0 <: js.Object, S0 <: js.Object] =
    BaseUnmounted[P1, MappedMounted[F, P1, S1, R, P0, S0], P0, MountedWithRawType[P0, S0, R]]

  type MappedMounted[F[+_], P1, S1, R <: RawMounted, P0 <: js.Object, S0 <: js.Object] =
    BaseMounted[F, P1, S1, R, P0, S0]

  implicit def toJsUnmountedOps[F[+_], P1, S1, R <: RawMounted, P0 <: js.Object, S0 <: js.Object](x: MappedUnmounted[F, P1, S1, R, P0, S0]): JsUnmountedOps[F, P1, S1, R, P0, S0] = new JsUnmountedOps(x)
  implicit def toJsComponentOps[F[+_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: RawMounted, P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]](x: MappedComponent[F, P1, S1, CT1, R, P0, S0, CT0]): JsComponentOps[F, P1, S1, CT1, R, P0, S0, CT0] = new JsComponentOps(x)

  // Scala bug requires special help for the Effect.Id case
  implicit def toJsUnmountedOpsI[P1, S1, R <: RawMounted, P0 <: js.Object, S0 <: js.Object](x: MappedUnmounted[Effect.Id, P1, S1, R, P0, S0]): JsUnmountedOps[Effect.Id, P1, S1, R, P0, S0] = new JsUnmountedOps(x)
  implicit def toJsComponentOpsI[P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: RawMounted, P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]](x: MappedComponent[Effect.Id, P1, S1, CT1, R, P0, S0, CT0]): JsComponentOps[Effect.Id, P1, S1, CT1, R, P0, S0, CT0] = new JsComponentOps(x)

  final class JsUnmountedOps[F[+_], P1, S1, R <: RawMounted, P0 <: js.Object, S0 <: js.Object](private val self: MappedUnmounted[F, P1, S1, R, P0, S0]) extends AnyVal {
    def withRawType[R2 <: RawMounted]: MappedUnmounted[F, P1, S1, R2, P0, S0] =
      self.asInstanceOf[MappedUnmounted[F, P1, S1, R2, P0, S0]]
    def addFacade[T <: js.Object]: MappedUnmounted[F, P1, S1, R with T, P0, S0] =
      withRawType[R with T]
    def mapProps[P2](f: P1 => P2): MappedUnmounted[F, P2, S1, R, P0, S0] =
      self.mapUnmountedProps(f).mapMounted(_ mapProps f)
    def xmapState[S2](f: S1 => S2)(g: S2 => S1): MappedUnmounted[F, P1, S2, R, P0, S0] =
      self.mapMounted(_.xmapState(f)(g))
    def zoomState[S2](get: S1 => S2)(set: S2 => S1 => S1): MappedUnmounted[F, P1, S2, R, P0, S0] =
      self.mapMounted(_.zoomState(get)(set))
  }

  final class JsComponentOps[F[+_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: RawMounted, P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]](private val self: MappedComponent[F, P1, S1, CT1, R, P0, S0, CT0]) extends AnyVal {
    def withRawType[R2 <: RawMounted]: MappedComponent[F, P1, S1, CT1, R2, P0, S0, CT0] =
      self.asInstanceOf[MappedComponent[F, P1, S1, CT1, R2, P0, S0, CT0]]
    def addFacade[T <: js.Object]: MappedComponent[F, P1, S1, CT1, R with T, P0, S0, CT0] =
      withRawType[R with T]
    def xmapProps[P2](f: P1 => P2)(g: P2 => P1): MappedComponent[F, P2, S1, CT1, R, P0, S0, CT0] =
      self.cmapCtorProps(g).mapUnmounted(_ mapProps f)
    def xmapState[S2](f: S1 => S2)(g: S2 => S1): MappedComponent[F, P1, S2, CT1, R, P0, S0, CT0] =
      self.mapUnmounted(_.xmapState(f)(g))
    def zoomState[S2](get: S1 => S2)(set: S2 => S1 => S1): MappedComponent[F, P1, S2, CT1, R, P0, S0, CT0] =
      self.mapUnmounted(_.zoomState(get)(set))
    def mapMounted[M2](f: MappedMounted[F, P1, S1, R, P0, S0] => M2) =
      self.mapUnmounted(_ mapMounted f)
  }
}

// =====================================================================================================================

trait TemplateForJsBaseComponent[RawComponent] {

  final type RootComponent[P <: js.Object, CT[-p, +u] <: CtorType[p, u], U] =
    BaseComponent[P, CT, U, P, CT, U]

  // Difference between this and its Generic counterpart:
  // - P0 has an upper bound of js.Object.
  // - .raw
  sealed trait BaseComponent[
      P1, CT1[-p, +u] <: CtorType[p, u], U1,
      P0 <: js.Object, CT0[-p, +u] <: CtorType[p, u], U0]
      extends Generic.BaseComponent[P1, CT1, U1, P0, CT0, U0] {

    override final type Root = RootComponent[P0, CT0, U0]
    override def cmapCtorProps[P2](f: P2 => P1): BaseComponent[P2, CT1, U1, P0, CT0, U0]
    override def mapUnmounted[U2](f: U1 => U2): BaseComponent[P1, CT1, U2, P0, CT0, U0]
    override def mapCtorType[CT2[-p, +u] <: CtorType[p, u]](f: CT1[P1, U1] => CT2[P1, U1])(implicit pf: Profunctor[CT2]): BaseComponent[P1, CT2, U1, P0, CT0, U0]

    val raw: RawComponent
  }

  final def rootComponent[P <: js.Object, CT[-p, +u] <: CtorType[p, u], U](rc: RawComponent, c: CT[P, U])
                                                                          (implicit pf: Profunctor[CT]): RootComponent[P, CT, U] =
    new RootComponent[P, CT, U] {
      override def root = this
      override val raw = rc
      override val ctor = c
      override implicit def ctorPF = pf

      override def cmapCtorProps[P2](f: P2 => P) = mappedC(this)(f, identity, identity, pf)
      override def mapUnmounted[U2](f: U => U2) = mappedC(this)(identity, identity, f, pf)
      override def mapCtorType[CT2[-p, +u] <: CtorType[p, u]](f: CT[P, U] => CT2[P, U])(implicit pf: Profunctor[CT2]) =
        mappedC(this)(identity, f, identity, pf)
    }

  protected final def mappedC[
      P2, CT2[-p, +u] <: CtorType[p, u], U2,
      P1, CT1[-p, +u] <: CtorType[p, u], U1,
      P0 <: js.Object, CT0[-p, +u] <: CtorType[p, u], U0]
      (from: BaseComponent[P1, CT1, U1, P0, CT0, U0])
      (cp: P2 => P1, mc: CT1[P1, U1] => CT2[P1, U1], mu: U1 => U2, pf: Profunctor[CT2])
      : BaseComponent[P2, CT2, U2, P0, CT0, U0] =
    new BaseComponent[P2, CT2, U2, P0, CT0, U0] {
      override def root = from.root
      override val raw = from.raw
      override val ctor = mc(from.ctor).dimap(cp, mu)
      override implicit def ctorPF = pf

      override def cmapCtorProps[P3](f: P3 => P2) = mappedC(from)(cp compose f, mc, mu, pf)
      override def mapUnmounted[U3](f: U2 => U3) = mappedC(from)(cp, mc, f compose mu, pf)
      override def mapCtorType[CT3[-p, +u] <: CtorType[p, u]](f: CT2[P2, U2] => CT3[P2, U2])(implicit pf3: Profunctor[CT3]) =
        mappedC(this)(identity, f, identity, pf3)
    }
}
