package japgolly.scalajs.react

import java.util.concurrent.atomic.AtomicReference
import scala.collection.mutable.ListBuffer
import vdom.ReactVDom._
import all._
import utest._

object TestUtil {

  def assertRender(comp: ReactComponentU_, expected: String): Unit = {
    val rendered: String = React.renderToStaticMarkup(comp)
    assert(rendered == expected)
  }

  implicit class ReactComponentUAS(val c: ReactComponentU_) extends AnyVal {
    def shouldRender(expected: String) = assertRender(c, expected)
  }

  def collector1[A](f: ComponentScopeU[_, _, _] => A) =
    ReactComponentB[AtomicReference[Option[A]]]("C₁").stateless
      .render(T => { T.props set Some(f(T)); div ("x") }).build

  def collector1C[A](f: PropsChildren => A) =
    collector1[A](t => f(t.propsChildren))

  def run1[A](C: ReactComponentC.ReqProps[AtomicReference[Option[A]], _, _, _])
             (f: AtomicReference[Option[A]] => ReactComponentU[AtomicReference[Option[A]], _, _, _]): A = {
    val a = new AtomicReference[Option[A]](None)
    React renderToStaticMarkup f(a)
    a.get().get
  }

  def run1C[A](c: ReactComponentC.ReqProps[AtomicReference[Option[A]], _, _, _], children: ReactNode*): A =
    run1(c)(a => c(a, children: _*))

  def collectorN[A](f: (ListBuffer[A], ComponentScopeU[_, _, _]) => Unit) =
    ReactComponentB[ListBuffer[A]]("Cₙ").stateless
      .render(T => { f(T.props, T); div ("x") }).build

  def collectorNC[A](f: (ListBuffer[A], PropsChildren) => Unit) =
    collectorN[A]((l,t) => f(l, t.propsChildren))

  def runN[A](C: ReactComponentC.ReqProps[ListBuffer[A], _, _, _])
             (f: ListBuffer[A] => ReactComponentU[ListBuffer[A], _, _, _]): List[A] = {
    val l = new ListBuffer[A]
    React renderToStaticMarkup f(l)
    l.result()
  }

  def runNC[A](c: ReactComponentC.ReqProps[ListBuffer[A], _, _, _], children: ReactNode*) =
    runN(c)(l => c(l, children: _*))

  implicit class AnyTestExt[A](val v: A) extends AnyVal {

    // nice output in assertion macro
    def mustEqual(e: A): Unit = {
      val a = v
      assert(a == e)
    }
  }
}
