package japgolly.scalajs.react.test.internal

import japgolly.microlibs.testutil.TestUtil
import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react.test.ReactTestUtils
import java.util.regex.Pattern
import org.scalajs.dom
import sourcecode.Line

object HtmlAssertionDsl {

  def apply(name: String, html: => String): HtmlAssertionDsl =
    apply(name, html, html)

  def apply(name: String, rawHtml: => String, html: => String): HtmlAssertionDsl =
    new HtmlAssertionDsl {
      override protected def dslName = name
      override def raw() = rawHtml
      override def apply() = html
    }

  def node(name     : String,
           node     : => Option[dom.Node],
           onElement: dom.Element => String,
           onNode   : dom.Node => String = _.nodeValue): HtmlAssertionDsl = {
    def read(sanitise: String => String): String =
      node.fold(""){
        case e: dom.Element => sanitise(onElement(e))
        case n              => onNode(n)
      }
    apply(
      name    = name,
      rawHtml = read(identity),
      html    = read(ReactTestUtils.removeReactInternals))
  }
}


// =====================================================================================================================

trait HtmlAssertionDsl extends Function0[String] {
  import japgolly.microlibs.testutil.TestUtilInternals._

  override def toString() =
    s"HtmlAssertionDsl:$dslName"

  protected def dslName: String

  /** Return the raw content as a String, which may include internal React attributes. */
  def raw(): String

  /** Return the raw content as a String, with any internal React attributes removed. */
  override def apply(): String

  def assert(expect: String)(implicit l: Line): Unit =
    assert(null, expect)

  def assert(name: => String, expect: String)(implicit l: Line): Unit =
    assertEqO(Option(name), apply(), expect)

  def assertContains(substr: String)(implicit q: Line): Unit =
    TestUtil.assertContains(apply(), substr)

  def assertContainsCI(substr: String)(implicit q: Line): Unit =
    TestUtil.assertContainsCI(apply(), substr)

  def assertNotContains(substr: String)(implicit q: Line): Unit =
    TestUtil.assertNotContains(apply(), substr)

  def assertNotContainsCI(substr: String)(implicit q: Line): Unit =
    TestUtil.assertNotContainsCI(apply(), substr)

  def assertContainsAny(substrs: String*)(implicit q: Line): Unit =
    TestUtil.assertContainsAny(apply(), substrs: _*)

  def assertContainsAll(substrs: String*)(implicit q: Line): Unit =
    TestUtil.assertContainsAll(apply(), substrs: _*)

  def assertNotContainsAny(substrs: String*)(implicit q: Line): Unit =
    TestUtil.assertNotContainsAny(apply(), substrs: _*)

  def assertNotContainsAll(substrs: String*)(implicit q: Line): Unit =
    TestUtil.assertNotContainsAll(apply(), substrs: _*)

  def assertContainsAnyCI(substrs: String*)(implicit q: Line): Unit =
    TestUtil.assertContainsAnyCI(apply(), substrs: _*)

  def assertContainsAllCI(substrs: String*)(implicit q: Line): Unit =
    TestUtil.assertContainsAllCI(apply(), substrs: _*)

  def assertNotContainsAnyCI(substrs: String*)(implicit q: Line): Unit =
    TestUtil.assertNotContainsAnyCI(apply(), substrs: _*)

  def assertNotContainsAllCI(substrs: String*)(implicit q: Line): Unit =
    TestUtil.assertNotContainsAllCI(apply(), substrs: _*)

  def assertMatches(regex: String)(implicit l: Line): Unit =
    assertMatches(null, Pattern.compile(regex))

  def assertMatches(pattern: Pattern)(implicit l: Line): Unit =
    assertMatches(null, pattern)

  def assertMatches(name: => String, regex: String)(implicit l: Line): Unit =
    assertMatches(name, Pattern.compile(regex))

  def assertMatches(name: => String, pattern: Pattern)(implicit l: Line): Unit =
    test2(name)("assertMatches", pattern.matcher(_).matches())("regexp", BOLD_BRIGHT_CYAN, pattern.pattern)

  def assertStartsWith(substr: String)(implicit l: Line): Unit =
    assertStartsWith(null, substr)

  def assertStartsWith(name: => String, substr: String)(implicit l: Line): Unit =
    test2(name)("assertStartsWith", _.startsWith(substr))("substr", BOLD_BRIGHT_CYAN, substr)

  def assertEndsWith(substr: String)(implicit l: Line): Unit =
    assertEndsWith(null, substr)

  def assertEndsWith(name: => String, substr: String)(implicit l: Line): Unit =
    test2(name)("assertEndsWith", _.endsWith(substr))("substr", BOLD_BRIGHT_CYAN, substr)

  def assertStartsWithCI(substr: String)(implicit l: Line): Unit =
    assertStartsWithCI(null, substr)

  def assertStartsWithCI(name: => String, substr: String)(implicit l: Line): Unit =
    test2(name)("assertStartsWithCI", ci(_).startsWith(ci(substr)))("substr", BOLD_BRIGHT_CYAN, substr)

  def assertEndsWithCI(substr: String)(implicit l: Line): Unit =
    assertEndsWithCI(null, substr)

  def assertEndsWithCI(name: => String, substr: String)(implicit l: Line): Unit =
    test2(name)("assertEndsWithCI", ci(_).endsWith(ci(substr)))("substr", BOLD_BRIGHT_CYAN, substr)

  private def ci(s: String) = s.toLowerCase

  private def test2(name: => String)
                   (methodName: String, test: String => Boolean)
                   (title1: String, colour1: String, value1: Any)
                   (implicit l: Line): Unit = {
    val actual = apply()
    if (!test(actual)) {
      val desc = Option(name)
      printFail2(desc)(title1, colour1, value1)("actual", BOLD_BRIGHT_RED, actual)
      failMethod(s"$dslName.$methodName", desc)
    }
  }
}
