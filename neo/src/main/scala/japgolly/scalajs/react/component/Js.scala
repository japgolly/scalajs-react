package japgolly.scalajs.react.component

import scala.scalajs.js
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.{Callback, ChildrenArg, CtorType, Key, PropsChildren, vdom, raw => Raw}
import org.scalajs.dom

object Js {

  // Underlying~  = 0s match 1s
  // Simple~      = when you first create a component before any mapping or addRawType
  // ~WithRawType = Simple~ with raw type

  type RawMounted =
    Raw.ReactComponent

  type UnderlyingComponent[P <: js.Object, CT[-p, +u] <: CtorType[p, u], U] =
    Component0[P, CT, U, P, CT, U]

  type UnderlyingUnmounted[P <: js.Object, M] =
    Unmounted0[P, M, P, M]

  type UnderlyingMounted[F[+_], P <: js.Object, S <: js.Object, R <: RawMounted] =
    Mounted0[F, P, S, R, P, S]

  type SimpleComponent[P <: js.Object, S <: js.Object, CT[-p, +u] <: CtorType[p, u]] = ComponentWithRawType[P, S, CT, RawMounted]
  type SimpleUnmounted[P <: js.Object, S <: js.Object]                               = UnmountedWithRawType[P, S,     RawMounted]
  type SimpleMounted  [P <: js.Object, S <: js.Object]                               = MountedWithRawType  [P, S,     RawMounted]

  type ComponentWithRawType[P <: js.Object, S <: js.Object, CT[-p, +u] <: CtorType[p, u], R <: RawMounted] =
    UnderlyingComponent[P, CT, UnmountedWithRawType[P, S, R]]

  type UnmountedWithRawType[P <: js.Object, S <: js.Object, R <: RawMounted] =
    UnderlyingUnmounted[P, MountedWithRawType[P, S, R]]

  type MountedWithRawType[P <: js.Object, S <: js.Object, R <: RawMounted] =
    UnderlyingMounted[Effect.Id, P, S, R]

  // ===================================================================================================================

  sealed trait Component0[
      P1, CT1[-p, +u] <: CtorType[p, u], U1,
      P0 <: js.Object, CT0[-p, +u] <: CtorType[p, u], U0]
      extends Generic.Component0[P1, CT1, U1, P0, CT0, U0] {
    override def underlying: UnderlyingComponent[P0, CT0, U0]
  }

  sealed trait Unmounted0[P, M, P0 <: js.Object, M0]
      extends Generic.Unmounted0[P, M, P0, M0] {
    override def underlying: UnderlyingUnmounted[P0, M0]

    val raw: Raw.ReactComponentElement
  }

  sealed trait Mounted0[F[+_], P, S, R <: RawMounted, P0 <: js.Object, S0 <: js.Object]
      extends Generic.Mounted0[F, P, S, P0, S0] {
    override def underlying: UnderlyingMounted[F, P0, S0, R]

    val raw: R

    final def addRawType[T <: js.Object]: Mounted0[F, P, S, R with T, P0, S0] =
      this.asInstanceOf[Mounted0[F, P, S, R with T, P0, S0]]

    // def getDefaultProps: Props
    // def getInitialState: js.Object | Null
    // def render(): ReactElement
  }

  // ===================================================================================================================

  def underlyingComponent[P <: js.Object, CT[-p, +u] <: CtorType[p, u], U](c: CT[P, U]): UnderlyingComponent[P, CT, U] =
    new UnderlyingComponent[P, CT, U] {
      override def underlying = this
      override val ctor = c
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
    }

  def underlyingMounted[P <: js.Object, S <: js.Object, R <: RawMounted](r: R): UnderlyingMounted[Effect.Id, P, S, R] =
    new UnderlyingMounted[Effect.Id, P, S, R] {

      override def underlying = this

      override val raw = r

      override protected implicit def F = Effect.idInstance

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
    }

  // ===================================================================================================================

  def simpleComponent[P <: js.Object, C <: ChildrenArg, S <: js.Object](rc: Raw.ReactClass)
                                                             (implicit s: CtorType.Summoner[P, C]): SimpleComponent[P, S, s.CT] =
    underlyingComponent(s.pf.rmap(s.summon(rc))(simpleUnmounted))

  def simpleUnmounted[P <: js.Object, S <: js.Object](r: Raw.ReactComponentElement): SimpleUnmounted[P, S] =
    underlyingUnmounted(r, simpleMounted)

  def simpleMounted[P <: js.Object, S <: js.Object](r: RawMounted): SimpleMounted[P, S] =
    underlyingMounted(r)

  // ===================================================================================================================

  def byName[P <: js.Object, C <: ChildrenArg, S <: js.Object](name: String)
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
