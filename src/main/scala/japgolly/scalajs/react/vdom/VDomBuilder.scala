package japgolly.scalajs.react.vdom

import scala.scalajs.js
import japgolly.scalajs.react.React

private[vdom] object VDomBuilder  {

  val specialCaseAttrs =
    Set("onBlur", "onChange", "onClick", "onFocus", "onKeyDown", "onKeyPress", "onKeyUp", "onLoad", "onMouseDown"
      , "onMouseMove", "onMouseOut", "onMouseOver", "onMouseUp", "onSelect", "onScroll", "onSubmit", "onReset"
      , "readOnly")

  val attrTranslations = specialCaseAttrs.toList.map(x => x.toLowerCase -> x).toMap
}

private[vdom] final class VDomBuilder {
  import VDomBuilder._

  private[this] var props = new js.Object
  private[this] var style = new js.Object
  private[this] var vdomArgs = js.Array[ReactFragT](props)

  def appendChild(c: ReactFragT): Unit =
    vdomArgs.push(c)

  def addAttr(k: String, v: js.Any): Unit =
    set(props, attrTranslations.getOrElse(k, k), v)

  def addStyle(k: String, v: String): Unit =
    set(style, k, v)

  @inline private[this] def set(o: js.Object, k: String, v: js.Any): Unit =
    o.asInstanceOf[js.Dynamic].updateDynamic(k)(v)

  @inline private[this] def hasStyle = js.Object.keys(style).length != 0

  def render(tag: String) = {
    if (hasStyle) set(props, "style", style)
    React.DOM.applyDynamic(tag)(vdomArgs: _*).asInstanceOf[ReactOutput]
  }
}
