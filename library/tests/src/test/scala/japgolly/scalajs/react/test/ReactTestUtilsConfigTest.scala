package japgolly.scalajs.react.test

import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtilsConfig._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.console
import scala.annotation.nowarn
import scala.util.Try
import utest._

object ReactTestUtilsConfigTest extends TestSuite {

  @nowarn
  private def api(): Unit = {
    // Make sure Scala 2 & 3 have the same API
    AroundReact.id
    AroundReact.fatalReactWarnings
    aroundReact.get
    (aroundReact.set _): (AroundReact => Unit)
    (i: Int) => aroundReact(i): Int
  }

  override def tests = Tests {

    "api" - api()

    "warnings" - {
      "react" - AroundReact.fatalReactWarnings {
        val c = ScalaFnComponent[Int](i => <.p(<.td(s"i = $i")))
        val t = Try(ReactTestUtils2.withRendered(c(123))(_ => ()))
        assertEq(t.isFailure, true)
        t
      }

      "unlreated" - AroundReact.fatalReactWarnings {
        val c = ScalaFnComponent[Int](i => <.p(s"i = $i"))
        val t = Try(ReactTestUtils2.withRendered(c(123)) { _ =>
          console.info(".")
          console.log(".")
          console.warn(".")
          console.error(".")
        })
        assertEq(t.isFailure, false)
      }
    }
  }
}
