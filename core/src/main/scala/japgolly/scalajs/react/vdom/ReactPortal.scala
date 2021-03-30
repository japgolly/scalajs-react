package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.raw.ReactDOM

object ReactPortal {

  def apply(child: VdomNode, container: ReactDOM.Container): VdomNode =
    VdomNode(ReactDOM.createPortal(child.rawNode, container))

}
