package ghpages.secret.tests

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Ajax
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.XMLHttpRequest
import scalaz.Equal
import scalaz.std.option._
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

  private def getUser(userId: Int) =
    Ajax("GET", s"https://reqres.in/api/users/$userId")
      .setRequestContentTypeJsonUtf8
      .send
      .validateSuccessful(Callback.error)
      .asAsyncCallback

  private val get0 = getUser(0)
  private val get1 = getUser(1)
  private val get2 = getUser(2)

  private def xhrToText(xhr: XMLHttpRequest): String = {
    val idRegex = "(\"id\":\\d+)".r
    val id = idRegex.findFirstIn(xhr.responseText).get.replace("\"", "")
    s"[${xhr.status}] $id"
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

      .add("ajax (ok *> ok)")(testCmp {
        val t = (get1 *> get2).map(xhrToText)
        t -> s"[200] id:2"
      })

      .add("ajax (ok <* ok)")(testCmp {
        val t = (get1 <* get2).map(xhrToText)
        t -> s"[200] id:1"
      })

      .add("ajax (ko *> ok)")(testCmp {
        val t = (get0 *> get2).map(xhrToText).attempt.map(_.toOption)
        t -> None
      })

      .add("ajax (ok *> ko)")(testCmp {
        val t = (get2 *> get0).map(xhrToText).attempt.map(_.toOption)
        t -> None
      })

      .result()

  def Component(): VdomElement =
    QuickTest.Component(
      QuickTest.Props(
        TestSuite, 4))
}
