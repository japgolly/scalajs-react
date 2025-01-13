package japgolly.scalajs.react.test

import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtilsConfig._
import japgolly.scalajs.react.util.Effect
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.console
import scala.annotation.nowarn
import utest._

object ReactTestUtilsConfigTest extends AsyncTestSuite {

  @nowarn
  private def api[F[_]: Effect](): Unit = {
    // Make sure Scala 2 & 3 have the same API
    AroundReact.id
    AroundReact.fatalReactWarnings
    aroundReact.get
    (aroundReact.set _): (AroundReact => Unit)
    (i: Int) => aroundReact[F, Int](Effect[F].pure(i)): F[Int]
  }

  override def tests = Tests {

    "api" - api[AsyncCallback]()

    "warnings" - {
      "react" - AroundReact.fatalReactWarnings {
        val c = ScalaFnComponent[Int](i => <.p(<.td(s"i = $i")))
        ReactTestUtils2.withRendered_(c(123))(_ => ()).attemptTry.map( t =>
          assertEq(t.isFailure, true)
        )
      }

      "unlreated" - AroundReact.fatalReactWarnings {
        val c = ScalaFnComponent[Int](i => <.p(s"i = $i"))
        ReactTestUtils2.withRendered_(c(123)) { _ =>
          console.info(".")
          console.log(".")
          console.warn(".")
          console.error(".")
        }.attemptTry.map( t =>
          assertEq(t.isFailure, false)
        )
      }
    }
  }
}
