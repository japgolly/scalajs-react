package japgolly.scalajs.react.test

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router
import scala.annotation.nowarn
import scala.reflect.ClassTag
import scala.scalajs.js
import scalaz.{Equal, Maybe}
import sourcecode.Line
import utest.CompileError

object TestUtil extends TestUtil

trait TestUtil
  extends japgolly.microlibs.testutil.TestUtil
     with scalaz.std.ListInstances
     with scalaz.std.OptionInstances
     with scalaz.std.VectorInstances {

  implicit final def equalKey          : UnivEq[Key]            = UnivEq.force
  implicit final def routerEqualBaseUrl: UnivEq[router.BaseUrl] = UnivEq.force
  implicit final def routerEqualPath   : UnivEq[router.Path]    = UnivEq.force
  implicit final def routerEqualAbsUrl : UnivEq[router.AbsUrl]  = UnivEq.force

  // TODO erm... not really. Only allow in raw testing
  implicit val equalRawRef: Equal[japgolly.scalajs.react.facade.React.Ref] = Equal.equalRef
  implicit def equalRawRefHandle[A]: Equal[japgolly.scalajs.react.facade.React.RefHandle[A]] = Equal.equalRef
  implicit def equalRefSimple[A]: Equal[Ref.Simple[A]] = Equal.equalRef

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

  def assertMaybeContains(actual: String, substr: String, expect: Boolean)(implicit q: Line): Unit =
    if (expect)
      assertContains(actual, substr)
    else
      assertNotContains(actual, substr)

  def assertOuterHTML(node: TopNode, expect: String)(implicit l: Line): Unit =
    assertOuterHTML(null, node, expect)

  def assertOuterHTML(name: => String, node: TopNode, expect: String)(implicit l: Line): Unit =
    assertEq(name, scrubReactHtml(node.outerHTML), expect)

  private val reactRubbish = """\s+data-react\S*?\s*?=\s*?".*?"|<!--(?:.|[\r\n])*?-->""".r

  def scrubReactHtml(html: String): String =
    reactRubbish.replaceAllIn(html, "")

  def assertRender(u: GenericComponent.UnmountedRaw, expected: String)(implicit l: Line): Unit =
    assertRender(u.raw, expected)

  def assertRender(e: japgolly.scalajs.react.vdom.VdomElement, expected: String)(implicit l: Line): Unit =
    assertRender(e.rawElement, expected)

  def assertRender(e: japgolly.scalajs.react.facade.React.Element, expected: String)(implicit l: Line): Unit = {
    val rendered: String = ReactDOMServer.raw.renderToStaticMarkup(e)
    assertEq(rendered, expected)
  }

  def assertRendered(n: TopNode, expected: String)(implicit l: Line): Unit = {
    val rendered: String = ReactTestUtils.removeReactInternals(n.outerHTML)
    assertEq(rendered, expected)
  }

  def assertTypeMismatch(e: CompileError)(implicit l: Line): Unit =
    assertContainsAny(e.msg, "Required:", "type mismatch")

  implicit class JsArrayTestExt[A](private val a: js.Array[A]) {
    def sole()(implicit l: Line): A =
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

  @nowarn("cat=unused")
  def assertCompiles[A](a: => A): Unit = ()
}

