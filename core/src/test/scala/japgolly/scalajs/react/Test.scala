package japgolly.scalajs.react

import vdom.ReactVDom._
import all._
import utest._
import TestUtil._

object Test extends TestSuite {

  lazy val CA = ReactComponentB[Unit]("CA").render((_,c) => div(c)).createU
  lazy val CB = ReactComponentB[Unit]("CB").render((_,c) => span(c)).createU
  lazy val H1 = ReactComponentB[String]("H").render(p => h1(p)).create

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

    'vdomGen {
      'listOfScalatags {
        val X = ReactComponentB[List[String]]("X").render(P => {
          def createItem(itemText: String) = li(itemText)
          ul(P map createItem)
        }).create
        X(List("123","abc")) shouldRender "<ul><li>123</li><li>abc</li></ul>"
      }
      'listOfReactComponents {
        val X = ReactComponentB[List[String]]("X").render(P => ul(P.map(i => H1(i)))).create
        X(List("123","abc")) shouldRender "<ul><h1>123</h1><h1>abc</h1></ul>"
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

      'argsToComponents {
        'listOfScalatags {
          CA(List(h1("nice"), h2("good"))) shouldRender "<div><h1>nice</h1><h2>good</h2></div>" }

        'listOfReactComponents {
          CA(List(CB(h1("nice")), CB(h2("good")))) shouldRender
            "<div><span><h1>nice</h1></span><span><h2>good</h2></span></div>" }
      }

      'rendersGivenChildren {
        'none { CA() shouldRender "<div></div>" }
        'one { CA(h1("yay")) shouldRender "<div><h1>yay</h1></div>" }
        'two { CA(h1("yay"), h3("good")) shouldRender "<div><h1>yay</h1><h3>good</h3></div>" }
        'nested { CA(CB(h1("nice"))) shouldRender "<div><span><h1>nice</h1></span></div>" }
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