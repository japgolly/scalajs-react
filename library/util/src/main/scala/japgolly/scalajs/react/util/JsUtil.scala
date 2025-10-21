package japgolly.scalajs.react.util

import java.time.Duration
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.|
import scala.util.{Failure, Success, Try}

object JsUtil {

  // TODO: Add to microlibs and maybe ReactTestUtils
  @inline def setStackTraceLimit(n: Int): Unit =
    js.constructorOf[js.Error].stackTraceLimit = n

  object JsSymbol {
    def unapply(a: Any): Option[js.Symbol] =
      js.typeOf(a.asInstanceOf[js.Any]) match {
        case "symbol" => Some(a.asInstanceOf[js.Symbol])
        case _        => None
      }
  }

  def safeToString(a: Any): String =
    try
      a match {
        case JsSymbol(s) => symbolToString(s)
        case _           => a.toString
      }
    catch {
      case _: Throwable => "?"
    }

  def symbolToString(s: js.Symbol): String =
    try
      s.asInstanceOf[js.Dynamic].applyDynamic("toString")().asInstanceOf[String]
    catch {
      case _: Throwable =>
        js.Symbol.keyFor(s).toOption match {
          case Some(k) => s"Symbol($k)"
          case None    => "Symbol(?)"
        }
    }

  def inspectValue(a: Any): String =
    a match {
      case s: String    => js.JSON.stringify(s)
      case o: js.Object => inspectObject(o)
      case () | null    => "" + a
      case JsSymbol(s)  => symbolToString(s)
      case _            => s"${safeToString(a)}: ${js.typeOf(a.asInstanceOf[js.Any])}"
    }

  def objectIterator(o: js.Object): Iterator[(String, js.Any)] = {
    val d = o.asInstanceOf[js.Dynamic]
    js.Object.properties(o).iterator.map { n =>
      val v = (try d.selectDynamic(n) catch { case t: Throwable => safeToString(t) }).asInstanceOf[js.Any]
      n -> v
    }
  }

  def inspectObject(o: js.Object): String = {
    val s = objectIterator(o).toVector.sortBy(_._1)
    if (s.isEmpty)
      "Value has no object properties: " + o
    else {
      val ss = s.map { case (k, v) =>
        (k, js.typeOf(v), safeToString(v).split('\n')(0))
      }
      val sz = s.size
      val nlen = sz.toString.length
      val klen = ss.map(_._1.length).max
      val tlen = ss.map(_._2.length).max
      val fmt = s"  [%${nlen}d/$sz] %-${klen}s : %-${tlen}s = %s"
      var i = 0
      ss.map { case (k, t, v) =>
        i = i + 1
        fmt.format(i, k, t, v)
      }.mkString(s"$o\n", "\n", "")
    }
  }

  def jsArray[A](as: A*): js.Array[A] = {
    val array = new js.Array[A]
    array.push(as: _*)
    array
  }

  def jsArrayFromTraversable[A](as: IterableOnce[A]): js.Array[A] = {
    val array = new js.Array[A]
    as.iterator.foreach(array push _)
    array
  }

  @inline def notNull[A](a: A | Null): A =
    a.asInstanceOf[A]

  def jsNullToOption[A](an: A | Null): Option[A] =
    Option(an.asInstanceOf[A])

  def optionToJsNull[A](oa: Option[A]): A | Null =
    oa match {
      case Some(a) => a
      case None    => null
    }

  def durationFromDOMHighResTimeStamp(ms: Double): Duration =
    Duration.ofNanos((ms * 1000000).toLong)

  def newPromise[A](): (js.Promise[A], Try[A] => js.Function0[Unit]) = {
    var complete: Try[A] => js.Function0[Unit] = null
    val p = new js.Promise[A]((respond: js.Function1[A | js.Thenable[A], _], reject: js.Function1[Any, _]) => {
      def fail(t: Throwable) =
        reject(t match {
          case js.JavaScriptException(e) => e
          case e                         => e
        })
      complete = {
        case Success(a) => () => respond(a)
        case Failure(e) => () => fail(e)
      }
    })
    (p, complete)
  }

  def runPromiseAsync[A](pa: => js.Thenable[A])(complete: Try[A] => js.Function0[Unit]): Unit = {
    def next(ta: Try[A]): js.Thenable[Unit] = {
      val (p, pc) = newPromise[Unit]()
      pc(Try(complete(ta)()))()
      p
    }
    type R = Unit | js.Thenable[Unit]
    val ok: A   => R = a => next(Success(a))
    val ko: Any => R = e => next(Failure(e match {
      case t: Throwable => t
      case _            => js.JavaScriptException(e)
    }))
    pa.`then`[Unit](ok, ko: js.Function1[Any, R])
  }

  def asyncToPromise[A](async: (Try[A] => js.Function0[Unit]) => js.Function0[Unit]): () => js.Promise[A] =
    () => {
      val (p, pc) = newPromise[A]()
      async(pc)()
      p
    }

  def global(): js.Dynamic =
    _global

  private lazy val _global =
    Try(js.Dynamic.global.globalThis)
      .orElse(Try(js.Dynamic.global.global))
      .orElse(Try(js.Dynamic.global.globalThis))
      .orElse(Try(js.Dynamic.global.self))
      .orElse(Try(js.Dynamic.global.window))
      .get

  def optionalField(subject: js.Object, name: String): js.UndefOr[js.Dynamic] =
    subject.asInstanceOf[js.Dynamic].selectDynamic(name).asInstanceOf[js.UndefOr[js.Dynamic]]

  def typeOfOptionalField(subject: js.Object, name: String): String =
    js.typeOf(optionalField(subject, name))

  def querySelectorFn(node: dom.Node): js.UndefOr[js.Function1[String, dom.Element]] = {
    if (typeOfOptionalField(node, "querySelector") == "function")
      js.defined(s => node.asInstanceOf[js.Dynamic].querySelector(s).asInstanceOf[dom.Element])
    else
      ()
  }

  def querySelectorAllFn(node: dom.Node): js.UndefOr[js.Function1[String, dom.NodeList[dom.Element]]] = {
    if (typeOfOptionalField(node, "querySelectorAll") == "function")
      js.defined(s => node.asInstanceOf[js.Dynamic].querySelectorAll(s).asInstanceOf[dom.NodeList[dom.Element]])
    else
      ()
  }

  def querySelector(node: dom.Node, selectors: String): js.UndefOr[dom.Element] =
    querySelectorFn(node).map(_ apply selectors)

  def querySelectorAll(node: dom.Node, selectors: String): js.UndefOr[dom.NodeList[dom.Element]] =
    querySelectorAllFn(node).map(_ apply selectors)
}
