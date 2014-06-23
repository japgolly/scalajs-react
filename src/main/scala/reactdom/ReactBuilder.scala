package golly.react.scalatags

import scala.scalajs.js

final class ReactBuilder {
  private[this] var props = new js.Object
  private[this] var style = new js.Object

  // This is (mostly) copied from scalatags.text.Builder for speed
  private[this] var children = new Array[ReactFragT](5)
  private[this] var childIndex = 1
  private[this] def ensureArrayCapacity(): Unit = {
    if (childIndex >= children.length) {
      val newArr = new Array[ReactFragT](children.length * 2)
      var i = children.length
      while (i > 0) {
        i -= 1
        newArr(i) = children(i)
      }
      children = newArr
    }
  }

  def appendChild(c: ReactFragT): Unit = {
    ensureArrayCapacity()
    children(childIndex) = c
    childIndex += 1
  }

  def addAttr(k: String, v: js.Any): Unit = set(props, k, v)
  def addStyle(k: String, v: String): Unit = set(style, k, v)

  @inline private[this] def set(o: js.Object, k: String, v: js.Any): Unit =
    o.asInstanceOf[js.Dynamic].updateDynamic(k)(v)

  @inline private[this] def hasStyle = js.Object.keys(style).length != 0

  def render(tag: String) = {
    if (hasStyle) set(props, "style", style)
    children(0) = props
    js.Dynamic.global.React.DOM.applyDynamic(tag)(children: _*).asInstanceOf[ReactOutput]
  }
}
