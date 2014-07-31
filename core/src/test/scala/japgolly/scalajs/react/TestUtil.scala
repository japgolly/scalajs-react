package japgolly.scalajs.react

import java.util.concurrent.atomic.AtomicReference
import scala.collection.mutable.ListBuffer
import scala.scalajs.js
import js.Dynamic.{global => $}
import vdom.ReactVDom._
import all._
import utest._

object TestUtil {

  def loadReact(): Unit =
    if (js.isUndefined($.React) && !js.isUndefined($.module))
      js.eval("React = module.exports")

  def assertRender(comp: ReactComponentU[_, _, _], expected: String): Unit = {
    val rendered: String = React.renderComponentToStaticMarkup(comp)
    assert(rendered == expected)
  }

  implicit class ReactComponentUAS(val c: ReactComponentU[_, _, _]) extends AnyVal {
    def shouldRender(expected: String) = assertRender(c, expected)
  }

  def collector1[A](f: ComponentScopeU[_, _, _] => A) =
    ReactComponentB[AtomicReference[Option[A]]]("C₁").stateless
      .render(T => { T.props set Some(f(T)); div ("x") }).create

  def collector1C[A](f: PropsChildren => A) =
    collector1[A](t => f(t.propsChildren))

  def run1[A](C: CompCtorP[AtomicReference[Option[A]], _, _])(f: AtomicReference[Option[A]] => ReactComponentU[AtomicReference[Option[A]], _, _]): A = {
    val a = new AtomicReference[Option[A]](None)
    React renderComponentToStaticMarkup f(a)
    a.get().get
  }

  def run1C[A](c: CompCtorP[AtomicReference[Option[A]], _, _], children: VDom*): A =
    run1(c)(a => c(a, children: _*))

  def collectorN[A](f: (ListBuffer[A], ComponentScopeU[_, _, _]) => Unit) =
    ReactComponentB[ListBuffer[A]]("Cₙ").stateless
      .render(T => { f(T.props, T); div ("x") }).create

  def collectorNC[A](f: (ListBuffer[A], PropsChildren) => Unit) =
    collectorN[A]((l,t) => f(l, t.propsChildren))

  def runN[A](C: CompCtorP[ListBuffer[A], _, _])(f: ListBuffer[A] => ReactComponentU[ListBuffer[A], _, _]): List[A] = {
    val l = new ListBuffer[A]
    React renderComponentToStaticMarkup f(l)
    l.result()
  }

  def runNC[A](c: CompCtorP[ListBuffer[A], _, _], children: VDom*) =
    runN(c)(l => c(l, children: _*))

}
