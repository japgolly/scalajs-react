package japgolly.scalajs.react

import java.util.concurrent.atomic.AtomicReference
import scala.collection.mutable.ListBuffer
import scala.scalajs.js
import js.Dynamic.{global => $}
import vdom.ReactVDom._
import all._
import utest._

object Test extends TestSuite {

  def collector1[A](f: PropsChildren => A) =
    ReactComponentB[AtomicReference[Option[A]]]("C₁").render((a,c) => { a set Some(f(c)); div ("x") }).create

  def run1[A](c: WComponentConstructor[AtomicReference[Option[A]], _, _], children: VDom*): A = {
    val a = new AtomicReference[Option[A]](None)
    React renderComponentToStaticMarkup c.apply2(a, children)
    a.get().get
  }

  def collectorN[A](f: (ListBuffer[A], PropsChildren) => Unit) =
    ReactComponentB[ListBuffer[A]]("Cₙ").render((l,c) => { f(l, c); div ("x") }).create

  def runN[A](c: WComponentConstructor[ListBuffer[A], _, _], children: VDom*) = {
    val l = new ListBuffer[A]
    React renderComponentToStaticMarkup c.apply2(l, children)
    l
  }

  val tests = TestSuite {
    if (js.isUndefined($.React) && !js.isUndefined($.module))
      js.eval("React = module.exports")

    'props {
      val Comp = ReactComponentB[String]("C").render(name => div("Hi ", name)).create
      val m = React renderComponentToStaticMarkup Comp("Mate")
      assert(m == "<div>Hi Mate</div>")
    }

    'children {
      val A = ReactComponentB[Unit]("A").render((_,c) => div(c)).create
      val B = ReactComponentB[Unit]("B").render((_,c) => span(c)).create

      'render {
        'none {
          val m = React renderComponentToStaticMarkup A(())
          assert(m == "<div></div>")
        }

        'one {
          val m = React renderComponentToStaticMarkup A((), h1("yay"))
          assert(m == "<div><h1>yay</h1></div>")
        }

        'two {
          val m = React renderComponentToStaticMarkup A((), h1("yay"), h3("good"))
          assert(m == "<div><h1>yay</h1><h3>good</h3></div>")
        }

        'nested {
          val m = React renderComponentToStaticMarkup A((), B((), h1("nice")))
          assert(m == "<div><span><h1>nice</h1></span></div>")
        }
      }

      'forEach {
        val C1 = collectorN[VDom]((l, c) => c.forEach(l append _))
        val C2 = collectorN[(VDom, Int)]((l, c) => c.forEach((a, b) => l.append((a, b))))

        'withoutIndex {
          val x = runN(C1, h1("yay"), h3("good"))
          assert(x.size == 2)
        }

        'withIndex {
          val x = runN(C2, h1("yay"), h3("good"))
          assert(x.size == 2)
          assert(x.toList.map(_._2) == List(0,1))
        }
      }

      'only {
        val A = collector1[Option[VDom]](_.only)

        'one {
          val r = run1(A, div("Voyager (AU) is an awesome band"))
          assert(r.isDefined)
        }

        'two {
          val r = run1(A, div("The Pensive Disarray"), div("is such a good song"))
          assert(r == None)
        }
      }
    }
  }
}