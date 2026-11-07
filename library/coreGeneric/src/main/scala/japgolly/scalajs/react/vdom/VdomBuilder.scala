package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.facade
import japgolly.scalajs.react.util.JsUtil
import japgolly.scalajs.react.vdom.VdomBuilder.{RawChild, RawRefFn}
import scala.scalajs.js

/** Mutable target for immutable VDOM constituents to compose.
  */
trait VdomBuilder {
  def addAttr        (name: String, value: js.Any): Unit
  def addClassName   (className: js.Any): Unit
  def addEventHandler(name: String, handler: js.Function1[js.Any, Any]): Unit
  def addStyle       (name: String, value: js.Any): Unit
  def addStylesObject(styles: js.Object): Unit
  def appendChild    (child: RawChild): Unit
  def setKey         (key: js.Any): Unit
  def addRefFn[A]    (refFn: RawRefFn[A]): Unit

  final def addStyles(j: js.Any): Unit = {
    // Hack because Attr.ValueType.Fn takes a js.Any => Unit.
    // Safe because Attr.Style declares that it needs a js.Object.
    val obj = j.asInstanceOf[js.Object]
    addStylesObject(obj)
  }

  // NOTE: This method isn't used internally. It is intended for advanced usage.
  // Eg: facades for components that implement the "child function" pattern.
  def addAttrsObject(o: js.Object, allowAttr: String => Boolean = _ => true): Unit =
    for ((k, v) <- JsUtil.objectIterator(o) if allowAttr(k))
      addAttr(k, v)
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

object VdomBuilder {
  type RawChild = facade.React.Node
  type RawRefFn[A] = facade.React.RefFn[A]

  @inline def setObjectKeyValue(o: js.Object, k: String, v: js.Any): Unit =
    o.asInstanceOf[js.Dynamic].updateDynamic(k)(v)

  @inline def modObjectKeyValue[A <: js.Any](o: js.Object, k: String, v: js.UndefOr[A] => A): Unit = {
    val cur = o.asInstanceOf[js.Dynamic].selectDynamic(k).asInstanceOf[js.UndefOr[A]]
    o.asInstanceOf[js.Dynamic].updateDynamic(k)(v(cur))
  }

  @inline def nonEmptyObject[O <: js.Object](o: O): js.UndefOr[O] =
    if (js.Object.keys(o).length == 0) js.undefined else o

  @inline def nonEmptyJsArray[A](as: js.Array[A]): js.UndefOr[js.Array[A]] =
    if (as.length == 0) js.undefined else as

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  /**
    * Raw JS values of:
    * - className
    * - key
    * - props
    * - styles
    * - children
    *
    * Everything here is mutable.
    *
    * There are convenience methods to (mutably) add className and styles to props.
    */
  trait ToJs extends VdomBuilder {
    // Exposing vars here is acceptable because:
    // 1. The contents are all mutable anyway and a defensive-copy cost isn't worth it
    // 2. None of this is visible by default in the main public API
    // 3. Target audience is hackers doing hacky things, so more control is preferred

    var props    : js.Object          = new js.Object
    var styles   : js.Object          = new js.Object
    var children : js.Array[RawChild] = new js.Array[RawChild]()
    var key      : js.UndefOr[js.Any] = js.undefined

    var nonEmptyClassName: js.UndefOr[js.Any            ] = js.undefined
    def nonEmptyProps    : js.UndefOr[js.Object         ] = nonEmptyObject(props)
    def nonEmptyStyles   : js.UndefOr[js.Object         ] = nonEmptyObject(styles)
    def nonEmptyChildren : js.UndefOr[js.Array[RawChild]] = nonEmptyJsArray(children)

    override def addAttr(name: String, value: js.Any): Unit =
      setObjectKeyValue(props, name, value)

    override def addClassName(className: js.Any): Unit =
      nonEmptyClassName = nonEmptyClassName.fold[js.Any](className)(_.toString + " " + className)

    override def addEventHandler(k: String, g: js.Function1[js.Any, Any]): Unit =
      modObjectKeyValue[js.Function1[js.Any, Any]](
        props,
        k,
        c => if (c.isEmpty) g else {val f = c.get; e => {f(e); g(e)}})

    override def addStyle(name: String, value: js.Any): Unit =
      setObjectKeyValue(styles, name, value)

    override def addStylesObject(styles: js.Object): Unit =
      for ((k, v) <- JsUtil.objectIterator(styles)) addStyle(k, v)

    override def appendChild(child: RawChild): Unit =
      children.push(child)

    override def setKey(key: js.Any): Unit =
      this.key = key

    override def addRefFn[A](refFn: RawRefFn[A]): Unit =
      addAttr("ref", refFn)

    def addKeyToProps(): Unit =
      key.foreach(setObjectKeyValue(props, "key", _))

    def addClassNameToProps(): Unit =
      nonEmptyClassName.foreach(setObjectKeyValue(props, "className", _))

    def addStyleToProps(): Unit =
      nonEmptyStyles.foreach(setObjectKeyValue(props, "style", _))

    def childrenAsVdomNodes: List[VdomNode] = {
      var i = children.length
      var nodes = List.empty[VdomNode]
      while (i > 0) {
        i -= 1
        nodes ::= VdomNode(children(i))
      }
      nodes
    }

    def nonEmptyChildrenAsVdomNodes: js.UndefOr[List[VdomNode]] =
      if (children.length == 0) js.undefined else childrenAsVdomNodes
  }

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  class ToRawReactElement extends ToJs {
    def render(tag: String): facade.React.Element = {
      addClassNameToProps()
      addStyleToProps()
      ToRawReactElement.build(tag, props, key, children)
    }
  }

  object ToRawReactElement {
    // type, props, key, children
    type BuildFn = (String, js.Object, js.UndefOr[js.Any], js.Array[RawChild]) => facade.React.Element

    val build: BuildFn = {

      // This used to copy what Babel's transform-react-inline-elements plugin does, but I found a case that broke
      // that I couldn't fix. As a result, it's all gone. We now have the slightly slower, but safer, uniform method
      // that's used in both fastOptJS and fullOptJS.

      // The issue: (top-level) <Main><div><A/><B/><C/></div></Main>
      // I would continually get this warning: Each child in a list should have a unique "key" prop.
      // There were no array children, keys aren't needed. What really makes this weird and let to me giving up is that
      // if you keep the optimised code as is, but then just call
      //     facade.React.createElement(tag, js.Object(), children.toSeq: _*)
      // and throw away the result, the warning would disappear (!). There seems to be some mutation going on somewhere
      // that I can't find. I've inspected all the data I've got access to, looked through React code itself; I can't
      // find where this mutation is occurring. If it's this hard to track down, I don't want scalajs-react being
      // brittle. I don't want it to break when React upgrade their internals. Frustratedly, in to the bin it all goes.

      (tag, props, key, children) => {
        key.foreach(setObjectKeyValue(props, "key", _))
        facade.React.createElement(tag, props, children.toSeq: _*)
      }

    }
  }
}
