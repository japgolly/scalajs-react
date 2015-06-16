package japgolly.scalajs.react

import scala.scalajs.js.{Any => JAny, Array => JArray, _}
import Internal._

object ReactComponentB {

  // ===================================================================================================================
  // Builder

  @inline def apply[Props](name: String) = new P[Props](name)

  implicit def defaultDomType[P,S,B](c: PSBN[P,S,B]) = c.domType[TopNode]
  implicit def defaultProps[P,S,B,N <: TopNode](c: ReactComponentB[P,S,B,N]) = c.propsRequired
  implicit def defaultDomTypeAndProps[P,S,B](c: PSBN[P,S,B]) = defaultProps(defaultDomType(c))

  // ===================================================================================================================
  // Convenience

  /**
   * Create a component that always displays the same content, never needs to be redrawn, never needs vdom diffing.
   */
  def static(name: String, content: ReactElement) =
    ReactComponentB[Unit](name)
      .stateless
      .noBackend
      .render(_ => content)
      .shouldComponentUpdate((_, _, _) => false)

  // ===================================================================================================================
  final class P[Props] private[ReactComponentB](name: String) {

    // getInitialState is how it's named in React
    def getInitialState[State](f: Props => State) = initialStateP(f)
    def initialStateP[State](f: Props => State)   = new PS(name, f)
    def initialState[State](s: => State)          = initialStateP(_ => s)
    def stateless                                 = initialState(())

    def render(f: Props                  => ReactElement) = stateless.render((p,_) => f(p))
    def render(f: (Props, PropsChildren) => ReactElement) = stateless.render((p,c,_) => f(p,c))
  }

  // ===================================================================================================================
  final class PS[Props, State] private[ReactComponentB](name: String, initF: Props => State) {

    def backend[Backend](f: BackendScope[Props, State] => Backend) = new PSB(name, initF, f)
    def noBackend                                                  = backend(_ => ())

    def render(f: (Props, State)                                       => ReactElement) = noBackend.render((p,s,_) => f(p,s))
    def render(f: (Props, PropsChildren, State)                        => ReactElement) = noBackend.render((p,c,s,_) => f(p,c,s))
    def render(f: ComponentScopeU[Props, State, Unit]                  => ReactElement) = noBackend.render(f)
    def renderS(f: (ComponentScopeU[Props, State, Unit], Props, State) => ReactElement) = noBackend.renderS(f)
  }

  // ===================================================================================================================
  final class PSB[P, S, B] private[ReactComponentB](name: String, initF: P => S, backF: BackendScope[P, S] => B) {

    def render(f: ComponentScopeU[P, S, B] => ReactElement): PSBN[P, S, B] =
      new PSBN(name, initF, backF, f)

    def render(f: (P, S, B) => ReactElement): PSBN[P, S, B] =
      render(s => f(s.props, s.state, s.backend))

    def render(f: (P, PropsChildren, S, B) => ReactElement): PSBN[P, S, B] =
      render(s => f(s.props, s.propsChildren, s.state, s.backend))

    def renderS(f: (ComponentScopeU[P, S, B], P, S) => ReactElement): PSBN[P, S, B] =
      render(T => f(T, T.props, T.state))
  }

  // ===================================================================================================================
  final class PSBN[P, S, B] private[ReactComponentB](name: String, initF: P => S, backF: BackendScope[P, S] => B, rendF: ComponentScopeU[P, S, B] => ReactElement) {
    def domType[N <: TopNode]: ReactComponentB[P, S, B, N] =
      new ReactComponentB(name, initF, backF, rendF, emptyLifeCycle, Vector.empty, undefined)
  }

  // ===================================================================================================================
  private[react] case class LifeCycle[P,S,B,N <: TopNode](
    configureSpec            : UndefOr[ReactComponentSpec[P, S, B, N]       => Unit],
    getDefaultProps          : UndefOr[()                                   => P],
    componentWillMount       : UndefOr[ComponentScopeU[P, S, B]             => Unit],
    componentDidMount        : UndefOr[ComponentScopeM[P, S, B, N]          => Unit],
    componentWillUnmount     : UndefOr[ComponentScopeM[P, S, B, N]          => Unit],
    componentWillUpdate      : UndefOr[(ComponentScopeWU[P, S, B, N], P, S) => Unit],
    componentDidUpdate       : UndefOr[(ComponentScopeM[P, S, B, N], P, S)  => Unit],
    componentWillReceiveProps: UndefOr[(ComponentScopeM[P, S, B, N], P)     => Unit],
    shouldComponentUpdate    : UndefOr[(ComponentScopeM[P, S, B, N], P, S)  => Boolean])

  private[react] def emptyLifeCycle[P,S,B,N <: TopNode] =
    LifeCycle[P,S,B,N](undefined, undefined, undefined, undefined, undefined, undefined, undefined, undefined, undefined)
}

import ReactComponentB.LifeCycle

