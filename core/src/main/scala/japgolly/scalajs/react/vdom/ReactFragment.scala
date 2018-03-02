package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{Key, raw => Raw}
import scala.scalajs.js

object ReactFragment {

  /** Elements keys are optional. */
  def apply(ns: VdomNode*): VdomElement =
    create(null, ns: _*)

  /** Elements keys are optional. */
  def withKey(key: Key)(ns: VdomNode*): VdomElement = {
    val jsKey: Raw.React.Key = key
    val props = js.Dynamic.literal("key" -> jsKey.asInstanceOf[js.Any])
    create(props, ns: _*)
  }

  private def create(props: js.Object, ns: VdomNode*): VdomElement =
    VdomElement(Raw.React.createElement(Raw.React.Fragment, props, ns.map(_.rawNode): _*))
}
