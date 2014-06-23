package golly.react.scalatags

import scala.scalajs.js

class ReactBuilder {
  private[this] var props = (new js.Object).asInstanceOf[js.Dynamic]
  private[this] var style = (new js.Object).asInstanceOf[js.Dynamic]
  private[this] var children = Vector.empty[js.Any]

  def addAttr(k: String, v: js.Any): Unit =
    props.updateDynamic(k)(v)

  def addStyle(k: String, v: String): Unit = {
    style.updateDynamic(k)(v)
    props.updateDynamic("style")(style)
  }

  def appendChild(c: js.Any): Unit =
    children = children :+ c

  def render(tag: String) = {
    val args = props +: children
    js.Dynamic.global.React.DOM.applyDynamic(tag)(args: _*).asInstanceOf[ReactOutput]
  }
}