final class ReactComponentB[P,S,B,N <: TopNode](val name: String,
                                                initF   : P => S,
                                                backF   : BackendScope[P, S] => B,
                                                rendF   : ComponentScopeU[P, S, B] => ReactElement,
                                                lc      : LifeCycle[P, S, B, N],
                                                jsMixins: Vector[JAny],
                                                context : UndefOr[ReactContext_.Context[P]]) {

  @inline private def copy(name    : String                                   = name    ,
                           initF   : P => S                                   = initF   ,
                           backF   : BackendScope[P, S] => B                  = backF   ,
                           rendF   : ComponentScopeU[P, S, B] => ReactElement = rendF   ,
                           lc      : LifeCycle[P, S, B, N]                    = lc      ,
                           jsMixins: Vector[JAny]                             = jsMixins,
                           context : UndefOr[ReactContext_.Context[P]]        = context): ReactComponentB[P, S, B, N] =
    new ReactComponentB(name, initF, backF, rendF, lc, jsMixins, context)

  @inline private implicit def lcmod(a: LifeCycle[P, S, B, N]): ReactComponentB[P, S, B, N] =
    copy(lc = a)

  def configureSpec(modify: ReactComponentSpec[P, S, B, N] => Unit): ReactComponentB[P, S, B, N] =
       lc.copy(configureSpec = modify)

  def configure(fs: (ReactComponentB[P, S, B, N] => ReactComponentB[P, S, B, N])*): ReactComponentB[P, S, B, N] =
    fs.foldLeft(this)((a,f) => f(a))

  def getDefaultProps(p: => P): ReactComponentB[P, S, B, N] =
    lc.copy(getDefaultProps = () => p)

  def componentWillMount(f: ComponentScopeU[P, S, B] => Unit): ReactComponentB[P, S, B, N] =
    lc.copy(componentWillMount = fcUnit(lc.componentWillMount, f))

  def componentDidMount(f: ComponentScopeM[P, S, B, N] => Unit): ReactComponentB[P, S, B, N] =
    lc.copy(componentDidMount = fcUnit(lc.componentDidMount, f))

  def componentWillUnmount(f: ComponentScopeM[P, S, B, N] => Unit): ReactComponentB[P, S, B, N] =
    lc.copy(componentWillUnmount = fcUnit(lc.componentWillUnmount, f))

  def componentWillUpdate(f: (ComponentScopeWU[P, S, B, N], P, S) => Unit): ReactComponentB[P, S, B, N] =
    lc.copy(componentWillUpdate = fcUnit(lc.componentWillUpdate, f))

  def componentDidUpdate(f: (ComponentScopeM[P, S, B, N], P, S) => Unit): ReactComponentB[P, S, B, N] =
    lc.copy(componentDidUpdate = fcUnit(lc.componentDidUpdate, f))

  def componentWillReceiveProps(f: (ComponentScopeM[P, S, B, N], P) => Unit): ReactComponentB[P, S, B, N] =
    lc.copy(componentWillReceiveProps = fcUnit(lc.componentWillReceiveProps, f))

  def shouldComponentUpdate(f: (ComponentScopeM[P, S, B, N], P, S) => Boolean): ReactComponentB[P, S, B, N] =
    lc.copy(shouldComponentUpdate = fcEither(lc.shouldComponentUpdate, f))

  /**
   * Install a pure-JS React mixin.
   *
   * Beware: There will be mixins that won't work correctly as they make assumptions that don't hold for Scala.
   * If a mixin expects to inspect your props or state, forget about it; Scala-land owns that data.
   */
  def mixinJS(mixins: JAny*): ReactComponentB[P, S, B, N] =
    copy(jsMixins = jsMixins ++ mixins)

  def defineContext(c: ReactContext_.Base): ReactComponentB[P, S, B, N] =
    copy(context = Left(c))

  def deriveContext(c: ReactContext_.Derived[P]): ReactComponentB[P, S, B, N] =
    copy(context = Right(c))

  /**
   * Modify the render function.
   */
  def reRender(f: (ComponentScopeU[P, S, B] => ReactElement) => ComponentScopeU[P, S, B] => ReactElement): ReactComponentB[P, S, B, N] =
    copy(rendF = f(rendF))

  // ===================================================================================================================
  @inline private def builder[C](cc: ReactComponentCU[P,S,B,N] => C) = new Builder(cc)

  def propsRequired         = builder(new ReactComponentC.ReqProps    [P,S,B,N](_, undefined, undefined))
  def propsDefault(p: => P) = builder(new ReactComponentC.DefaultProps[P,S,B,N](_, undefined, undefined, () => p))
  def propsConst  (p: => P) = builder(new ReactComponentC.ConstProps  [P,S,B,N](_, undefined, undefined, () => p))

  def propsUnit(implicit ev: Unit =:= P) = propsConst(ev(()))
  def buildU   (implicit ev: Unit =:= P) = propsUnit.build

  final class Builder[C] private[ReactComponentB](cc: ReactComponentCU[P,S,B,N] => C) {

    def buildSpec: ReactComponentSpec[P, S, B, N] = {
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

      context.foreach(_ match {
        case Right(c) => ReactContext_.applyToSpec(spec, c)
        case Left(c) => ReactContext_.applyToSpec(spec, c)
      })

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
        val g = (t: ComponentScopeM[P, S, B, N], p: WrapObj[P]) => f(t, p.v)
        spec.updateDynamic("componentWillReceiveProps")(g: ThisFunction)
      }

      if (jsMixins.nonEmpty) {
        val mixins = JArray(jsMixins: _*)
        spec.updateDynamic("mixins")(mixins)
      }

      val spec2 = spec.asInstanceOf[ReactComponentSpec[P, S, B, N]]
      lc.configureSpec.foreach(_(spec2))
      spec2
    }

    def build: C =
      cc(React.createFactory(React.createClass(buildSpec)))
  }
}
