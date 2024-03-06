package japgolly.scalajs.react.test

import japgolly.scalajs.react.test.internal.HtmlAssertionDsl
import japgolly.scalajs.react.util.JsUtil
import org.scalajs.dom
import scala.reflect.ClassTag
import scala.util.Try

object TestDom {
  def apply(n: dom.Node): TestDom =
    new TestDom {
      override type Self = TestDom

      override protected def Self(n2: dom.Node) = TestDom(n2)
      override def node = n
      override def toString = s"TestDom($node)"
    }
}

// =====================================================================================================================

/** Wraps a DOM and provides utilities for testing its state.
  *
  * As an example `testDom.outerHTML.assert("<div>Welcome</div>")`
  *
  * @since 2.2.0
  */
trait TestDom {

  def node: dom.Node

  assert(node.isInstanceOf[dom.Node], "Invalid test DOM. Expected a DOM node but got: " + node)

  // -------------------------------------------------------------------------------------------------------------------
  // Returning `TestDom`

  type Self <: TestDom

  protected def Self(n: dom.Node): Self

  def select(f: dom.Node => dom.Node): Self =
    Self(f(node))

  def select(selectors: String): Self = {
    val all = querySelectorAll(selectors)
    all.length match {
      case 1 => Self(all.head)
      case 0 => throw new RuntimeException(s"No child of $node found matching '$selectors'")
      case n => throw new RuntimeException(s"Found $n children of $node found matching '$selectors', expected 1. Use .selectFirst() instead to select the first matching result.")
    }
  }

  def selectFirst(selectors: String): Self =
    querySelectorOption(selectors) match {
      case Some(e) => Self(e)
      case None    => throw new RuntimeException(s"No child of $node found matching '$selectors'")
    }

  def selectFirstChild(): Self =
    Self(firstChild())

  // -------------------------------------------------------------------------------------------------------------------
  // Returning DOM

  /** Cast the DOM as `A` or throw an exception. */
  def as[A](implicit ct: ClassTag[A]): A =
    (node: Any) match {
      case a: A =>
        a
      case _ =>
        val cls = ct.runtimeClass
        val name = Try(cls.getSimpleName()).getOrElse(cls.getName())
        throw new RuntimeException(s"Expected DOM to be a $name, got: $node")
    }

  def asButton(): dom.HTMLButtonElement =
    as[dom.HTMLButtonElement]

  def asDocument(): dom.Document =
    as[dom.Document]

  def asDocumentFragment(): dom.DocumentFragment =
    as[dom.DocumentFragment]

  def asElement(): dom.Element =
    as[dom.Element]

  def asHtml(): dom.HTMLElement =
    as[dom.HTMLElement]

  def asInput(): dom.HTMLInputElement =
    as[dom.HTMLInputElement]

  def asSelect(): dom.HTMLSelectElement =
    as[dom.HTMLSelectElement]

  def asTextArea(): dom.HTMLTextAreaElement =
    as[dom.HTMLTextAreaElement]

  def children(): Vector[dom.Node] =
    node.childNodes.toVector

  def firstChild(): dom.Node =
    node.childNodes(0)

  def querySelector(selectors: String): dom.Element = {
    JsUtil.querySelectorFn(node)
      .map(_(selectors))
      .getOrElse(throw new RuntimeException(s".querySelector() isn't available on $node"))
  }

  def querySelectorOption(selectors: String): Option[dom.Element] =
    JsUtil.querySelectorFn(node).toOption.flatMap(f => Option(f(selectors)))

  def querySelectorAll(selectors: String): Vector[dom.Element] =
    JsUtil.querySelectorAllFn(node).map(_(selectors).toVector).getOrElse(Vector.empty)

  // /**
  //  * Traverse all components in tree and accumulate all components where test(component) is true.
  //  * This is not that useful on its own, but it's used as a primitive for other test utils.
  //  */
  // def findAllInRenderedTree(tree: Mounted, test: MountedOutput => Boolean): Vector[MountedOutput] =
  //   raw.findAllInRenderedTree(tree.raw, (m: RawM) => test(wrapMO(m))).iterator.map(wrapMO(_)).toVector

  // /**
  //  * Finds all instance of components in the rendered tree that are DOM components with the class name
  //  * matching className.
  //  */
  // def scryRenderedDOMComponentsWithClass(tree: Mounted, className: String): Vector[MountedOutput] =
  //   raw.scryRenderedDOMComponentsWithClass(tree.raw, className).iterator.map(wrapMO(_)).toVector

  // /**
  //  * Like [[scryRenderedDOMComponentsWithClass()]] but expects there to be one result, and returns that one result, or
  //  * throws exception if there is any other number of matches besides one.
  //  */
  // def findRenderedDOMComponentWithClass(tree: Mounted, className: String): MountedOutput =
  //   wrapMO(raw.findRenderedDOMComponentWithClass(tree.raw, className))

  // /**
  //  * Finds all instance of components in the rendered tree that are DOM components with the tag name
  //  * matching tagName.
  //  */
  // def scryRenderedDOMComponentsWithTag(tree: Mounted, tagName: String): Vector[MountedOutput] =
  //   raw.scryRenderedDOMComponentsWithTag(tree.raw, tagName).iterator.map(wrapMO(_)).toVector

  // /**
  //  * Like [[scryRenderedDOMComponentsWithTag()]] but expects there to be one result, and returns that one result, or
  //  * throws exception if there is any other number of matches besides one.
  //  */
  // def findRenderedDOMComponentWithTag(tree: Mounted, tagName: String): MountedOutput =
  //   wrapMO(raw.findRenderedDOMComponentWithTag(tree.raw, tagName))

  // /** Finds all instances of components with type equal to componentClass. */
  // def scryRenderedComponentsWithType(tree: Mounted, c: CompType): Vector[MountedOutput] =
  //   raw.scryRenderedComponentsWithType(tree.raw, c.raw).iterator.map(wrapMO(_)).toVector

  // /**
  //  * Same as [[scryRenderedComponentsWithType()]] but expects there to be one result and returns that one result, or throws
  //  * exception if there is any other number of matches besides one.
  //  */
  // def findRenderedComponentWithType(tree: Mounted, c: CompType): MountedOutput =
  //   wrapMO(raw.findRenderedComponentWithType(tree.raw, c.raw))

  // -------------------------------------------------------------------------------------------------------------------
  // Testing

  def innerHTML: HtmlAssertionDsl =
    HtmlAssertionDsl.node("innerHTML", node, _.innerHTML)

  def outerHTML: HtmlAssertionDsl =
    HtmlAssertionDsl.node("outerHTML", node, _.outerHTML)
}
