package japgolly.scalajs.react.vdom2

import japgolly.scalajs.react.{Callback, raw => Raw}
import japgolly.scalajs.react.vdom.Builder.RawChild
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.|

object Sketch {
  type TopNode     = dom.Node
  type HtmlTopNode = dom.html.Element
  type SvgTopNode  = dom.svg.Element

  trait Builder {
    val addAttr        : (String, js.Any) => Unit
    val addClassName   : js.Any           => Unit
    val addStyle       : (String, js.Any) => Unit
    val addStylesObject: js.Object        => Unit
    val appendChild    : RawChild         => Unit
    val setKey         : js.Any           => Unit
    final def addStyles(j: js.Any): Unit = ???
  }

  // TagMod   x TagMod   = TagMod
  // TagMod   x Tag      = TagMod
  // TagMod   x Node     = TagMod
  // TagMod   x Array    = TagMod
  // TagMod   x Element  = TagMod
  // TagMod   x Fragment = TagMod
  // Tag      x TagMod   = Tag
  // Tag      x Tag      = ++ Fragment | apply Tag
  // Tag      x Node     = ++ Fragment | apply Tag
  // Tag      x Array    = ++ Fragment | apply Tag
  // Tag      x Element  = ++ Fragment | apply Tag
  // Tag      x Fragment = ++ Fragment | apply Tag
  // Node     x TagMod   = -
  // Node     x Tag      = Fragment
  // Node     x Node     = Fragment
  // Node     x Array    = Fragment
  // Node     x Element  = Fragment
  // Node     x Fragment = Fragment
  // Array    x TagMod   = -
  // Array    x Tag      = Fragment
  // Array    x Node     = Fragment
  // Array    x Array    = Fragment
  // Array    x Element  = Fragment
  // Array    x Fragment = Fragment
  // Element  x TagMod   = -
  // Element  x Tag      = Fragment
  // Element  x Node     = Fragment
  // Element  x Array    = Fragment
  // Element  x Element  = Fragment
  // Element  x Fragment = Fragment
  // Fragment x TagMod   = -
  // Fragment x Tag      = Fragment
  // Fragment x Node     = Fragment
  // Fragment x Array    = Fragment
  // Fragment x Element  = Fragment
  // Fragment x Fragment = Fragment

  trait VdomAttr {
    def :=(whatever: Any): TagMod
  }

  trait VdomNodeOrTagMod

  trait TagMod extends VdomNodeOrTagMod {
    def ~(as: VdomNodeOrTagMod): TagMod
  }

  trait VdomNode extends VdomNodeOrTagMod {
    def rawNode: Raw.React.Node
    def ~(next: VdomNode): VdomFragment
  }

  trait VdomArray extends VdomNode {
    override def rawNode = rawArray.asInstanceOf[Raw.React.Node]
    def rawArray: js.Array[Raw.React.Node]
  }

  // TODO cases: raw | component | tag
  trait VdomElement extends VdomNode {
    def rawElement: Raw.React.Element
  }

  // TODO cases: raw | composite
  // TODO Is this a Node or Element?
  trait VdomFragment extends VdomNode

  trait Tag extends VdomElement {
    def apply(as: VdomNodeOrTagMod*): Tag
  }

}
