package japgolly.scalajs.react

import scala.scalajs.js.{Any => JAny, _}
import Internal._

object ReactComponentB {
  def apply[Props](name: String) = new P[Props](name)

  // ===================================================================================================================
  final class P[Props] private[ReactComponentB](name: String) {

    // getInitialState is how it's named in React
    def getInitialState[State](f: Props => State) = initialStateP(f)
    def initialStateP[State](f: Props => State)   = new PS(name, f)
    def initialState[State](s: => State)          = initialStateP(_ => s)
    def stateless                                 = initialState(())

    def render(f: Props                  => VDom) = stateless.render((p,_) => f(p))
    def render(f: (Props, PropsChildren) => VDom) = stateless.render((p,c,_) => f(p,c))
  }

  // ===================================================================================================================
  final class PS[Props, State] private[ReactComponentB](name: String, initF: Props => State) {

    def backend[Backend](f: BackendScope[Props, State] => Backend) = new PSB(name, initF, f)
    def noBackend                                                  = backend(_ => ())

    def render(f: (Props, State)                                       => VDom) = noBackend.render((p,s,_) => f(p,s))
    def render(f: (Props, PropsChildren, State)                        => VDom) = noBackend.render((p,c,s,_) => f(p,c,s))
    def render(f: ComponentScopeU[Props, State, Unit]                  => VDom) = noBackend.render(f)
    def renderS(f: (ComponentScopeU[Props, State, Unit], Props, State) => VDom) = noBackend.renderS(f)
  }

  // ===================================================================================================================
  final class PSB[P, S, B] private[ReactComponentB](name: String, initF: P => S, backF: BackendScope[P, S] => B) {

    def render(f: ComponentScopeU[P, S, B] => VDom): ReactComponentB[P, S, B] =
      new ReactComponentB(name, initF, backF, f,
        LifeCycle(undefined, undefined, undefined, undefined, undefined, undefined, undefined, undefined))

    def render(f: (P, S, B) => VDom): ReactComponentB[P, S, B] =
      render(s => f(s.props, s.state, s.backend))

    def render(f: (P, PropsChildren, S, B) => VDom): ReactComponentB[P, S, B] =
      render(s => f(s.props, s.propsChildren, s.state, s.backend))

    def renderS(f: (ComponentScopeU[P, S, B], P, S) => VDom): ReactComponentB[P, S, B] =
      render(T => f(T, T.props, T.state))
  }

  // ===================================================================================================================
  private[react] case class LifeCycle[P,S,B](
    getDefaultProps           : UndefOr[()                                => P],
    componentWillMount        : UndefOr[ComponentScopeU[P, S, B]          => Unit],
    componentDidMount         : UndefOr[ComponentScopeM[P, S, B]          => Unit],
    componentWillUnmount      : UndefOr[ComponentScopeM[P, S, B]          => Unit],
    componentWillUpdate       : UndefOr[(ComponentScopeWU[P, S, B], P, S) => Unit],
    componentDidUpdate        : UndefOr[(ComponentScopeM[P, S, B], P, S)  => Unit],
    componentWillReceiveProps : UndefOr[(ComponentScopeM[P, S, B], P)     => Unit],
    shouldComponentUpdate     : UndefOr[(ComponentScopeM[P, S, B], P, S)  => Boolean])
}

import ReactComponentB.LifeCycle

