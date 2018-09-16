package japgolly.scalajs.react.feature

import japgolly.scalajs.react.{Key, raw => Raw}
import japgolly.scalajs.react.vdom._
import scala.scalajs.js

object ReactFragment {

  /** Unlike [[VdomArray]],
    *
    * - This is immutable.
    * - Elements may, but needn't have keys.
    * - The result can be assigned a key.
    */
  def apply(ns: VdomNode*): VdomElement =
    create(null, ns: _*)

  /** Unlike [[VdomArray]],
    *
    * - This is immutable.
    * - Elements may, but needn't have keys.
    * - The result can be assigned a key.
    */
  def withKey(key: Key)(ns: VdomNode*): VdomElement = {
    val jsKey: Raw.React.Key = key
    val props = js.Dynamic.literal("key" -> jsKey.asInstanceOf[js.Any])
    create(props, ns: _*)
  }

  private def create(props: js.Object, ns: VdomNode*): VdomElement =
    VdomElement(Raw.React.createElement(Raw.React.Fragment, props, ns.map(_.rawNode): _*))
}
