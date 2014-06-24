package golly.react

import scala.scalajs.js

class ComponentBuilder[Props, State, Backend](backend: js.UndefOr[() => Backend]) {

  type Scope = ComponentScope[Props, State, Backend]

  def render(render: Scope => VDom) =
    B(backend, render, js.undefined, js.undefined, js.undefined)

  case class B(backend: js.UndefOr[() => Backend]
               , render: Scope => VDom
               , getInitialState: js.UndefOr[Scope => State]
               , componentDidMount: js.UndefOr[Scope => Unit]
               , componentWillUnmount: js.UndefOr[Scope => Unit]
                ) {

    def getInitialState(f: Scope => State): B = copy(getInitialState = f)
    def initialState(s: State): B = getInitialState(_ => s)
    def componentDidMount(f: Scope => Unit): B = copy(componentDidMount = f)
    def componentWillUnmount(f: Scope => Unit): B = copy(componentWillUnmount = f)

    def buildSpec = {
      @inline def set(o: js.Object, k: String, v: js.Any): Unit = o.asInstanceOf[js.Dynamic].updateDynamic(k)(v) // TODO share
      val spec = js.Dynamic.literal("render" -> (render: js.ThisFunction)).asInstanceOf[js.Object]
      backend.foreach(f => set(spec, "_backend", f().wrap))
      getInitialState.foreach(f => {
        val f2: Scope => WrapObj[State] = f.andThen(_.wrap)
        set(spec, "getInitialState", f2: js.ThisFunction)
      })
      componentDidMount.foreach(f => set(spec, "componentDidMount", f: js.ThisFunction))
      componentWillUnmount.foreach(f => set(spec, "componentWillUnmount", f: js.ThisFunction))
      spec.asInstanceOf[ComponentSpec[Props]]
    }

    def build = React.createClass(buildSpec)
  }
}

object ComponentBuilder {
  def apply[Props, State] = new {

    def backend[Backend](backend: => Backend) =
      new ComponentBuilder[Props, State, Backend](() => backend)

    def render(render: ComponentScope[Props, State, Unit] => VDom) =
      new ComponentBuilder[Props, State, Unit](js.undefined).render(render)
  }
}