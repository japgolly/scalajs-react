package golly.react

import scala.scalajs.js

final case class ComponentSpecBuilder[C <: Component](
  backend: C#Backend
  , render: C#Scope => VDom
  , getInitialState: C#Scope => C#S
  , componentDidMount: C#Scope => Unit
  , componentWillUnmount: C#Scope => Unit
  ) {

  def getInitialState(f: C#Scope => C#S): ComponentSpecBuilder[C] =
    copy(getInitialState = f)

  def initialState(s: C#S): ComponentSpecBuilder[C] =
    getInitialState(_ => s)

  def componentDidMount(f: C#Scope => Unit): ComponentSpecBuilder[C] =
    copy(componentDidMount = f)

  def componentWillUnmount(f: C#Scope => Unit): ComponentSpecBuilder[C] =
    copy(componentWillUnmount = f)

  def build =
    js.Dynamic.literal(
      "_sc_b" -> WrapObj(backend)
      , "render" -> (render: js.ThisFunction)
      , "getInitialState" -> (getInitialState: js.ThisFunction)
      , "componentDidMount" -> (componentDidMount: js.ThisFunction)
      , "componentWillUnmount" -> (componentWillUnmount: js.ThisFunction)
    ).asInstanceOf[C#Spec]
}

object ComponentSpecBuilder {
  class Init[C <: Component](b: C#Backend) {
    def render(render: C#Scope => VDom) = new ComponentSpecBuilder[C](b, render, null, null, null)
  }
}
