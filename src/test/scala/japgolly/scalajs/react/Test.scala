package japgolly.scalajs.react

import vdom.ReactVDom._
import all._
import utest._
import TestUtil._

object Test extends TestSuite {

  val tests = TestSuite {
    loadReact()

    'props {
      'unit {
        val r = ReactComponentB[Unit]("U").render((_,c) => h1(c)).createU
        r(div("great")) shouldRender "<h1><div>great</div></h1>"
      }

      'required {
        val r = ReactComponentB[String]("C").render(name => div("Hi ", name)).create
        r("Mate") shouldRender "<div>Hi Mate</div>"
      }

      val O = ReactComponentB[String]("C").render(name => div("Hey ", name)).propsDefault("man").create
      'optionalNone {
        O() shouldRender "<div>Hey man</div>"
      }
      'optionalSome {
        O(Some("dude")) shouldRender "<div>Hey dude</div>"
      }

      'always {
        val r = ReactComponentB[String]("C").render(name => div("Hi ", name)).propsAlways("there").create
        r() shouldRender "<div>Hi there</div>"
      }
    }

    'classSet {
      'allConditional {
        val r = ReactComponentB[(Boolean,Boolean)]("C").render(p => div(classSet("p1" -> p._1, "p2" -> p._2))("x")).create
        r((false, false)) shouldRender """<div>x</div>"""
        r((true,  false)) shouldRender """<div class="p1">x</div>"""
        r((false, true))  shouldRender """<div class="p2">x</div>"""
        r((true,  true))  shouldRender """<div class="p1 p2">x</div>"""
      }
      'hasMandatory {
        val r = ReactComponentB[Boolean]("C").render(p => div(classSet("mmm", "ccc" -> p))("x")).create
        r(false) shouldRender """<div class="mmm">x</div>"""
        r(true)  shouldRender """<div class="mmm ccc">x</div>"""
      }
    }

    'children {
      val A = ReactComponentB[Unit]("A").render((_,c) => div(c)).create
      val B = ReactComponentB[Unit]("B").render((_,c) => span(c)).create

      'render {
        'none { A(()) shouldRender "<div></div>" }
        'one { A((), h1("yay")) shouldRender "<div><h1>yay</h1></div>" }
        'two { A((), h1("yay"), h3("good")) shouldRender "<div><h1>yay</h1><h3>good</h3></div>" }
        'nested { A((), B((), h1("nice"))) shouldRender "<div><span><h1>nice</h1></span></div>" }
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