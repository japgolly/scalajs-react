package japgolly.scalajs.react.test

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

  @inline def act[A](body: => A): A = 
    root.act(body)

  @inline def unmount(): Unit =
    root.unmount()
}
