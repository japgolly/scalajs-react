package golly.react

import scala.scalajs.js

class ComponentBuilder[Props, State, Backend]{

  type ScopeU = ComponentScopeU[Props, State, Backend]
  type ScopeM = ComponentScopeM[Props, State, Backend]

  def render(render: ScopeU => VDom) =
    B(render, js.undefined, js.undefined, js.undefined, js.undefined, js.undefined, js.undefined)

  case class B(render: ScopeU => VDom
               , backend: js.UndefOr[ScopeM => Backend]
               , displayName: js.UndefOr[String]
               , getInitialState: js.UndefOr[ScopeU => State]
               , componentWillMount: js.UndefOr[ScopeU => Unit]
               , componentDidMount: js.UndefOr[ScopeM => Unit]
               , componentWillUnmount: js.UndefOr[ScopeM => Unit]
                ) {

    def getInitialState(f: ScopeU => State): B = copy(getInitialState = f)
    def initialState(s: State): B = getInitialState(_ => s)
    def componentWillMount(f: ScopeU => Unit): B = copy(componentWillMount = f)
    def componentDidMount(f: ScopeM => Unit): B = copy(componentDidMount = f)
    def componentWillUnmount(f: ScopeM => Unit): B = copy(componentWillUnmount = f)
    def backend(f: ScopeM => Backend): B = copy(backend = f)
    def displayName(name: String): B = copy(displayName = name)

    def buildSpec = {
      @inline def set(o: js.Object, k: String, v: js.Any): Unit = o.asInstanceOf[js.Dynamic].updateDynamic(k)(v) // TODO share
      val spec = js.Dynamic.literal("render" -> (render: js.ThisFunction)).asInstanceOf[js.Object]

      var componentWillMount2 = componentWillMount
      backend.foreach(f => {
        set(spec, "_backend", "PENDING...")
        componentWillMount2 = (t: ScopeU) => {
          val scopeM = t.asInstanceOf[ScopeM] // It will be mounted when the backend uses it
          t.asInstanceOf[js.Dynamic].updateDynamic("_backend")(WrapObj(f(scopeM)))
          componentWillMount.foreach(g => g(t))
        }
      })
      
      displayName.foreach(set(spec, "displayName", _))
      getInitialState.foreach(f => {
        val f2: ScopeU => WrapObj[State] = f.andThen(_.wrap)
        set(spec, "getInitialState", f2: js.ThisFunction)
      })
      componentWillMount2.foreach(f => set(spec, "componentWillMount", f: js.ThisFunction))
      componentDidMount.foreach(f => set(spec, "componentDidMount", f: js.ThisFunction))
      componentWillUnmount.foreach(f => set(spec, "componentWillUnmount", f: js.ThisFunction))
      spec.asInstanceOf[ComponentSpec[Props]]
    }

    def build = React.createClass(buildSpec)
  }
}

object ComponentBuilder {
  def apply[Props, State, Backend] = new ComponentBuilder[Props, State, Backend]
}