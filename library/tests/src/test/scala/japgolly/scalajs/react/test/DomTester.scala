package japgolly.scalajs.react.test

import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react._
import japgolly.scalajs.react.test.LegacyReactTestUtils._
import japgolly.scalajs.react.test._
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

  def clickButton(n: Int = 1): Unit = {
    val bs = root.querySelectorAll("button")
    assert(n > 0 && n <= bs.length, s"${bs.length} buttons found (n=$n)")
    val b = bs(n - 1).asInstanceOf[Button]
    act(Simulate.click(b))
  }

  def assertInputText(expect: String)(implicit l: Line): Unit =
    assertEq(getInputText().value, expect)

  def setInputText(t: String): Unit = {
    val i = getInputText()
    act(SimEvent.Change(t).simulate(i))
  }

  private def getInputText(): Input = {
    val is = root.querySelectorAll("input[type=text]")
    val len = is.length
    assert(len == 1)
    is(0).domCast[Input]
  }
}
