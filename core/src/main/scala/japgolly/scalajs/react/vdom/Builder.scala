package japgolly.scalajs.react.vdom

import scala.scalajs.js
import japgolly.scalajs.react.{ReactElement, ReactNode, React}

private[vdom] final class Builder {

  private[this] var className: js.UndefOr[js.Any] = js.undefined
  private[this] var props    = new js.Object
  private[this] var style    = new js.Object
  private[this] var children = new js.Array[ReactNode]()

  def addClassName(n: js.Any): Unit =
    className = className.fold(n)(m => s"$m $n")

  def appendChild(c: ReactNode): Unit =
    children.push(c)

  def addAttr(k: String, v: js.Any): Unit =
    set(props, k, v)

  def addStyle(k: String, v: String): Unit =
    set(style, k, v)

  @inline private[this] def set(o: js.Object, k: String, v: js.Any): Unit =
    o.asInstanceOf[js.Dynamic].updateDynamic(k)(v)

  @inline private[this] def hasStyle: Boolean =
    js.Object.keys(style).length != 0

  def render(tag: String): ReactElement = {
    className.foreach(set(props, "className", _))
    if (hasStyle)
      set(props, "style", style)
    React.createElement(tag, props, children: _*)
  }
}
