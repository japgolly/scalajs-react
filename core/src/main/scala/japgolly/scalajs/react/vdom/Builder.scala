package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.internal.JsUtil
import japgolly.scalajs.react.raw
import japgolly.scalajs.react.vdom.Builder.RawChild
import japgolly.scalajs.react.vdom.Builder.RawRefFn
import scala.scalajs.js

/** Mutable target for immutable VDOM constituents to compose.
  */
trait Builder {
  val addAttr        : (String, js.Any)                     => Unit
  val addClassName   : js.Any                               => Unit
  val addEventHandler: (String, js.Function1[js.Any, Unit]) => Unit
  val addStyle       : (String, js.Any)                     => Unit
  val addStylesObject: js.Object                            => Unit
  val appendChild    : RawChild                             => Unit
  val setKey         : js.Any                               => Unit
  def addRefFn[A]    : RawRefFn[A]                          => Unit

  final def addStyles(j: js.Any): Unit = {
    // Hack because Attr.ValueType.Fn takes a js.Any => Unit.
    // Safe because Attr.Style declares that it needs a js.Object.
    val obj = j.asInstanceOf[js.Object]
    addStylesObject(obj)
  }

  // NOTE: This method isn't used internally. It is intended for advanced usage.
  // Eg: facades for components that implement the "child function" pattern.
  def addAttrsObject(o:  js.Object, except: Set[String] = Set.empty): Unit =
    for ((k, v) <- JsUtil.objectIterator(o) if !except.contains(k))
      addAttr(k, v)
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

object Builder {
  type RawChild = raw.React.Node
  type RawRefFn[A] = raw.React.RefFn[A] 

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
  trait ToJs extends Builder {
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

    override val addAttr: (String, js.Any) => Unit =
      setObjectKeyValue(props, _, _)

    override val addClassName: js.Any => Unit =
      n => nonEmptyClassName = nonEmptyClassName.fold(n)(_.toString + " " + n)

    override val addEventHandler =
      (k, g) => modObjectKeyValue[js.Function1[js.Any, Unit]](
        props,
        k,
        c => if (c.isEmpty) g else {val f = c.get; e => {f(e); g(e)}})

    override val addStyle: (String, js.Any) => Unit =
      setObjectKeyValue(styles, _, _)

    override val addStylesObject: js.Object => Unit =
      o => for ((k, v) <- JsUtil.objectIterator(o)) addStyle(k, v)

    override val appendChild: RawChild => Unit =
      children.push(_)

    override val setKey: js.Any => Unit =
      k => key = k

    override def addRefFn[A]: RawRefFn[A] => Unit =
      refFn => addAttr("ref", refFn)

    def addKeyToProps(): Unit =
      key.foreach(setObjectKeyValue(props, "key", _))

    def addClassNameToProps(): Unit =
      nonEmptyClassName.foreach(setObjectKeyValue(props, "className", _))

    def addStyleToProps(): Unit =
      nonEmptyStyles.foreach(setObjectKeyValue(props, "style", _))

    def childrenAsVdomNodes: List[VdomNode] = {
      import Implicits._
      var i = children.length
      var nodes = List.empty[VdomNode]
      while (i > 0) {
        i -= 1
        nodes ::= children(i)
      }
      nodes
    }

    def nonEmptyChildrenAsVdomNodes: js.UndefOr[List[VdomNode]] =
      if (children.length == 0) js.undefined else childrenAsVdomNodes
  }

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  class ToVdomElement extends ToJs {
    def render(tag: String): VdomElement = {
      val r = (new ToRawReactElement).render(tag)
      VdomElement(r)
    }
  }

  class ToRawReactElement extends ToJs {
    def render(tag: String): raw.React.Element = {
      addClassNameToProps()
      addStyleToProps()
      ToRawReactElement.build(tag, props, key, children)
    }
  }

  object ToRawReactElement {
    // type, props, key, children
    type BuildFn = (String, js.Object, js.UndefOr[js.Any], js.Array[RawChild]) => raw.React.Element

    val build: BuildFn = {

      // This used to copy what Babel's transform-react-inline-elements plugin does, but I found a case that broke
      // that I couldn't fix. As a result, it's all gone. We now have the slightly slower, but safer, uniform method
      // that's used in both fastOptJS and fullOptJS.

      // The issue: (top-level) <Main><div><A/><B/><C/></div></Main>
      // I would continually get this warning: Each child in a list should have a unique "key" prop.
      // There were no array children, keys aren't needed. What really makes this weird and let to me giving up is that
      // if you keep the optimised code as is, but then just call
      //     raw.React.createElement(tag, js.Object(), children.toSeq: _*)
      // and throw away the result, the warning would disappear (!). There seems to be some mutation going on somewhere
      // that I can't find. I've inspected all the data I've got access to, looked through React code itself; I can't
      // find where this mutation is occurring. If it's this hard to track down, I don't want scalajs-react being
      // brittle. I don't want it to break when React upgrade their internals. Frustratedly, in to the bin it all goes.

      (tag, props, key, children) => {
        key.foreach(setObjectKeyValue(props, "key", _))
        raw.React.createElement(tag, props, children.toSeq: _*)
      }

    }
  }
}
