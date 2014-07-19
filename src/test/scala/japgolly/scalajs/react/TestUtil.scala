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

  def collector1[A](f: PropsChildren => A) =
    ReactComponentB[AtomicReference[Option[A]]]("C₁").render((a,c) => { a set Some(f(c)); div ("x") }).create

  def run1[A](c: CompCtorP[AtomicReference[Option[A]], _, _], children: VDom*): A = {
    val a = new AtomicReference[Option[A]](None)
    React renderComponentToStaticMarkup c.apply2(a, children)
    a.get().get
  }

  def collectorN[A](f: (ListBuffer[A], PropsChildren) => Unit) =
    ReactComponentB[ListBuffer[A]]("Cₙ").render((l,c) => { f(l, c); div ("x") }).create

  def runN[A](c: CompCtorP[ListBuffer[A], _, _], children: VDom*) = {
    val l = new ListBuffer[A]
    React renderComponentToStaticMarkup c.apply2(l, children)
    l
  }

}