final class ReactComponentB[P, S, B](name: String,
                                     initF: P => S,
                                     backF: BackendScope[P, S] => B,
                                     rendF: ComponentScopeU[P, S, B] => VDom,
                                     lc: LifeCycle[P, S, B]) {

  def configure(fs: (ReactComponentB[P, S, B] => ReactComponentB[P, S, B])*): ReactComponentB[P, S, B] =
    fs.foldLeft(this)((a,f) => f(a))

  @inline private implicit def lcmod(a: LifeCycle[P, S, B]): ReactComponentB[P, S, B] =
    new ReactComponentB(name, initF, backF, rendF, a)

  def getDefaultProps(p: => P): ReactComponentB[P, S, B] =
    lc.copy(getDefaultProps = () => p)

  def componentWillMount(f: ComponentScopeU[P, S, B] => Unit): ReactComponentB[P, S, B] =
    lc.copy(componentWillMount = fcUnit(lc.componentWillMount, f))

  def componentDidMount(f: ComponentScopeM[P, S, B] => Unit): ReactComponentB[P, S, B] =
    lc.copy(componentDidMount = fcUnit(lc.componentDidMount, f))

  def componentWillUnmount(f: ComponentScopeM[P, S, B] => Unit): ReactComponentB[P, S, B] =
    lc.copy(componentWillUnmount = fcUnit(lc.componentWillUnmount, f))

  def componentWillUpdate(f: (ComponentScopeWU[P, S, B], P, S) => Unit): ReactComponentB[P, S, B] =
    lc.copy(componentWillUpdate = fcUnit(lc.componentWillUpdate, f))

  def componentDidUpdate(f: (ComponentScopeM[P, S, B], P, S) => Unit): ReactComponentB[P, S, B] =
    lc.copy(componentDidUpdate = fcUnit(lc.componentDidUpdate, f))

  def componentWillReceiveProps(f: (ComponentScopeM[P, S, B], P) => Unit): ReactComponentB[P, S, B] =
    lc.copy(componentWillReceiveProps = fcUnit(lc.componentWillReceiveProps, f))

  def shouldComponentUpdate(f: (ComponentScopeM[P, S, B], P, S) => Boolean): ReactComponentB[P, S, B] =
    lc.copy(shouldComponentUpdate = fcEither(lc.shouldComponentUpdate, f))

  def propsRequired         = new Builder(new ReactComponentC.ReqProps(_, None))
  def propsDefault(p: => P) = new Builder(new ReactComponentC.DefaultProps(_, None, () => p))
  def propsConst(p: => P)   = new Builder(new ReactComponentC.ConstProps(_, None, () => p))

  @deprecated("propsAlways() has been renamed to propsConst() and will be removed in 0.6.0.", "0.5.0")
  def propsAlways(p: => P)  = propsConst(p)

  // Convenience methods

  @inline def buildSpec = propsRequired.buildSpec
  @inline def create    = propsRequired.create

  @inline def propsUnit(implicit ev: Unit =:= P) = propsConst(ev(()))
  @inline def createU(implicit ev: Unit =:= P)   = propsUnit.create

  // ===================================================================================================================
  final class Builder[C] private[ReactComponentB](cc: ComponentConstructor[P, S, B] => C) {

    def buildSpec: ComponentSpec[P, S, B] = {
      val spec = Dynamic.literal(
        "displayName" -> name,
        "backend" -> 0,
        "render" -> (rendF: ThisFunction)
      )

      @inline def setFnPS[T, R](fn: UndefOr[(T, P, S) => R], name: String): Unit =
        fn.foreach { f =>
          val g = (t: T, p: WrapObj[P], s: WrapObj[S]) => f(t, p.v, s.v)
          spec.updateDynamic(name)(g: ThisFunction)
        }

      val componentWillMount2 = (t: ComponentScopeU[P, S, B]) => {
        val scopeB = t.asInstanceOf[BackendScope[P, S]]
        t.asInstanceOf[Dynamic].updateDynamic("backend")(backF(scopeB).asInstanceOf[JAny])
        lc.componentWillMount.foreach(g => g(t))
      }
      spec.updateDynamic("componentWillMount")(componentWillMount2: ThisFunction)

      val initStateFn: ComponentScopeU[P, S, B] => WrapObj[S] = scope => WrapObj(initF(scope.props))
      spec.updateDynamic("getInitialState")(initStateFn: ThisFunction)

      lc.getDefaultProps.foreach(f => spec.updateDynamic("getDefaultProps")(f: Function))
      lc.componentWillUnmount.foreach(f => spec.updateDynamic("componentWillUnmount")(f: ThisFunction))
      lc.componentDidMount.foreach(f => spec.updateDynamic("componentDidMount")(f: ThisFunction))

      setFnPS(lc.componentWillUpdate, "componentWillUpdate")
      setFnPS(lc.componentDidUpdate, "componentDidUpdate")
      setFnPS(lc.shouldComponentUpdate, "shouldComponentUpdate")

      lc.componentWillReceiveProps.foreach { f =>
        val g = (t: ComponentScopeM[P, S, B], p: WrapObj[P]) => f(t, p.v)
        spec.updateDynamic("componentWillReceiveProps")(g: ThisFunction)
      }

      spec.asInstanceOf[ComponentSpec[P, S, B]]
    }

    def create: C = cc(React.createClass(buildSpec))
  }
}
