package japgolly.scalajs.react

import japgolly.scalajs.react.ReactComponentC.{ConstProps, DefaultProps, ReqProps}

import scala.scalajs.js

object ElementFactory {
  /**
   * add types to js constructor
   * @param ctor
   * @tparam P
   * @tparam S
   * @return
   */
  private def getComponentConstructor[P, S, N <: TopNode](ctor: js.Dynamic): ReactClass[P, S, Unit, N] = {
    ctor.asInstanceOf[ReactClass[P, S, Unit, N]]
  }

  def noProps[S, N <: TopNode](cls: js.Dynamic, c: Class[_ <: BasicReactComponent[Unit, S, N]]) = {
    val ctor = getComponentConstructor[Unit, S, N](cls)
    val factory = React.createFactory[Unit, S, Unit, N](ctor)
    new ConstProps[Unit, S, Unit, N](factory, ctor, js.undefined, js.undefined, () => Unit)
  }

  def constantProps[P, S, N <: TopNode](cls: js.Dynamic, c: Class[_ <: BasicReactComponent[P, S, N]])(props: P) = {
    val ctor = getComponentConstructor[P, S, N](cls)
    val factory = React.createFactory[P, S, Unit, N](ctor)
    new ConstProps[P, S, Unit, N](factory, ctor, js.undefined, js.undefined, () => props)
  }

  def defaultProps[P, S, N <: TopNode](cls: js.Dynamic, c: Class[_ <: BasicReactComponent[P, S, N]])(defaultProps: P) = {
    val ctor = getComponentConstructor[P, S, N](cls)
    val factory = React.createFactory[P, S, Unit, N](ctor)
    new DefaultProps[P, S, Unit, N](factory, ctor, js.undefined, js.undefined, () => defaultProps)
  }

  def requiredProps[P, S, N <: TopNode](cls: js.Dynamic, c: Class[_ <: BasicReactComponent[P, S, N]]) = {
    val ctor = getComponentConstructor[P, S, N](cls)
    val factory = React.createFactory[P, S, Unit, N](ctor)
    new ReqProps[P, S, Unit, N](factory, ctor, js.undefined, js.undefined)
  }
}
