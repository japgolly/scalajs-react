package ghpages.secret.tests

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Ajax
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.XMLHttpRequest
import scala.util.{Random, Success, Try}
import scalaz.Equal
import scalaz.std.anyVal._
import scalaz.std.either._
import scalaz.std.list._
import scalaz.std.option._
import scalaz.std.string._
import scalaz.std.tuple._
import scalaz.std.vector._
import scalaz.syntax.equal._

object AsyncTest {
  import AsyncCallback.point
  import QuickTest.{Status, TestSuite, TestSuiteBuilder}

  def cmpFn[A: Equal](expect: A): A => Status.Result =
    a => if (a === expect) Status.Pass else Status.Fail(s"Actual: $a. Expect: $expect.")

  def testCmp[A: Equal](x: (AsyncCallback[A], A)) = {
    val (body, expect) = x
    body.map(cmpFn(expect))
  }

  def testCmp2[A: Equal](body: AsyncCallback[A], expect1: A, prep2: () => Unit, expect2: A) = {
    var complete  = null: Try[A] => Callback
    val subjectCB = body.attemptTry.flatMap(complete(_).asAsyncCallback).toCallback

    def run(expect: A): AsyncCallback[Status.Result] =
      for {
        (p, f) <- AsyncCallback.promise[A].asAsyncCallback
        _      <- AsyncCallback.point{complete = f}
        _      <- subjectCB.asAsyncCallback
        a      <- p
      } yield cmpFn(expect).apply(a)

    for {
      s1 <- run(expect1)
      _  <- AsyncCallback.point(prep2())
      s2 <- run(expect2)
    } yield s1 && s2
  }

  private def getUser(userId: Int) =
    Ajax("GET", s"https://reqres.in/api/users/$userId")
      .setRequestContentTypeJsonUtf8
      .send
      .validateStatusIsSuccessful(Callback.error)
      .asAsyncCallback

  private val get0 = getUser(0)
  private val get1 = getUser(1)
  private val get2 = getUser(2)

  private def xhrToText(xhr: XMLHttpRequest): String = {
    val idRegex = "(\"id\":\\d+)".r
    val id = idRegex.findFirstIn(xhr.responseText).get.replace("\"", "")
    s"[${xhr.status}] $id"
  }

  private implicit val equalThrowable: Equal[Throwable] = Equal.equalRef

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

      .add("race (3)") {
        var logs = Vector.empty[String]
        def go(n: String, d: => Double) = point{logs :+= n; Callback(logs :+= s"${n}2").delayMs(d)}.flatten
        var da      = 10
        var db      = 20
        val t       = go("a", da).race(go("b", db)) >> point(logs :+= "|") >> point(logs).delayMs(30)
        val expect1 = "a b a2 | b2".split(" ").toVector
        val expect2 = "a b b2 | a2".split(" ").toVector
        val prep2   = () => {da = 20; db = 10; logs = Vector.empty[String]}

        testCmp2(t, expect1, prep2, expect2)
      }

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

      .add("traverse")(testCmp {
        val r = new Random()
        val is = (1 to 10).toList
        val t = AsyncCallback.traverse(is)(i => AsyncCallback.pure(i * 100).delayMs(r.nextInt(64)))
        t -> is.map(_ * 100)
      })

      .add("jsPromise: to & from")(testCmp {
        val a = AsyncCallback.pure(123).delayMs(20)
        val t = AsyncCallback.fromJsPromise(a.unsafeToJsPromise())
        t -> 123
      })

      .add("jsPromise: from fixed ok")(testCmp {
        val p = AsyncCallback.pure(123).unsafeToJsPromise()
        val t1,t2 = AsyncCallback.fromJsPromise(p)
        t1.zip(t2) -> (123, 123)
      })

      .add("jsPromise: from fixed ko")(testCmp {
        val e = new RuntimeException("AH")
        val p = AsyncCallback.throwException[Int](e).unsafeToJsPromise()
        val t1,t2 = AsyncCallback.fromJsPromise(p).attempt
        t1.zip(t2) -> (Left(e), Left(e))
      })

      .add("future")(testCmp {
        import scala.concurrent.ExecutionContext.Implicits.global
        val a = AsyncCallback.pure(123).delayMs(20)
        val t = AsyncCallback.fromFuture(a.unsafeToFuture())
        t -> 123
      })

      .add("memo")(testCmp {
        var count = 0
        val getCount = AsyncCallback.point(count)
        val incCount = AsyncCallback.point(count += 1).delayMs(400)
        val m = incCount.memo()
        // start 1
        // start 2
        // complete 1
        // complete 2
        // start 3
        // complete 3
        val t = (m *> m) >> m.memo() >> getCount
        t -> 1
      })

    .add("init")(testCmp {
      val x = AsyncCallback.init[Boolean, Int] { f =>
        f(Success(123)).delayMs(500).toCallback.ret(true)
      }
      val y = for {
        (b, ac) <- x.asAsyncCallback
        i       <- ac
      } yield (b, i)
      y -> ((true, 123))
    })

      .result()

  def Component(): VdomElement =
    QuickTest.Component(
      QuickTest.Props(
        TestSuite, 4))
}
