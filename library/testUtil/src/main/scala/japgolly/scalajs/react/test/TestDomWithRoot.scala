package japgolly.scalajs.react.test

import japgolly.scalajs.react.util.Effect
import japgolly.scalajs.react.util.Effect.Async
import org.scalajs.dom

object TestDomWithRoot {
  def apply(r: TestReactRoot, n: Option[dom.Node]): TestDomWithRoot =
    new TestDomWithRoot {
      override type Self = TestDomWithRoot
      override protected def Self(n2: Option[dom.Node]) = TestDomWithRoot(root, n2)
      override val root = r
      override def node = n
      override def toString = s"TestDomWithRoot($node)"
    }
}

// =====================================================================================================================

trait TestDomWithRoot extends TestDom {
  override type Self <: TestDomWithRoot
  val root: TestReactRoot

  @inline def actSync[A](body: => A): A = 
    root.actSync(body)

  @inline def act[F[_]: Async, A](body: F[A]): F[A] =
    root.act(body)

  @inline def act_[F[_]: Async, A](body: => A): F[A] =
    root.act_(body)

  @inline def unmountSync(): Unit =
    root.unmountSync()

  @inline def unmount[F[_]: Async](): F[Unit] =
    root.unmount()

  def withNode[F[_]: Effect](f: dom.Node => F[Unit]): F[Unit] = 
    node.map(f).getOrElse(Effect[F].throwException(new RuntimeException("Node not rendered")))

  def withNode_[F[_]: Effect](f: dom.Node => Unit): F[Unit] = 
    withNode(n => Effect[F].delay(f(n)))

  def actOnNode[F[_]: Async](f: dom.Node => F[Unit]): F[Unit] = 
    withNode(n => act(f(n)))

  def actOnNode_[F[_]: Async](f: dom.Node => Unit): F[Unit] = 
    withNode(n => act_(f(n)))
}
