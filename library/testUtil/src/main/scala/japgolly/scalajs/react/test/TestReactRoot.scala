package japgolly.scalajs.react.test

import japgolly.scalajs.react.{facade => mainFacade, _}
import japgolly.scalajs.react.util.Effect.Async
import org.scalajs.dom

object TestReactRoot {

  def apply(container: mainFacade.ReactDOMClient.RootContainer): TestReactRoot =
    apply(ReactDOMClient.createRoot(container), container)

  def apply(root: ReactRoot, container: mainFacade.ReactDOM.Container): TestReactRoot = {
    @inline def r = root
    @inline def c = container
    new TestReactRoot {
      override type Self = TestDomWithRoot
      override protected def Self(n2: Option[dom.Node]) = TestDomWithRoot(this, n2)
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

  def act[A](body: => A): A = 
    ReactTestUtils2.act(body)

  def actAsync[F[_], A](body: F[A])(implicit F: Async[F]): F[A] =
    ReactTestUtils2.actAsync(body)

  @inline def actAsync_[F[_], A](body: => A)(implicit F: Async[F]): F[A] =
    ReactTestUtils2.actAsync_(body)

  def render[A](unmounted: A)(implicit r: Renderable[A]): Unit =
    act(root.render(unmounted))

  def unmount(): Unit =
    act(root.unmount())
}
