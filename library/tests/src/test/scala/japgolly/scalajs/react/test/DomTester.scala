package japgolly.scalajs.react.test

import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils2._
import japgolly.scalajs.react.test._
import japgolly.scalajs.react.util.Effect.Async
import org.scalajs.dom.html.{Button, Element, Input}
import sourcecode.Line

object DomTester {
  val tagRegex = "<[a-zA-Z].*?>|</[a-zA-Z]+?>".r

  def getText(e: Element): String =
    tagRegex.replaceAllIn(e.innerHTML, "").trim

  def assertText(e: Element, expect: String)(implicit l: Line): Unit =
    assertEq(getText(e), expect)
}

// =====================================================================================================================

class DomTester(root: Element) {

  def assertText(expect: String)(implicit l: Line): Unit =
    DomTester.assertText(root, expect)

  def clickButton[F[_]: Async](n: Int = 1): F[Unit] = {
    val bs = root.querySelectorAll("button")
    assert(n > 0 && n <= bs.length, s"${bs.length} buttons found (n=$n)")
    val b = bs(n - 1).asInstanceOf[Button]
    act_(Simulate.click(b))
  }

  def assertInputText(expect: String)(implicit l: Line): Unit =
    assertEq(getInputText().value, expect)

  def setInputText[F[_]: Async](t: String): F[Unit] = {
    val i = getInputText()
    act_(SimEvent.Change(t).simulate(i))
  }

  def getText: String =
    DomTester.getText(root)

  private def getInputText(): Input = {
    val is = root.querySelectorAll("input[type=text]")
    val len = is.length
    assert(len == 1)
    is(0).domCast[Input]
  }
}
