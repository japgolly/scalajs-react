package japgolly.scalajs.react.test

import japgolly.scalajs.react._
import org.scalajs.dom

object TestReactRoot {

  def apply(container: facade.ReactDOM.RootContainer): TestReactRoot =
    apply(ReactDOM.createRoot(container), container)

  def apply(root: ReactRoot, container: facade.ReactDOM.Container): TestReactRoot = {
    @inline def r = root
    @inline def c = container
    new TestReactRoot {
      override type Self = TestDomWithRoot
      override protected def Self(n2: dom.Node) = TestDomWithRoot(this, n2)
      override def root = r

      override def container = c
      override def toString = s"TestReactRoot($root, $container)"
    }
  }
}

// =====================================================================================================================

/** Wraps a React Root (introduced in React 18) and provides utilities for testing its state.
  *
  * As an example `testRoot.innerHTML.assert("<div>Welcome</div>")`
  *
  * @since 2.2.0 / React 18
  */
trait TestReactRoot extends TestContainer {
  override type Self <: TestDomWithRoot

  def root: ReactRoot

  @inline def raw =
    root.raw

  def render[A](unmounted: A)(implicit r: Renderable[A]): Unit =
    ReactTestUtils.act(root.render(unmounted))

  def unmount(): Unit =
    ReactTestUtils.act(root.unmount())
}
