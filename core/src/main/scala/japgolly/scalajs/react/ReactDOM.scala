package japgolly.scalajs.react

import org.scalajs.dom

object ReactDOM {
  def raw = japgolly.scalajs.react.raw.ReactDOM

  def unmountComponentAtNode(container: dom.Node): Boolean =
    raw.unmountComponentAtNode(container)

  // .hydrate is not here because currently, SSR with scalajs-react isn't directly supported.
  // .raw.hydrate can be used if needed.

  sealed trait NotAllowed

  // There are three ways of providing this functionality:
  // 1. ReactDOM.render here (problem: lose return type precision)
  // 2. ReactDOM.render{Js,Scala,Vdom,etc}
  // 3. Add .renderIntoDOM to mountable types (current solution)
  @deprecated("Use .renderIntoDOM on unmounted components.", "")
  def render(element  : NotAllowed,
             container: NotAllowed,
             callback : NotAllowed = null): Null = null

  @deprecated("Use .getDOMNode on mounted components.", "")
  def findDOMNode(comonent: NotAllowed): Null = null

  @deprecated("Import vdom and use ReactPortal()", "")
  def createPortal(child: NotAllowed, container: NotAllowed): Null = null

}