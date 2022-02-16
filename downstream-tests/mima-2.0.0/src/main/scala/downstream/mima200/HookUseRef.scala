package downstream.mima200

import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react._
import japgolly.scalajs.react.test.DomTester
import japgolly.scalajs.react.test.ReactTestUtils._
import japgolly.scalajs.react.vdom.html_<^._
import scalajs.js
import utest.compileError

// As part of https://github.com/japgolly/scalajs-react/issues/1037,
// `Hooks.UseRefF` was made to extend a new trait `NonEmptyRef.FullF`.
object HookUseRef {

  private def runTest[M, A](u: Unmounted[M])(f: DomTester => A): A =
    withRenderedIntoBody(u).withParent(root => f(new DomTester(root)))

  private def testRefAccess(ref: Hooks.UseRef[Int]): Unit = {
    assertEq(js.typeOf(ref.raw), "object")
    assertContains(compileError("ref.map(null)").msg, "map is not a member")
  }

  def test(): Unit = {
    val comp = ScalaFnComponent.withHooks[Unit]
      .useRef(100)
      .useState(0)
      .render { (_, ref, s) =>

        testRefAccess(ref)

        <.div(
          ref.value,
          <.button(^.onClick --> ref.mod(_ + 1)),
          <.button(^.onClick --> s.modState(_ + 1)),
        )
      }

    runTest(comp()) { t =>
      t.assertText("100")
      t.clickButton(1); t.assertText("100")
      t.clickButton(2); t.assertText("101")
      t.clickButton(1); t.assertText("101")
      t.clickButton(2); t.assertText("102")
    }
  }
}
