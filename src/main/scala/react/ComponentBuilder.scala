package golly.react

import scala.scalajs.js

class ComponentBuilder[Props, State, Backend]{

  type Scope = ComponentScope[Props, State, Backend]

  def render(render: Scope => VDom) =
    B(render, js.undefined, js.undefined, js.undefined, js.undefined, js.undefined, js.undefined)

  case class B(render: Scope => VDom
               , backend: js.UndefOr[Scope => Backend]
               , displayName: js.UndefOr[String]
               , getInitialState: js.UndefOr[Scope => State]
               , componentWillMount: js.UndefOr[Scope => Unit]
               , componentDidMount: js.UndefOr[Scope => Unit]
               , componentWillUnmount: js.UndefOr[Scope => Unit]
                ) {

    def getInitialState(f: Scope => State): B = copy(getInitialState = f)
    def initialState(s: State): B = getInitialState(_ => s)
    def componentWillMount(f: Scope => Unit): B = copy(componentWillMount = f)
    def componentDidMount(f: Scope => Unit): B = copy(componentDidMount = f)
    def componentWillUnmount(f: Scope => Unit): B = copy(componentWillUnmount = f)
    def backend(f: Scope => Backend): B = copy(backend = f)

    def buildSpec = {
      @inline def set(o: js.Object, k: String, v: js.Any): Unit = o.asInstanceOf[js.Dynamic].updateDynamic(k)(v) // TODO share
      val spec = js.Dynamic.literal("render" -> (render: js.ThisFunction)).asInstanceOf[js.Object]

      var componentWillMount2 = componentWillMount
      backend.foreach(f => {
        set(spec, "_backend", "PENDING...")
        componentWillMount2 = (t: Scope) => {
          t.asInstanceOf[js.Dynamic].updateDynamic("_backend")(WrapObj(f(t)))
          componentWillMount.foreach(g => g(t))
        }
      })
      
      displayName.foreach(set(spec, "displayName", _))
      getInitialState.foreach(f => {
        val f2: Scope => WrapObj[State] = f.andThen(_.wrap)
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