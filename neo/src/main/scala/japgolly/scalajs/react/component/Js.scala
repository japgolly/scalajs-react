package japgolly.scalajs.react.component

import scala.scalajs.js
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.{Callback, CallbackTo, ChildrenArg, CtorType, Key, PropsChildren, vdom, raw => Raw}
import org.scalajs.dom

object Js extends TemplateForJsComponent0[Raw.ReactClass] {

  // Underlying~  = Shallow. 0s match 1s. {P,S,CT} unmapped {F,R} possibly mapped.
  // Simple~      = Deep. when you first create a component before any mapping or addRawType
  // ~WithRawType = Deep. Simple~ with raw type
  // Mapped~      = Deep.

  type RawMounted = Raw.ReactComponent

  type UnderlyingUnmounted[P <: js.Object, M] =
    Unmounted0[P, M, P, M]

  type UnderlyingMounted[F[+_], P <: js.Object, S <: js.Object, R <: RawMounted] =
    Mounted0[F, P, S, R, P, S]

  type SimpleComponent[P <: js.Object, S <: js.Object, CT[-p, +u] <: CtorType[p, u]] = ComponentWithRawType2[P, S, CT, RawMounted]
  type SimpleUnmounted[P <: js.Object, S <: js.Object]                               = UnmountedWithRawType2[P, S,     RawMounted]
  type SimpleMounted  [P <: js.Object, S <: js.Object]                               = MountedWithRawType2  [P, S,     RawMounted]

  type ComponentWithRawType2[P <: js.Object, S <: js.Object, CT[-p, +u] <: CtorType[p, u], R <: RawMounted] =
    UnderlyingComponent[P, CT, UnmountedWithRawType2[P, S, R]]

  type UnmountedWithRawType2[P <: js.Object, S <: js.Object, R <: RawMounted] =
    UnderlyingUnmounted[P, MountedWithRawType2[P, S, R]]

  type MountedWithRawType2[P <: js.Object, S <: js.Object, R <: RawMounted] =
    UnderlyingMounted[Effect.Id, P, S, R]

  type ComponentWithRawType[P <: js.Object, S <: js.Object, CT[-p, +u] <: CtorType[p, u], R <: js.Object] =
    ComponentWithRawType2[P, S, CT, RawMounted with R]

  type UnmountedWithRawType[P <: js.Object, S <: js.Object, R <: js.Object] =
    UnmountedWithRawType2[P, S, RawMounted with R]

  type MountedWithRawType[P <: js.Object, S <: js.Object, R <: js.Object] =
    MountedWithRawType2[P, S, RawMounted with R]

  // ===================================================================================================================

  type MappedMounted  [F[+_], P1, S1, R <: RawMounted, P0 <: js.Object, S0 <: js.Object] = Mounted0[F, P1, S1, R, P0, S0]
  type MappedUnmounted[F[+_], P1, S1, R <: RawMounted, P0 <: js.Object, S0 <: js.Object] = Unmounted0[P1, MappedMounted[F, P1, S1, R, P0, S0], P0, MountedWithRawType2[P0, S0, R]]
  type MappedComponent[F[+_], P1, S1, R <: RawMounted, P0 <: js.Object, S0 <: js.Object, CT1[-p, +u] <: CtorType[p, u], CT0[-p, +u] <: CtorType[p, u]] = Component0[P1, CT1, MappedUnmounted[F, P1, S1, R, P0, S0], P0, CT0, UnmountedWithRawType2[P0, S0, R]]

  implicit def toJsUnmountedOps[F[+_], P1, S1, R <: RawMounted, P0 <: js.Object, S0 <: js.Object](x: MappedUnmounted[F, P1, S1, R, P0, S0]): JsUnmountedOps[F, P1, S1, R, P0, S0] = new JsUnmountedOps(x)
  implicit def toJsComponentOps[F[+_], P1, S1, R <: RawMounted, P0 <: js.Object, S0 <: js.Object, CT1[-p, +u] <: CtorType[p, u], CT0[-p, +u] <: CtorType[p, u]](x: MappedComponent[F, P1, S1, R, P0, S0, CT1, CT0]): JsComponentOps[F, P1, S1, R, P0, S0, CT1, CT0] = new JsComponentOps(x)

