package golly.react

import scala.scalajs.js

final case class ComponentSpecBuilder[C <: Component](
  render: C#Scope => VDom
  , getInitialState: () => C#S
  , componentDidMount: C#Scope => Unit
  , componentWillUnmount: C#Scope => Unit
  ) {

  def getInitialState(f: () => C#S): ComponentSpecBuilder[C] =
    copy(getInitialState = f)

  def initialState(s: C#S): ComponentSpecBuilder[C] =
    getInitialState(() => s)

  def componentDidMount(f: C#Scope => Unit): ComponentSpecBuilder[C] =
    copy(componentDidMount = f)

  def componentWillUnmount(f: C#Scope => Unit): ComponentSpecBuilder[C] =
    copy(componentWillUnmount = f)

  def build =
    js.Dynamic.literal(
      "render" -> (render: js.ThisFunction)
      , "getInitialState" -> (getInitialState: js.Function)
      , "componentDidMount" -> (componentDidMount: js.ThisFunction)
      , "componentWillUnmount" -> (componentWillUnmount: js.ThisFunction)
    ).asInstanceOf[C#Spec]
}

object ComponentSpecBuilder {
  def apply[C <: Component] = BuildInitS.subst[C]

  sealed trait BuildInit[C <: Component] {
    def render(render: C#Scope => VDom) = new ComponentSpecBuilder[C](render, null, null, null)
  }

  private[this] object BuildInitS extends BuildInit[Nothing] {
    @inline def subst[C <: Component] = this.asInstanceOf[BuildInit[C]]
  }
}
