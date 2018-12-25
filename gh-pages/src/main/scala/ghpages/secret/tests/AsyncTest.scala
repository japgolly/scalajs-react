package ghpages.secret.tests

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scalaz.Equal
import scalaz.std.string._
import scalaz.std.vector._
import scalaz.syntax.equal._

object AsyncTest {
  import AsyncCallback.point
  import QuickTest.{Status, TestSuite, TestSuiteBuilder}

  def testCmp[A: Equal](x: (AsyncCallback[A], A)) = {
    val (body, expect) = x
    body.map(a => if (a === expect) Status.Pass else Status.Fail(s"Actual: $a. Expect: $expect."))
  }

  val TestSuite: TestSuite =
    TestSuiteBuilder()

      .add("zip")(testCmp {
        var logs = Vector.empty[String]
        def go(n: String) = point(logs :+= n) >> Callback(logs :+= s"${n}2").delayMs(10)
        val t = go("a").zip(go("b")) >> point(logs :+= "|") >> point(logs)
        t -> "a b a2 b2 |".split(" ").toVector
      })

      .add("race (1)")(testCmp {
        var logs = Vector.empty[String]
        def go(n: String, d: Double) = point(logs :+= n) >> Callback(logs :+= s"${n}2").delayMs(d)
        val t = go("a", 10).race(go("b", 20)) >> point(logs :+= "|") >> point(logs).delayMs(30)
        t -> "a b a2 | b2".split(" ").toVector
      })

      .add("race (2)")(testCmp {
        var logs = Vector.empty[String]
        def go(n: String, d: Double) = point(logs :+= n) >> Callback(logs :+= s"${n}2").delayMs(d)
        val t = go("a", 20).race(go("b", 10)) >> point(logs :+= "|") >> point(logs).delayMs(30)
        t -> "a b b2 | a2".split(" ").toVector
      })

      .result()

  def Component(): VdomElement =
    QuickTest.Component(
      QuickTest.Props(
        TestSuite))
}