  // Scala bug requires special help for the Effect.Id case
  implicit def toJsUnmountedOpsI[P1, S1, R <: RawMounted, P0 <: js.Object, S0 <: js.Object](x: MappedUnmounted[Effect.Id, P1, S1, R, P0, S0]): JsUnmountedOps[Effect.Id, P1, S1, R, P0, S0] = new JsUnmountedOps(x)
  implicit def toJsComponentOpsI[P1, S1, R <: RawMounted, P0 <: js.Object, S0 <: js.Object, CT1[-p, +u] <: CtorType[p, u], CT0[-p, +u] <: CtorType[p, u]](x: MappedComponent[Effect.Id, P1, S1, R, P0, S0, CT1, CT0]): JsComponentOps[Effect.Id, P1, S1, R, P0, S0, CT1, CT0] = new JsComponentOps(x)

  final class JsUnmountedOps[F[+_], P1, S1, R <: RawMounted, P0 <: js.Object, S0 <: js.Object](private val self: MappedUnmounted[F, P1, S1, R, P0, S0]) extends AnyVal {
    def withRawType[R2 <: RawMounted]: MappedUnmounted[F, P1, S1, R2, P0, S0] =
      self.asInstanceOf[MappedUnmounted[F, P1, S1, R2, P0, S0]]
    def addRawType[T <: js.Object]: MappedUnmounted[F, P1, S1, R with T, P0, S0] =
      withRawType[R with T]
    def mapProps[P2](f: P1 => P2): MappedUnmounted[F, P2, S1, R, P0, S0] =
      self.mapUnmountedProps(f).mapMounted(_ mapProps f)
    def xmapState[S2](f: S1 => S2)(g: S2 => S1): MappedUnmounted[F, P1, S2, R, P0, S0] =
      self.mapMounted(_.xmapState(f)(g))
    def zoomState[S2](get: S1 => S2)(set: S2 => S1 => S1): MappedUnmounted[F, P1, S2, R, P0, S0] =
      self.mapMounted(_.zoomState(get)(set))
  }

  final class JsComponentOps[F[+_], P1, S1, R <: RawMounted, P0 <: js.Object, S0 <: js.Object, CT1[-p, +u] <: CtorType[p, u], CT0[-p, +u] <: CtorType[p, u]](private val self: MappedComponent[F, P1, S1, R, P0, S0, CT1, CT0]) extends AnyVal {
    def withRawType[R2 <: RawMounted]: MappedComponent[F, P1, S1, R2, P0, S0, CT1, CT0] =
      self.asInstanceOf[MappedComponent[F, P1, S1, R2, P0, S0, CT1, CT0]]
    def addRawType[T <: js.Object]: MappedComponent[F, P1, S1, R with T, P0, S0, CT1, CT0] =
      withRawType[R with T]
    def xmapProps[P2](f: P1 => P2)(g: P2 => P1): MappedComponent[F, P2, S1, R, P0, S0, CT1, CT0] =
      self.cmapCtorProps(g).mapUnmounted(_ mapProps f)
    def xmapState[S2](f: S1 => S2)(g: S2 => S1): MappedComponent[F, P1, S2, R, P0, S0, CT1, CT0] =
      self.mapUnmounted(_.xmapState(f)(g))
    def zoomState[S2](get: S1 => S2)(set: S2 => S1 => S1): MappedComponent[F, P1, S2, R, P0, S0, CT1, CT0] =
      self.mapUnmounted(_.zoomState(get)(set))
    def mapMounted[M2](f: MappedMounted[F, P1, S1, R, P0, S0] => M2) =
      self.mapUnmounted(_ mapMounted f)
  }

  object Test {
    @js.native sealed trait Raw1 extends js.Object { def raw1: Int }
    @js.native sealed trait Raw2 extends js.Object { def raw2: Int }
    @js.native sealed trait JsObjP extends js.Object
    case class ScalaObjP(js: JsObjP)
    case class ScalaObjP2(s: ScalaObjP)
    @js.native sealed trait JsObjS extends js.Object
    case class ScalaObjS(js: JsObjS)
    case class ScalaObjS2(s: ScalaObjS)
  }
  object TestId {
    import Test._
    val xxx = apply[JsObjP, ChildrenArg.None, JsObjS]("Test")

    xxx.mapUnmounted(u =>
//      toJsUnmountedOps[Effect.Id, JsObjP, JsObjS, RawMounted, JsObjP, JsObjS](u)
//      JsUnmountedOps(u)
//      toJsUnmountedOps(u)
      u
        .mapProps(ScalaObjP)
        .addRawType[Raw1]
        .mapProps(ScalaObjP2)
        .addRawType[Raw2]
    )

