package ghpages.secret.tests

import cats.instances.either._
import cats.instances.list._
import cats.instances.option._
import cats.instances.string._
import cats.instances.tuple._
import cats.instances.vector._
import cats.kernel.Eq
import cats.syntax.eq._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Ajax
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.{XMLHttpRequest, console}
import scala.util.{Random, Success, Try}

object AsyncTest {
  import AsyncCallback.delay
  import QuickTest.{Status, TestSuite, TestSuiteBuilder}

  def cmpFn[A: Eq](expect: A): A => Status.Result =
    a => if (a === expect) Status.Pass else Status.Fail(s"Actual: $a. Expect: $expect.")

  def testCmp[A: Eq](x: (AsyncCallback[A], A)) = {
    val (body, expect) = x
    body.map(cmpFn(expect))
  }

  def testCmp2[A: Eq](body: AsyncCallback[A], expect1: A, prep2: () => Unit, expect2: A) = {
    var complete  = null: (Try[A] => Callback)
    val subjectCB = body.attemptTry.flatMap(complete(_).asAsyncCallback).toCallback

    def run(expect: A): AsyncCallback[Status.Result] =
      for {
        (p, f) <- AsyncCallback.promise[A].asAsyncCallback
        _      <- AsyncCallback.delay{complete = f}
        _      <- subjectCB.asAsyncCallback
        a      <- p
      } yield cmpFn(expect).apply(a)

    for {
      s1 <- run(expect1)
      _  <- AsyncCallback.delay(prep2())
      s2 <- run(expect2)
    } yield s1 && s2
  }

  private def getUser(userId: Int) =
    Ajax("GET", s"https://reqres.in/api/users/$userId")
      .setRequestContentTypeJsonUtf8
      .send
      .validateStatusIsSuccessful(Callback.throwException(_))
      .asAsyncCallback

  private val get0 = getUser(0)
  private val get1 = getUser(1)
  private val get2 = getUser(2)

  private def xhrToText(xhr: XMLHttpRequest): String = {
    val idRegex = "(\"id\":\\d+)".r
    idRegex.findFirstIn(xhr.responseText) match {
      case Some(m) =>
        val id = m.replace("\"", "")
        s"[${xhr.status}] $id"
      case None =>
        console.info("Ajax response: ", xhr)
        throw new RuntimeException(s"Unable to parse ajax response: [${xhr.responseText}]")
    }
  }

  private implicit val equalThrowable: Eq[Throwable] =
    _ eq _

  val TestSuite: TestSuite =
    TestSuiteBuilder()

      .add("zip")(testCmp {
        var logs = Vector.empty[String]
        def go(n: String) = delay(logs :+= n) >> Callback(logs :+= s"${n}2").delayMs(10)
        val t = go("a").zip(go("b")) >> delay(logs :+= "|") >> delay(logs)
        t -> "a b a2 b2 |".split(" ").toVector
      })

      .add("race (1)")(testCmp {
        var logs = Vector.empty[String]
        def go(n: String, d: Double) = delay(logs :+= n) >> Callback(logs :+= s"${n}2").delayMs(d)
        val t = go("a", 10).race(go("b", 20)) >> delay(logs :+= "|") >> delay(logs).delayMs(30)
        t -> "a b a2 | b2".split(" ").toVector
      })

      .add("race (2)")(testCmp {
        var logs = Vector.empty[String]
        def go(n: String, d: Double) = delay(logs :+= n) >> Callback(logs :+= s"${n}2").delayMs(d)
        val t = go("a", 20).race(go("b", 10)) >> delay(logs :+= "|") >> delay(logs).delayMs(30)
        t -> "a b b2 | a2".split(" ").toVector
      })

      .add("race (3)") {
        var logs = Vector.empty[String]
        def go(n: String, d: => Double) = delay{logs :+= n; Callback(logs :+= s"${n}2").delayMs(d)}.flatten
        var da      = 10
        var db      = 20
        val t       = go("a", da).race(go("b", db)) >> delay(logs :+= "|") >> delay(logs).delayMs(30)
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
        t1.zip(t2) -> ((123, 123))
      })

      .add("jsPromise: from fixed ko")(testCmp {
        val e = new RuntimeException("AH")
        val p = AsyncCallback.throwException[Int](e).unsafeToJsPromise()
        val t1,t2 = AsyncCallback.fromJsPromise(p).attempt
        t1.zip(t2) -> ((Left(e), Left(e)))
      })

      .add("future")(testCmp {
        import scala.concurrent.ExecutionContext.Implicits.global
        val a = AsyncCallback.pure(123).delayMs(20)
        val t = AsyncCallback.fromFuture(a.unsafeToFuture())
        t -> 123
      })

      .add("memo")(testCmp {
        var count = 0
        val getCount = AsyncCallback.delay(count)
        val incCount = AsyncCallback.delay(count += 1).delayMs(400)
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
