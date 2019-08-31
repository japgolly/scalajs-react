package japgolly.scalajs.react.vdom

import scala.scalajs.LinkingInfo.developmentMode
import scala.scalajs.js
import japgolly.scalajs.react.internal.JsUtil
import japgolly.scalajs.react.raw
import Builder.RawChild

/** Mutable target for immutable VDOM constituents to compose.
  */
trait Builder {
  val addAttr        : (String, js.Any) => Unit
  val addClassName   : js.Any           => Unit
  val addStyle       : (String, js.Any) => Unit
  val addStylesObject: js.Object        => Unit
  val appendChild    : RawChild         => Unit
  val setKey         : js.Any           => Unit

  final def addStyles(j: js.Any): Unit = {
    // Hack because Attr.ValueType.Fn takes a js.Any => Unit.
    // Safe because Attr.Style declares that it needs a js.Object.
    val obj = j.asInstanceOf[js.Object]
    addStylesObject(obj)
  }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

object Builder {
  type RawChild = raw.React.Node

  def setObjectKeyValue(o: js.Object, k: String, v: js.Any): Unit =
    o.asInstanceOf[js.Dynamic].updateDynamic(k)(v)

  def nonEmptyObject[O <: js.Object](o: O): js.UndefOr[O] =
    if (js.Object.keys(o).length == 0) js.undefined else o

  def nonEmptyJsArray[A](as: js.Array[A]): js.UndefOr[js.Array[A]] =
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

    override val addStyle: (String, js.Any) => Unit =
      setObjectKeyValue(styles, _, _)

    override val addStylesObject: js.Object => Unit =
      o => for ((k, v) <- JsUtil.objectIterator(o)) addStyle(k, v)

    override val appendChild: RawChild => Unit =
      children.push(_)

    override val setKey: js.Any => Unit =
      k => key = k

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

      val unoptimised: BuildFn =
        (tag, props, key, children) => {
          key.foreach(setObjectKeyValue(props, "key", _))
          raw.React.createElement(tag, props, children.toSeq: _*)
        }

      if (developmentMode)

      // Development mode
        unoptimised

      else {
        // Production mode
        // https://babeljs.io/docs/plugins/transform-react-inline-elements/
        // Taken from Babel @ 960fa66c9ef013e247311144332756cdfc9d51bc

        // To check for new changes:
        // before=960fa66c9ef013e247311144332756cdfc9d51bc
        // after=master
        // git diff -M -w -b $before..$after -- packages/babel-helpers/src/helpers.js
        // git diff -M -w -b $before..$after -- packages/babel-plugin-transform-react-inline-elements/test/fixtures/inline-elements

        val REACT_ELEMENT_TYPE: js.Any =
          try
            js.Dynamic.global.Symbol.`for`("react.element")
          catch {
            case _: Throwable => 0xeac7
          }

        (tag, props, key, children) => {

          // From packages/babel-plugin-transform-react-inline-elements/test/fixtures/inline-elements/ref-deopt
          val ref = props.asInstanceOf[js.Dynamic].ref.asInstanceOf[js.UndefOr[js.Any]]
          if (ref.isDefined)
            unoptimised(tag, props, key, children)

          else {
            // From packages/babel-helpers/src/helpers.js # jsx()

            val clen = children.length
            if (clen != 0) {
              val c = if (clen == 1) children(0) else children
              setObjectKeyValue(props, "children", c.asInstanceOf[js.Any])
            }

            val output =
              js.Dynamic.literal(
                `$$typeof` = REACT_ELEMENT_TYPE,
                `type`     = tag,
                key        = key.fold(null: js.Any)("" + _),
                ref        = null,
                props      = props,
                _owner     = null)
                .asInstanceOf[raw.React.Element]

            // org.scalajs.dom.console.log("VDOM: ", output)

            output
          }
        }
      }
    }
  }
}