    toJsComponentOps[Effect.Id, JsObjP, JsObjS, RawMounted, JsObjP, JsObjS, CtorType.Props, CtorType.Props](xxx)
//    JsComponentOps(xxx)
//    toJsComponentOps(xxx)
//    xxx
      .xmapProps(ScalaObjP)(_.js)
      .addRawType[Raw1]
      .xmapProps(ScalaObjP2)(_.s)
      .addRawType[Raw2]
  }
  object TestCB {
    import Test._
    val xxx = apply[JsObjP, ChildrenArg.None, JsObjS]("Test").mapUnmounted(_.mapMounted(_.withEffect[CallbackTo]))

    xxx.mapUnmounted(u =>
//      toJsUnmountedOps[CallbackTo, JsObjP, JsObjS, RawMounted, JsObjP, JsObjS](u)
//      JsUnmountedOps(u)
//      toJsUnmountedOps(u)
      u
        .mapProps(ScalaObjP)
        .addRawType[Raw1]
        .mapProps(ScalaObjP2)
        .addRawType[Raw2]
    )

//    toJsComponentOps[CallbackTo, JsObjP, JsObjS, RawMounted, JsObjP, JsObjS, CtorType.Props, CtorType.Props](xxx)
//    JsComponentOps(xxx)
//    toJsComponentOps(xxx)
    xxx
      .xmapProps(ScalaObjP)(_.js)
      .addRawType[Raw1]
      .xmapProps(ScalaObjP2)(_.s)
      .addRawType[Raw2]
  }

  // ===================================================================================================================

  sealed trait Unmounted0[P1, M1, P0 <: js.Object, M0]
      extends Generic.Unmounted0[P1, M1, P0, M0] {

    override final type Underlying = UnderlyingUnmounted[P0, M0]
    override def mapUnmountedProps[P2](f: P1 => P2): Unmounted0[P2, M1, P0, M0]
    override def mapMounted[M2](f: M1 => M2): Unmounted0[P1, M2, P0, M0]

    val raw: Raw.ReactComponentElement
  }

