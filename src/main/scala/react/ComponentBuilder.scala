package golly.react

import scala.scalajs.js

final class ComponentBuilder[Props, State](name: String) {

  type ScopeB = ComponentScopeB[Props, State]

  def backend[Backend](f: ScopeB => Backend) = new B2[Backend](f)
  def noBackend = new B2[Unit](js.undefined)
  def render(render: ComponentScopeU[Props, State, Unit] => VDom) = noBackend.render(render)

  class B2[Backend](backend: js.UndefOr[ScopeB => Backend]) {

    type ScopeU = ComponentScopeU[Props, State, Backend]
    type ScopeM = ComponentScopeM[Props, State, Backend]
    type ScopeWU = ComponentScopeWU[Props, State, Backend]

    def render(render: ScopeU => VDom) =
      B3(render, js.undefined, js.undefined, js.undefined, js.undefined, js.undefined, js.undefined)

    case class B3(__render: ScopeU => VDom
                 , getInitialState: js.UndefOr[ScopeU => State]
                 , componentWillMount: js.UndefOr[ScopeU => Unit]
                 , componentDidMount: js.UndefOr[ScopeM => Unit]
                 , componentWillUnmount: js.UndefOr[ScopeM => Unit]
                 , componentWillUpdate: js.UndefOr[(ScopeWU, Props, State) => Unit]
                 , componentDidUpdate: js.UndefOr[(ScopeM, Props, State) => Unit]
                  ) {

      def getInitialState(f: ScopeU => State): B3 = copy(getInitialState = f)
      def initialState(s: State): B3 = getInitialState(_ => s)
      def componentWillMount(f: ScopeU => Unit): B3 = copy(componentWillMount = f)
      def componentDidMount(f: ScopeM => Unit): B3 = copy(componentDidMount = f)
      def componentWillUnmount(f: ScopeM => Unit): B3 = copy(componentWillUnmount = f)
      def componentWillUpdate(f: (ScopeWU, Props, State) => Unit): B3 = copy(componentWillUpdate = f)
      def componentDidUpdate(f: (ScopeM, Props, State) => Unit): B3 = copy(componentDidUpdate = f)

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
        getInitialState.foreach(f => {
          val f2: ScopeU => WrapObj[State] = f.andThen(_.wrap)
          set(spec, "getInitialState", f2: js.ThisFunction)
        })
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

object ComponentBuilder {
  def apply[Props, State](name: String) = new ComponentBuilder[Props, State](name)
}