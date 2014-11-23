package japgolly.scalajs.react.test

import utest._
import japgolly.scalajs.react._, vdom.ReactVDom._, all._
import TestUtil._

object SelTest extends TestSuite {

  lazy val C = ReactComponentB[Unit]("C").render(_ =>
    div(
      h1(span("Monuments"), "2014"),
      p(span("The Amanuensis")),
      h3("Quasimodo"),
      h4(cls := "monuments t3", "Atlas"),
      div(cls := "mastodon t3", "High road"),
      div(cls := "mastodon t5", "Chimes at midnight")
    )
  ).buildU

  lazy val c = ReactTestUtils renderIntoDocument C()

  def test1(s: String, e: String): Unit =
    Sel(s).findIn(c).getDOMNode().innerHTML mustEqual e

  def testF(errFrag: String) = (s: String) => {
    val a: Either[String, String] = Sel(s).findInE(c).right.map(_.getDOMNode().innerHTML)
    if (a.isRight) println(Sel(s))
    assert(a.isLeft, a.toString contains errFrag)
  }
  def testNonUnique = testF("Too many")
  def testNotFound = testF("not found")

  val tests = TestSuite {
    'tag - test1("h3", "Quasimodo")

    'cls - test1(".t5", "Chimes at midnight")

    'cls2 {
      test1(".t3.mastodon", "High road")
      test1(".mastodon.t3", "High road")
    }

    'tagAndCls {
      test1("h4.t3", "Atlas")
      testNotFound("h4.t0")
      test1("div.t3", "High road")
      test1("div.mastodon.t3", "High road")
    }

    "*.cls" - test1("*.t5", "Chimes at midnight")

    'decent {
      test1("h1 span", "Monuments")
      test1("p span", "The Amanuensis")
      test1("div p span", "The Amanuensis")
      testNonUnique("div span")
      testNotFound("p h3")
      testNotFound("h3 div")
      testNotFound(".t3 .mastodon")
    }
  }
}