  def underlyingUnmounted[P <: js.Object, M](r: Raw.ReactComponentElement, m: Raw.ReactComponent => M): UnderlyingUnmounted[P, M] =
    new UnderlyingUnmounted[P, M] {

      override def underlying = this

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
      (from: Unmounted0[P1, M1, P0, M0])
      (mp: P1 => P2, mm: M1 => M2)
      : Unmounted0[P2, M2, P0, M0] =
    new Unmounted0[P2, M2, P0, M0] {
      override def underlying    = from.underlying
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

  sealed trait Mounted0[F[+_], P1, S1, R <: RawMounted, P0 <: js.Object, S0 <: js.Object]
      extends Generic.Mounted0[F, P1, S1, P0, S0] {

    override final type Underlying = UnderlyingMounted[F, P0, S0, R]
    override def mapProps[P2](f: P1 => P2): Mounted0[F, P2, S1, R, P0, S0]
    override def xmapState[S2](f: S1 => S2)(g: S2 => S1): Mounted0[F, P1, S2, R, P0, S0]
    override def zoomState[S2](get: S1 => S2)(set: S2 => S1 => S1): Mounted0[F, P1, S2, R, P0, S0]
    override def withEffect[F2[+_]](implicit t: Effect.Trans[F, F2]): Mounted0[F2, P1, S1, R, P0, S0]

    val raw: R

    final def withRawType[R2 <: RawMounted]: Mounted0[F, P1, S1, R2, P0, S0] =
      this.asInstanceOf[Mounted0[F, P1, S1, R2, P0, S0]]

    final def addRawType[T <: js.Object]: Mounted0[F, P1, S1, R with T, P0, S0] =
      withRawType[R with T]

    // def getDefaultProps: Props
    // def getInitialState: js.Object | Null
    // def render(): ReactElement
  }

  def underlyingMounted[P <: js.Object, S <: js.Object, R <: RawMounted](r: R): UnderlyingMounted[Effect.Id, P, S, R] =
    new UnderlyingMounted[Effect.Id, P, S, R] {

      override def underlying = this

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
      (from: Mounted0[Effect.Id, P1, S1, R, P0, S0])
      (mp: P1 => P2, ls: Lens[S1, S2])
      (implicit ft: Effect.Trans[Effect.Id, F])
      : Mounted0[F, P2, S2, R, P0, S0] =
    new Mounted0[F, P2, S2, R, P0, S0] {
      override implicit def F    = ft.to
      override def underlying    = from.underlying.withEffect[F]
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

  def simpleComponent[P <: js.Object, C <: ChildrenArg, S <: js.Object](rc: Raw.ReactClass)
                                                                       (implicit s: CtorType.Summoner[P, C]): SimpleComponent[P, S, s.CT] =
    underlyingComponent[P, s.CT, SimpleUnmounted[P, S]](rc, s.pf.rmap(s.summon(rc))(simpleUnmounted))(s.pf)

  def simpleUnmounted[P <: js.Object, S <: js.Object](r: Raw.ReactComponentElement): SimpleUnmounted[P, S] =
    underlyingUnmounted(r, simpleMounted)

  def simpleMounted[P <: js.Object, S <: js.Object](r: RawMounted): SimpleMounted[P, S] =
    underlyingMounted(r)

  // ===================================================================================================================

  def apply[P <: js.Object, C <: ChildrenArg, S <: js.Object](name: String)
      (implicit s: CtorType.Summoner[P, C]): SimpleComponent[P, S, s.CT] = {

    val reactClass = findInScope(name.split('.').toList) match {
      case Some(constructor : js.Function) => constructor
      case Some(_)                         => throw new IllegalArgumentException(s"React constructor $name is not a function")
      case None                            => throw new IllegalArgumentException(s"React constructor $name is not defined")
    }
    simpleComponent[P, C, S](reactClass.asInstanceOf[Raw.ReactClass])(s)
  }

  private[this] def findInScope(path: List[String], scope: js.Dynamic = js.Dynamic.global) : Option[js.Dynamic] = {
    path match {
      case Nil => Some(scope)
      case name :: tail =>
        val nextScope = scope.selectDynamic(name).asInstanceOf[js.UndefOr[js.Dynamic]].toOption
        nextScope.flatMap(s => findInScope(tail, s))
    }
  }
}

// =====================================================================================================================

trait TemplateForJsComponent0[RawComponent] {

  final type UnderlyingComponent[P <: js.Object, CT[-p, +u] <: CtorType[p, u], U] =
    Component0[P, CT, U, P, CT, U]

  // Difference between this and its Generic counterpart:
  // - P0 has an upper bound of js.Object.
  // - .raw
  sealed trait Component0[
      P1, CT1[-p, +u] <: CtorType[p, u], U1,
      P0 <: js.Object, CT0[-p, +u] <: CtorType[p, u], U0]
      extends Generic.Component0[P1, CT1, U1, P0, CT0, U0] {

    override final type Underlying = UnderlyingComponent[P0, CT0, U0]
    override def cmapCtorProps[P2](f: P2 => P1): Component0[P2, CT1, U1, P0, CT0, U0]
    override def mapUnmounted[U2](f: U1 => U2): Component0[P1, CT1, U2, P0, CT0, U0]
    override def mapCtorType[CT2[-p, +u] <: CtorType[p, u]](f: CT1[P1, U1] => CT2[P1, U1])(implicit pf: Profunctor[CT2]): Component0[P1, CT2, U1, P0, CT0, U0]

    val raw: RawComponent
  }

  final def underlyingComponent[P <: js.Object, CT[-p, +u] <: CtorType[p, u], U](rc: RawComponent, c: CT[P, U])
                                                                                (implicit pf: Profunctor[CT]): UnderlyingComponent[P, CT, U] =
    new UnderlyingComponent[P, CT, U] {
      override def underlying = this
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
      (from: Component0[P1, CT1, U1, P0, CT0, U0])
      (cp: P2 => P1, mc: CT1[P1, U1] => CT2[P1, U1], mu: U1 => U2, pf: Profunctor[CT2])
      : Component0[P2, CT2, U2, P0, CT0, U0] =
    new Component0[P2, CT2, U2, P0, CT0, U0] {
      override def underlying = from.underlying
      override val raw = from.raw
      override val ctor = mc(from.ctor).dimap(cp, mu)
      override implicit def ctorPF = pf

      override def cmapCtorProps[P3](f: P3 => P2) = mappedC(from)(cp compose f, mc, mu, pf)
      override def mapUnmounted[U3](f: U2 => U3) = mappedC(from)(cp, mc, f compose mu, pf)
      override def mapCtorType[CT3[-p, +u] <: CtorType[p, u]](f: CT2[P2, U2] => CT3[P2, U2])(implicit pf3: Profunctor[CT3]) =
        mappedC(this)(identity, f, identity, pf3)
    }
}
