package japgolly.scalajs.react.test

import org.scalajs.dom.{document, html}
import scala.io.AnsiColor._
import scala.reflect.ClassTag
import scala.scalajs.js
import scalaz.{Equal, Maybe}
import scalaz.syntax.equal._
import utest.CompileError
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.PackageBase._

object TestUtil extends TestUtil

trait TestUtil
  extends scalaz.std.StringInstances
     with scalaz.std.StreamInstances
     with scalaz.std.VectorInstances
     with scalaz.std.SetInstances
     with scalaz.std.TupleInstances
     with scalaz.std.OptionInstances
     with scalaz.std.AnyValInstances
     with scalaz.std.ListInstances {

  implicit val equalNull: Equal[Null] = Equal.equalA
  implicit val equalKey: Equal[Key] = Equal.equalA

  // TODO erm... not really. Only allow in raw testing
  implicit val equalRawRef: Equal[japgolly.scalajs.react.raw.React.Ref] = Equal.equalRef
  implicit def equalRawRefHandle[A]: Equal[japgolly.scalajs.react.raw.React.RefHandle[A]] = Equal.equalRef
  implicit def equalRefSimple[A]: Equal[Ref.Simple[A]] = Equal.equalRef

  implicit def jsUndefOrEqual[A](implicit e: Equal[A]): Equal[js.UndefOr[A]] =
    Equal.equal[js.UndefOr[A]]((a, b) =>
      if (a.isEmpty) b.isEmpty else b.exists(e.equal(a.get, _)))

  def assertEq[A: Equal](actual: A, expect: A): Unit =
    assertEqO(None, actual, expect)

  def assertEq[A: Equal](name: => String, actual: A, expect: A): Unit =
    assertEqO(Some(name), actual, expect)

  private def lead(s: String) = s"$RED_B$s$RESET "
  private def failureStart(name: Option[String], leadSize: Int): Unit = {
    println()
    name.foreach(n => println(lead(">" * leadSize) + BOLD + YELLOW + n + RESET))
  }

  def assertEqO[A: Equal](name: => Option[String], actual: A, expect: A): Unit =
    if (actual ≠ expect) {
      failureStart(name, 7)

      val toString: Any => String = {
        case s: Stream[_] => s.force.toString() // SI-9266
        case a            => a.toString
      }

      val as = toString(actual)
      val es = toString(expect)
      val ss = as :: es :: Nil
      var pre = "["
      var post = "]"
      val htChars = ss.flatMap(s => s.headOption :: s.lastOption :: Nil)
      if (htChars.forall(_.exists(c => !Character.isWhitespace(c)))) {
        pre = ""
        post = ""
      }
      if (ss.exists(_ contains "\n")) {
        pre = "↙[\n"
      }
      println(lead("expect:") + pre + BOLD + GREEN + es + RESET + post)
      println(lead("actual:") + pre + BOLD + RED + as + RESET + post)
      println()
      fail(s"assertEq${name.fold("")("(" + _ + ")")} failed.")
    }

  def assertMultiline(actual: String, expect: String): Unit =
    if (actual != expect) {
      println()
      val AE = List(actual, expect).map(_.split("\n"))
      val List(as, es) = AE
      val lim = as.length max es.length
      val List(maxA,_) = AE.map(x => (0 #:: x.map(_.length).toStream).max)
      val maxL = lim.toString.length
      println("A|E")
      val fmt = s"%s%${maxL}d: %-${maxA}s |%s| %s$RESET\n"
      def removeWhitespace(s: String) = s.filterNot(_.isWhitespace)
      for (i <- 0 until lim) {
        val List(a, e) = AE.map(s => if (i >= s.length) "" else s(i))
        val ok = a == e
        val cmp = if (ok) " " else if (removeWhitespace(a) == removeWhitespace(e)) "≈" else "≠"
        val col = if (ok) BOLD + BLACK else WHITE
        printf(fmt, col, i + 1, a, cmp, e)
      }
      println()
      fail("assertMultiline failed.")
    }

  def fail(msg: String, clearStackTrace: Boolean = true): Nothing =
    _fail(colourMultiline(msg, BOLD + MAGENTA), clearStackTrace)

  def _fail(msg: String, clearStackTrace: Boolean = true): Nothing = {
    val e = new AssertionError(msg)
    if (clearStackTrace)
      e.setStackTrace(Array.empty)
    throw e
  }

  private def colourMultiline(text: String, colour: String): String =
    colour + text.replace("\n", "\n" + colour) + RESET

  implicit class AnyTestExt[A](a: A) {

    def some: Option[A] = Some(a)
    def none: Option[A] = None

    def jsdef: js.UndefOr[A] = a
    def undef: js.UndefOr[A] = js.undefined

    def just    : Maybe[A] = Maybe.just(a)
    def maybeNot: Maybe[A] = Maybe.empty

    def matchesBy[B <: A : ClassTag](f: B => Boolean) = a match {
      case b: B => f(b)
      case _ => false
    }
  }

  def none[A]: Option[A] = None

  final type TopNode = org.scalajs.dom.Element

  def assertOuterHTML(node: TopNode, expect: String): Unit =
    assertOuterHTML(null, node, expect)

  def assertOuterHTML(name: => String, node: TopNode, expect: String): Unit =
    assertEq(name, scrubReactHtml(node.outerHTML), expect)

  private val reactRubbish = """\s+data-react\S*?\s*?=\s*?".*?"|<!--(?:.|[\r\n])*?-->""".r

  def scrubReactHtml(html: String): String =
    reactRubbish.replaceAllIn(html, "")

  def assertRender(u: GenericComponent.UnmountedRaw, expected: String): Unit =
    assertRender(u.raw, expected)
  def assertRender(e: japgolly.scalajs.react.vdom.VdomElement, expected: String): Unit =
    assertRender(e.rawElement, expected)
  def assertRender(e: japgolly.scalajs.react.raw.React.Element, expected: String): Unit = {
    val rendered: String = ReactDOMServer.raw.renderToStaticMarkup(e)
    assertEq(rendered, expected)
  }

  def assertRendered(n: TopNode, expected: String): Unit = {
    val rendered: String = ReactTestUtils.removeReactInternals(n.outerHTML)
    assertEq(rendered, expected)
  }

  def assertContains(value: String, search: String, expect: Boolean = true): Unit =
    if (value.contains(search) != expect) {
      println(s"\nValue: $value\nSearch: $search\nExpect: $expect\n")
      assert(false)
    }

  def assertTypeMismatch(e: CompileError): Unit =
    assertContains(e.msg, "type mismatch")

  implicit class JsArrayTestExt[A](private val a: js.Array[A]) {
    def sole(): A =
      a.length match {
        case 1 => a(0)
        case n => fail(s"Expected an array with one element, found $n: ${a.mkString("[", ",", "]")}")
      }
  }

  def yesItsMounted: Option[Boolean] = Some(true)
  def nopeNotMounted: Option[Boolean] = Some(false)

  def catchError[A](a: => A): Option[Throwable] =
    try {
      a
      None
    }
    catch {
      case t: Throwable => Some(t)
    }

  def expectError[A](a: => A): Throwable =
    catchError(a).getOrElse(fail("Error expected but code succeeded."))

  def expectErrorContaining[A](errFrag: String)(a: => A): String = {
    val err = expectError(a).getMessage
    assertContains(err, errFrag)
    err
  }

  def assertCompiles[A](a: => A): Unit = ()
}

