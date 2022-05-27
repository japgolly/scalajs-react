package japgolly.scalajs.react.test

import japgolly.scalajs.react.{facade => mainFacade}
import org.scalajs.dom

object TestContainer {
  def apply(c: mainFacade.ReactDOM.Container): TestContainer =
    new TestContainer {
      override type Self = TestDom
      override protected def Self(n2: dom.Node) = TestDom(n2)
      override def container = c
      override def toString = s"TestContainer($c)"
    }
}

// =====================================================================================================================

/** Wraps a DOM container and provides utilities for testing its state.
  *
  * As an example `testContainer.innerHTML.assert("<div>Welcome</div>")`
  *
  * @since 2.2.0
  */
trait TestContainer extends TestDom {

  def container: mainFacade.ReactDOM.Container

  final def node =
    fold(identity, identity, identity)

  def fold[A](onElement         : dom.Element          => A,
              onDocument        : dom.Document         => A,
              onDocumentFragment: dom.DocumentFragment => A): A =
    (container: Any) match {
      case x: dom.Element          => onElement         (x)
      case x: dom.Document         => onDocument        (x)
      case x: dom.DocumentFragment => onDocumentFragment(x)
    }

  def isEmpty(): Boolean =
    node.childNodes.length == 0

  @inline final def nonEmpty(): Boolean =
    !isEmpty()
}
