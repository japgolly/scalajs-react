package golly.react

import scala.scalajs.js

class ComponentSpecBuilder[Props <: golly.react.Props, State <: golly.react.State, Backend] {

  type Scope = ComponentScope[Props, State, Backend]

  def init(backend: Backend, render: Scope => VDom) =
    BuilderX(backend, render, null, null, null)

  case class BuilderX(
                       backend: Backend
                       , render: Scope => VDom
                       , getInitialState: Scope => State
                       , componentDidMount: Scope => Unit
                       , componentWillUnmount: Scope => Unit
                       ) {

    type This = BuilderX

    def getInitialState(f: Scope => State): This = copy(getInitialState = f)

    def initialState(s: State): This = getInitialState(_ => s)

    def componentDidMount(f: Scope => Unit): This = copy(componentDidMount = f)

    def componentWillUnmount(f: Scope => Unit): This = copy(componentWillUnmount = f)

    def build =
      js.Dynamic.literal(
        "_backend" -> WrapObj(backend)
        , "render" -> (render: js.ThisFunction)
        , "getInitialState" -> (getInitialState: js.ThisFunction)
        , "componentDidMount" -> (componentDidMount: js.ThisFunction)
        , "componentWillUnmount" -> (componentWillUnmount: js.ThisFunction)
      ).asInstanceOf[ComponentSpec[Props]]
  }
}
