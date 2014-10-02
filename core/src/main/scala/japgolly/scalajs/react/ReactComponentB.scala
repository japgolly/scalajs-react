package japgolly.scalajs.react

import scala.scalajs.js._
import Internal._

object ReactComponentB {
  def apply[Props](name: String) = new ReactComponentB[Props](name)
}

final class ReactComponentB[Props](name: String) {

  def getInitialState[State](f: Props => State) = new B2[State](f)
  def initialState[State](s: => State)          = getInitialState(_ => s)
  def stateless                                 = initialState(())

  def render(f: Props                  => VDom) = stateless.render((p,_) => f(p))
  def render(f: (Props, PropsChildren) => VDom) = stateless.render((p,c,_) => f(p,c))

  // -----------------------------------------------------------------------------------------------
  final class B2[State] private[ReactComponentB](getInitialState: Props => State) {
    type ScopeB = BackendScope[Props, State]

    def backend[Backend](f: ScopeB => Backend) = new B3[Backend](f)
    def noBackend                              = new B3[Unit](_ => ())

    def render(f: (Props, State)                                       => VDom) = noBackend.render((p,s,_) => f(p,s))
    def render(f: (Props, PropsChildren, State)                        => VDom) = noBackend.render((p,c,s,_) => f(p,c,s))
    def render(f: ComponentScopeU[Props, State, Unit]                  => VDom) = noBackend.render(f)
    def renderS(f: (ComponentScopeU[Props, State, Unit], Props, State) => VDom) = noBackend.renderS(f)

    // ---------------------------------------------------------------------------------------------
    final class B3[Backend] private[ReactComponentB](backend: ScopeB => Backend) {
      type ScopeU  = ComponentScopeU [Props, State, Backend]
      type ScopeM  = ComponentScopeM [Props, State, Backend]
      type ScopeWU = ComponentScopeWU[Props, State, Backend]

      type CCP  = CompCtorP [Props, State, Backend]
      type CCOP = CompCtorOP[Props, State, Backend]
      type CCNP = CompCtorNP[Props, State, Backend]

      def renderS(f: (ScopeU, Props, State)                => VDom): B4[CCP] = render(T => f(T, T.props, T.state))
      def render(f: (Props, State, Backend)                => VDom): B4[CCP] = render(s => f(s.props, s.state, s.backend))
      def render(f: (Props, PropsChildren, State, Backend) => VDom): B4[CCP] = render(s => f(s.props, s.propsChildren, s.state, s.backend))
      def render(f: ScopeU                                 => VDom): B4[CCP] =
        B4[CCP](f, new CompCtorP(_, None)
          , undefined, undefined, undefined, undefined, undefined, undefined, undefined, undefined)

      // -------------------------------------------------------------------------------------------
      case class B4[C] private[ReactComponentB](
          __render: ScopeU => VDom,
          __c: ComponentConstructor[Props, State, Backend] => C
          , getDefaultProps           : UndefOr[() => Props]
          , componentWillMount        : UndefOr[ScopeU                  => Unit]
          , componentDidMount         : UndefOr[ScopeM                  => Unit]
          , componentWillUnmount      : UndefOr[ScopeM                  => Unit]
          , componentWillUpdate       : UndefOr[(ScopeWU, Props, State) => Unit]
          , componentDidUpdate        : UndefOr[(ScopeM, Props, State)  => Unit]
          , componentWillReceiveProps : UndefOr[(ScopeM, Props)         => Unit]
          , shouldComponentUpdate     : UndefOr[(ScopeM, Props, State)  => Boolean]
          ) {

        def getDefaultProps(f: => Props): B4[C]    = copy(getDefaultProps = () => f)
        def propsDefault   (f: => Props): B4[CCOP] = copy(__c = new CompCtorOP(_, None, () => f))
        def propsAlways    (f: => Props): B4[CCNP] = copy(__c = new CompCtorNP(_, None, () => f))
        // def propsAlways(f: => Props): B4[CCNP] = getDefaultProps(f).copy(__c = new CompCtorNP(_))

        def componentWillMount       (f: ScopeU                  => Unit   ): B4[C] = copy(componentWillMount        = fcUnit(componentWillMount       , f))
        def componentDidMount        (f: ScopeM                  => Unit   ): B4[C] = copy(componentDidMount         = fcUnit(componentDidMount        , f))
        def componentWillUnmount     (f: ScopeM                  => Unit   ): B4[C] = copy(componentWillUnmount      = fcUnit(componentWillUnmount     , f))
        def componentWillUpdate      (f: (ScopeWU, Props, State) => Unit   ): B4[C] = copy(componentWillUpdate       = fcUnit(componentWillUpdate      , f))
        def componentDidUpdate       (f: (ScopeM, Props, State)  => Unit   ): B4[C] = copy(componentDidUpdate        = fcUnit(componentDidUpdate       , f))
        def componentWillReceiveProps(f: (ScopeM, Props)         => Unit   ): B4[C] = copy(componentWillReceiveProps = fcUnit(componentWillReceiveProps, f))
        def shouldComponentUpdate    (f: (ScopeM, Props, State)  => Boolean): B4[C] = copy(shouldComponentUpdate     = fcEither(shouldComponentUpdate  , f))

        def buildSpec = {
          val spec = Dynamic.literal(
              "displayName" -> name,
              "backend" -> 0,
              "render" -> (__render: ThisFunction)
            )

          @inline def setFnPS[T, R](fn: UndefOr[(T, Props, State) => R], name: String): Unit =
            fn.foreach { f =>
              val g = (t: T, p: WrapObj[Props], s: WrapObj[State]) => f(t, p.v, s.v)
              spec.updateDynamic(name)(g: ThisFunction)
            }

          val componentWillMount2 = (t: ScopeU) => {
            val scopeB = t.asInstanceOf[ScopeB]
            t.asInstanceOf[Dynamic].updateDynamic("backend")(backend(scopeB).asInstanceOf[Any])
            componentWillMount.foreach(g => g(t))
          }
          spec.updateDynamic("componentWillMount")(componentWillMount2: ThisFunction)

          val initStateFn: ScopeU => WrapObj[State] = scope => WrapObj(getInitialState(scope.props))
          spec.updateDynamic("getInitialState")(initStateFn: ThisFunction)

          getDefaultProps.foreach(f => spec.updateDynamic("getDefaultProps")(f: Function))
          componentWillUnmount.foreach(f => spec.updateDynamic("componentWillUnmount")(f: ThisFunction))
          componentDidMount.foreach(f => spec.updateDynamic("componentDidMount")(f: ThisFunction))

          setFnPS(componentWillUpdate, "componentWillUpdate")
          setFnPS(componentDidUpdate, "componentDidUpdate")
          setFnPS(shouldComponentUpdate, "shouldComponentUpdate")

          componentWillReceiveProps.foreach { f =>
            val g = (t: ScopeM, p: WrapObj[Props]) => f(t, p.v)
            spec.updateDynamic("componentWillReceiveProps")(g: ThisFunction)
          }

          spec.asInstanceOf[ComponentSpec[Props, State, Backend]]
        }

        def create = __c(React.createClass(buildSpec))

        /** When the Props type is Unit, this shortcut returns a constructor that won't ask for a props value. */
        def createU(implicit ev: Unit =:= Props) = propsAlways(ev(())).create
      }
    }
  }
}
