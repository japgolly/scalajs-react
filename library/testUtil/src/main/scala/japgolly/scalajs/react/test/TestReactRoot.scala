package japgolly.scalajs.react.test

import japgolly.scalajs.react.util.Effect.Async
import japgolly.scalajs.react.{facade => mainFacade, _}
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
  * @since 3.0.0 / React 18
  */
trait TestReactRoot extends TestContainer {
  override type Self <: TestDomWithRoot

  def root: ReactRoot

  @inline def raw =
    root.raw

  def actSync[A](body: => A): A =
    ReactTestUtils.actSync(body)

  def act[F[_]: Async, A](body: F[A]): F[A] =
    ReactTestUtils.act(body)

  @inline def act_[F[_]: Async, A](body: => A): F[A] =
    ReactTestUtils.act_(body)

  def renderSync[A: Renderable](unmounted: A): Unit =
    actSync(root.render(unmounted))

  def render[F[_]: Async, A: Renderable](unmounted: A): F[Unit] =
    act_(root.render(unmounted))

  def unmountSync(): Unit =
    actSync(root.unmount())

  def unmount[F[_]: Async](): F[Unit] =
    act_(root.unmount())
}
