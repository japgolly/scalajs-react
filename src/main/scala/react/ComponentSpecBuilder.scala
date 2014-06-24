package golly.react

import scala.scalajs.js

class ComponentSpecBuilder[Props, State, Backend](backend: => Backend) {

  type Scope = ComponentScope[Props, State, Backend]

  def render(render: Scope => VDom) = B(() => backend, render, null, null, null)

  case class B(backend: () => Backend
               , render: Scope => VDom
               , getInitialState: Scope => State
               , componentDidMount: Scope => Unit
               , componentWillUnmount: Scope => Unit
                ) {
    type This = B

    def getInitialState(f: Scope => State): This = copy(getInitialState = f)

    def initialState(s: State): This = getInitialState(_ => s)

    def componentDidMount(f: Scope => Unit): This = copy(componentDidMount = f)

    def componentWillUnmount(f: Scope => Unit): This = copy(componentWillUnmount = f)

    def build = {
      // TODO nulls, ew
      val _getInitialState: Scope => WrapObj[State] = getInitialState.andThen(_.wrap)

      js.Dynamic.literal(
        "_backend" -> WrapObj(backend())
        , "render" -> (render: js.ThisFunction)
        , "getInitialState" -> (_getInitialState: js.ThisFunction)
        , "componentDidMount" -> (componentDidMount: js.ThisFunction)
        , "componentWillUnmount" -> (componentWillUnmount: js.ThisFunction)
      ).asInstanceOf[ComponentSpec[Props]]
    }

    def createClass = React.createClass(build)
  }

}
