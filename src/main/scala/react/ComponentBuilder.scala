package golly.react

import scala.scalajs.js
import js.UndefOr

final class ComponentBuilder[Props](name: String) {

  def getInitialState[State](f: Props => State) = new B2[State](f)
  def initialState[State](s: State) = getInitialState(_ => s)
  def stateless = initialState(())
  def render(render: ComponentScopeU[Props, Unit, Unit] => VDom) = stateless.render(render)

  class B2[State] private[ComponentBuilder](getInitialState: Props => State) {
    type ScopeB = ComponentScopeB[Props, State]

    def backend[Backend](f: ScopeB => Backend) = new B3[Backend](f)
    def noBackend = new B3[Unit](js.undefined)
    def render(render: ComponentScopeU[Props, State, Unit] => VDom) = noBackend.render(render)

    class B3[Backend] private[ComponentBuilder](backend: UndefOr[ScopeB => Backend]) {
      type ScopeU = ComponentScopeU[Props, State, Backend]
      type ScopeM = ComponentScopeM[Props, State, Backend]
      type ScopeWU = ComponentScopeWU[Props, State, Backend]

      def render(render: ScopeU => VDom) =
        B4(render, js.undefined, js.undefined, js.undefined, js.undefined, js.undefined)

      case class B4 private[ComponentBuilder](
          __render: ScopeU => VDom
          , componentWillMount: UndefOr[ScopeU => Unit]
          , componentDidMount: UndefOr[ScopeM => Unit]
          , componentWillUnmount: UndefOr[ScopeM => Unit]
          , componentWillUpdate: UndefOr[(ScopeWU, Props, State) => Unit]
          , componentDidUpdate: UndefOr[(ScopeM, Props, State) => Unit]
          ) {

        def componentWillMount(f: ScopeU => Unit): B4 = copy(componentWillMount = f)
        def componentDidMount(f: ScopeM => Unit): B4 = copy(componentDidMount = f)
        def componentWillUnmount(f: ScopeM => Unit): B4 = copy(componentWillUnmount = f)
        def componentWillUpdate(f: (ScopeWU, Props, State) => Unit): B4 = copy(componentWillUpdate = f)
        def componentDidUpdate(f: (ScopeM, Props, State) => Unit): B4 = copy(componentDidUpdate = f)

        def buildSpec = {
          @inline def set(o: js.Object, k: String, v: js.Any): Unit = o.asInstanceOf[js.Dynamic].updateDynamic(k)(v) // TODO share
          val spec = js.Dynamic.literal(
              "displayName" -> name,
              "render" -> (__render: js.ThisFunction)
            ).asInstanceOf[js.Object]

          var componentWillMount2 = componentWillMount
          backend.foreach(f => {
            set(spec, "_backend", "PENDING...")
            componentWillMount2 = (t: ScopeU) => {
              val scopeB = t.asInstanceOf[ScopeB]
              t.asInstanceOf[js.Dynamic].updateDynamic("_backend")(WrapObj(f(scopeB)))
              componentWillMount.foreach(g => g(t))
            }
          })

          val initStateFn: ScopeU => WrapObj[State] = scope => WrapObj(getInitialState(scope.props))
          set(spec, "getInitialState", initStateFn: js.ThisFunction)

          componentWillMount2.foreach(f => set(spec, "componentWillMount", f: js.ThisFunction))
          componentWillUnmount.foreach(f => set(spec, "componentWillUnmount", f: js.ThisFunction))
          componentDidMount.foreach(f => set(spec, "componentDidMount", f: js.ThisFunction))
          componentWillUpdate.foreach { f =>
            val g = (t: ScopeWU, p: WrapObj[Props], s: WrapObj[State]) => f(t, p.v, s.v)
            set(spec, "componentWillUpdate", g: js.ThisFunction)
          }
          componentDidUpdate.foreach { f =>
            val g = (t: ScopeM, p: WrapObj[Props], s: WrapObj[State]) => f(t, p.v, s.v)
            set(spec, "componentDidUpdate", g: js.ThisFunction)
          }

          spec.asInstanceOf[ComponentSpec[Props]]
        }

        def build = React.createClass(buildSpec)
      }
    }
  }
}

object ComponentBuilder {
  def apply[Props](name: String) = new ComponentBuilder[Props](name)
}