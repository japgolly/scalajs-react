package japgolly.scalajs.react.vdom

import scala.scalajs.LinkingInfo.developmentMode
import scala.scalajs.js
import japgolly.scalajs.react.internal.JsUtil
import japgolly.scalajs.react.raw

object Builder {
  @inline private[Builder] def set(o: js.Object, k: String, v: js.Any): Unit =
    o.asInstanceOf[js.Dynamic].updateDynamic(k)(v)

  type BuildFn = (String, js.Object, js.Array[raw.ReactNodeList]) => raw.ReactElement

  val buildFn: BuildFn =
    if (developmentMode)

      // Development mode
      (tag, props, children) => {
        raw.React.createElement(tag, props, children: _*)
      }

    else {

      // Production mode
      // http://babeljs.io/blog/2015/03/31/5.0.0/#inline-elements

      // Logic here taken from:
      // https://github.com/babel/babel/blob/master/packages/babel-helpers/src/helpers.js
      // https://github.com/babel/babel/tree/master/packages/babel-plugin-transform-react-inline-elements/test/fixtures/inline-elements

      val REACT_ELEMENT_TYPE: js.Any =
        try
          js.Dynamic.global.Symbol.`for`("react.element")
        catch {
          case _: Throwable => 0xeac7
        }

      (tag, props, children) => {
        val ref = props.asInstanceOf[js.Dynamic].ref.asInstanceOf[js.UndefOr[js.Any]]
        if (ref.isDefined)
          raw.React.createElement(tag, props, children: _*)
        else {

          val key = props.asInstanceOf[js.Dynamic].key.asInstanceOf[js.UndefOr[js.Any]]

          val clen = children.length
          if (clen != 0) {
            val c = if (clen == 1) children(0) else children
            set(props, "children", c.asInstanceOf[js.Any])
          }

          val output =
            js.Dynamic.literal(
              `$$typeof` = REACT_ELEMENT_TYPE,
              `type`     = tag,
              key        = key.fold(null: js.Any)("" + _),
              ref        = null,
              props      = props,
              _owner     = null)
              .asInstanceOf[raw.ReactElement]

//         org.scalajs.dom.console.log("VDOM: ", output)

          output
        }
      }
    }
}

final class Builder {
  import Builder.set

  private[this] var className: js.UndefOr[js.Any] = js.undefined
  private[this] var props    = new js.Object
  private[this] var style    = new js.Object
  private[this] var children = new js.Array[raw.ReactNodeList]()

  val addClassName: js.Any => Unit =
    n => className = className.fold(n)(m => s"$m $n")

  def appendChild(n: raw.ReactNodeList): Unit =
    children.push(n)

  def addAttr(k: String, v: js.Any): Unit =
    set(props, k, v)

  def addStyle(k: String, v: js.Any): Unit =
    set(style, k, v)

  val addStyles: js.Any => Unit = {
    case obj: js.Object =>
      for ((k,v) <- JsUtil.objectIterator(obj))
        addStyle(k, v)
  }

  @inline private[this] def hasStyle: Boolean =
    js.Object.keys(style).length != 0

  def render(tag: String): ReactElement = {
    className.foreach(set(props, "className", _))
    if (hasStyle)
      set(props, "style", style)
    ReactElement(Builder.buildFn(tag, props, children))
  }
}
