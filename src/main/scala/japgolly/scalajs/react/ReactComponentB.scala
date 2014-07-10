package japgolly.scalajs.react

import scala.scalajs.js._

final class ReactComponentB[Props](name: String) {

  def getInitialState[State](f: Props => State) = new B2[State](f)
  def initialState[State](s: => State) = getInitialState(_ => s)
  def stateless = initialState(())
  def render(render: Props => VDom) = stateless.render((p,_) => render(p))

  class B2[State] private[ReactComponentB](getInitialState: Props => State) {
    type ScopeB = BackendScope[Props, State]

    def backend[Backend](f: ScopeB => Backend) = new B3[Backend](f)
    def noBackend = new B3[Unit](_ => ())
    def render(render: (Props, State) => VDom) = noBackend.render((p,s,_) => render(p,s))

    class B3[Backend] private[ReactComponentB](backend: ScopeB => Backend) {
      type ScopeU = ComponentScopeU[Props, State, Backend]
      type ScopeM = ComponentScopeM[Props, State, Backend]
      type ScopeWU = ComponentScopeWU[Props, State, Backend]

      def render(r: (Props, State, Backend) => VDom): B4 =
        render(s => r(s.props, s.state, s.backend))

      def render(render: ScopeU => VDom): B4 =
        B4(render, undefined, undefined, undefined, undefined, undefined, undefined, undefined, undefined)

      case class B4 private[ReactComponentB](
          __render: ScopeU => VDom
          , getDefaultProps: UndefOr[() => Props]
          , componentWillMount: UndefOr[ScopeU => Unit]
          , componentDidMount: UndefOr[ScopeM => Unit]
          , componentWillUnmount: UndefOr[ScopeM => Unit]
          , componentWillUpdate: UndefOr[(ScopeWU, Props, State) => Unit]
          , componentDidUpdate: UndefOr[(ScopeM, Props, State) => Unit]
          , componentWillReceiveProps: UndefOr[(ScopeM, Props) => Unit]
          , shouldComponentUpdate: UndefOr[(ScopeM, Props, State) => Boolean]
          ) {

        def getDefaultProps(f: => Props): B4 = copy(getDefaultProps = () => f)
        def componentWillMount(f: ScopeU => Unit): B4 = copy(componentWillMount = f)
        def componentDidMount(f: ScopeM => Unit): B4 = copy(componentDidMount = f)
        def componentWillUnmount(f: ScopeM => Unit): B4 = copy(componentWillUnmount = f)
        def componentWillUpdate(f: (ScopeWU, Props, State) => Unit): B4 = copy(componentWillUpdate = f)
        def componentDidUpdate(f: (ScopeM, Props, State) => Unit): B4 = copy(componentDidUpdate = f)
        def componentWillReceiveProps(f: (ScopeM, Props) => Unit): B4 = copy(componentWillReceiveProps = f)
        def shouldComponentUpdate(f: (ScopeM, Props, State) => Boolean): B4 = copy(shouldComponentUpdate = f)

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

        def create = new WComponentConstructor(React.createClass(buildSpec))
      }
    }
  }
}

object ReactComponentB {
  def apply[Props](name: String) = new ReactComponentB[Props](name)
}