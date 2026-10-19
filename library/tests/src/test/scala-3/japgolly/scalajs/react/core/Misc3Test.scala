package japgolly.scalajs.react.core

import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import utest._

object Misc3Test extends TestSuite {
  japgolly.scalajs.react.test.InitTestEnv()

  sealed trait S
  case class S1(x: Int) extends S
  case class S2(y: String) extends S

  val rs = Reusability.derive[S](logNonReuse = true, logCode = false, compareRefs = true, excludeFields = "")

  case class CC(a: Int, b: String)
  val rcc = Reusability.derive[CC](logNonReuse = true, logCode = false, compareRefs = true, excludeFields = "")

  object StaticComp {
    val comp = ScalaComponent.builder.static(<.div).build
  }

  override def tests = Tests {

    "cssUnits" - {
      "static" - {
        assertTastyEq(0.px, "0")
        assertTastyEq(0.0.px, "0")
        assertTastyEq(1.px, "1px")
      }
      "dynamic" - {
        def x(n: Int) = n.px
        assertEq(x(0), "0")
        assertEq(x(1), "1px")
      }
    }

    "reusability" - {
      "sumTypeLogNonReuse" - {
        assert(rs.test(S1(1), S1(1)))
        assert(!rs.test(S1(1), S1(2)))
        assert(!rs.test(S1(1), S2("a")))
      }

      "caseClassLogNonReuse" - {
        assert(rcc.test(CC(1, "a"), CC(1, "a")))
        assert(!rcc.test(CC(1, "a"), CC(2, "a")))
        assert(!rcc.test(CC(1, "a"), CC(1, "b")))
      }
    }

    "componentBuilder" - {
      "staticAutoName" - {
        assertEq(StaticComp.comp.displayName, "japgolly.scalajs.react.core.Misc3Test.Static")
      }
      "componentBuilderStatic" - {
        val b = japgolly.scalajs.react.component.builder.ComponentBuilder.static("CustomStatic", <.div).build
        assertEq(b.displayName, "CustomStatic")
      }
    }
  }
}
