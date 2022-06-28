package japgolly.scalajs.react.test.emissions

import japgolly.microlibs.testutil.TestUtil._
import japgolly.microlibs.utils.FileUtils
import japgolly.scalajs.react.test.emissions.util._
import java.lang.{Boolean => JBoolean}
import scala.Console._
import scala.annotation.nowarn
import scala.util.Try
import utest._
import utest.framework.TestPath

object ReactRefreshTest extends TestSuite {

  override def tests = Tests {

    "js" - {
      "fn"           - testJs()
      "hooks"        - testJs()
      "custom_hooks" - testJs()
      // "temp"      - testJs()
    }

    "sjr" - {
      "CustomHooks"                 - testScala(assertRR = false) // TODO:
      "HooksPrimative"              - testScala(assertRR = false) // TODO:
      "HooksTrivial"                - testScala()
      "HooksWithChildrenCtxFn"      - testScala()
      "HooksWithChildrenCtxObj"     - testScala()
      "HooksWithJsFns"              - testScala()
      "HooksWithScalaFns"           - testScala()
      "JustPropsChildrenViaHookApi" - testScala(assertRR = false) // TODO:
      "JustPropsViaHookApi"         - testScala(assertRR = false) // TODO:
      "RenderReusable"              - testScala(expectedInstalls = 6)
      "RenderWithReuse"             - testScala(expectedInstalls = 6)
      "RenderWithReuseBy"           - testScala(expectedInstalls = 6)
      "UseCallback"                 - testScala()
      "UseEffect"                   - testScala()
      "UseMemo"                     - testScala()
      "UseRef"                      - testScala()
      "UseStateWithReuse"           - testScala()
    }

    "version" - validateReactRefreshVersion()
  }

  // ===================================================================================================================

  private var globalFailure = Option.empty[RuntimeException]

  private def ignoreReactRefreshUpdate(ver: String): Boolean = {
    val ignore = Set[String]()
    ignore.contains(ver)
  }

  private def validateReactRefreshVersion(): Any = {
    val r = Node.run("npm", "outdated")
    r.exitStatus match {
      case 0 =>
        // everything is up to date
      case 1 =>
        r.out.linesIterator.find(_ startsWith "react-refresh ") match {
          case None =>
            // react-refresh is up to date
          case Some(line) =>
            val latest = line.split("\\s+")(3)
            val msg = s"react-refresh is out-of-date. Latest version is $YELLOW_B$latest$RESET"
            if (ignoreReactRefreshUpdate(latest))
              msg // render as test output
            else {
              val e = new RuntimeException(msg)
              globalFailure = Some(e)
              throw e
            }
        }
      case _ =>
        r.assertExitStatus()
    }
  }

  private def preTest(): Unit =
    globalFailure.foreach(throw _)

  /** Load a JS file in /resources/, run ReactRefresh transforms over it, and confirm the result. */
  protected def testJs()(implicit tp: TestPath) = {
    preTest()

    val dir            = Props.resSubdirJsRR
    val name           = tp.value.last
    val base           = s"${Props.testResDir}/$dir/$name"
    val inFilename     = s"$base-in.js"
    val expectFilename = s"$base-out.js"
    val tempFilename   = s"${Props.tempDir}/$dir-$name-in.js"
    val babel          = Babel.dev(inFilename, tempFilename)

    // For temp code, just show the output on screen
    if (name.matches(""".*\bte?mp\b.*""")) {
      Util.debugShowContent(s"$inFilename <output>", babel.after, "\u001b[106;30m")
      ()
    } else
      babel.assertOrSaveOutput(expectFilename) match {
        case () => testOutputFromSig(babel.after, null)
        case r  => r
      }
  }

  /** Load scalajs-react output JS, run ReactRefresh transforms over it, and confirm the result. */
  @nowarn("cat=unused")
  protected def showScala(assertRR          : JBoolean    = null,
                          assertBabelChanges: JBoolean    = null,
                          showPreBabel      : Boolean     = false,
                          showResult        : Boolean     = true,
                          showDiff          : Boolean     = true,
                          golden            : Boolean     = false,
                          hack              : TestJs.Hack = null,
                          onCmp             : TestJs.Hack = TestJs.Hack.forComparison,
                          onShow            : TestJs.Hack = TestJs.Hack.humanReadable,
                          expectedInstalls  : Int         = 1,
                          expectedFrags     : Seq[String] = Seq.empty)
                         (implicit tp       : TestPath) =
  testScala(
    assertRR           = null,
    assertBabelChanges = null,
    showPreBabel       = showPreBabel,
    showResult         = showResult,
    showDiff           = showDiff,
    golden             = false,
    hack               = hack,
    onCmp              = onCmp,
    onShow             = onShow,
    expectedInstalls   = expectedInstalls,
    expectedFrags      = expectedFrags,
  )

  /** Load scalajs-react output JS, run ReactRefresh transforms over it, and confirm the result. */
  protected def testScala(assertRR          : JBoolean    = true,
                          assertBabelChanges: JBoolean    = null,
                          showPreBabel      : Boolean     = false,
                          showResult        : Boolean     = false,
                          showDiff          : Boolean     = false,
                          golden            : Boolean     = true,
                          hack              : TestJs.Hack = null,
                          onCmp             : TestJs.Hack = TestJs.Hack.forComparison,
                          onShow            : TestJs.Hack = TestJs.Hack.humanReadable,
                          expectedInstalls  : Int         = 1,
                          expectedFrags     : Seq[String] = Seq.empty)
                         (implicit tp       : TestPath) = {
    preTest()

    val pkg            = Props.rootPkg
    val name           = tp.value.last
    val actualFilename = s"${Props.jsOutputDir}/$pkg.$name" + "$.js"
    val tempFilename   = s"${Props.tempDir}/$name.js"
    val expectFilename = s"${Props.testResDir}/${Props.resSubdirScalaRR}/$name-out${Props.scalaMajorVer}.js"
    var utestOutput    = () : Any

    Babel.normaliseToFile(actualFilename, tempFilename)

    if (hack ne null) {
      val js = new TestJs(name = name, filename = tempFilename)
      hack.run(js)
      if (js.changed())
        FileUtils.write(js.filename, js.content)
    }

    val babel =
      try
        Babel.dev(tempFilename)
      catch {
        case t: Throwable =>
          Try {
            val content = Util.needFileContent(tempFilename)
            Util.debugShowContent(s"$name.scala JS pre-babel error", content, "\u001b[107;30m")
          }
          throw t
      }

    try {
      if (assertBabelChanges || (assertBabelChanges == null && assertRR))
        babel.assertChanged()

      if (assertRR)
        babel.assertRR(true, expectedInstalls)

      if (assertRR == false)
        babel.assertRR(false)

      babel.assertOutputContains(expectedFrags: _*)

      if (golden)
        utestOutput = babel.assertOrSaveOutput(expectFilename, onCmp)

      if (utestOutput == ())
        utestOutput = testOutputFromSig(babel.after, assertRR)

    } finally {
      def show(s: String): String = onShow.runAs(name, s).content
      lazy val before = show(babel.before)
      lazy val after = show(babel.after)
      if (showPreBabel)
        Util.debugShowContent(s"$name.scala JS pre-babel", before, "\u001b[107;30m", rrFlags = false)
      if (showResult)
        Util.debugShowContent(s"$name.scala JS post-babel", after, "\u001b[107;30m")
      if (showDiff && (before !=* after))
        Util.debugShowDiff(before, after)
    }

    utestOutput
  }

  private val rrSigRegex = """^.*(?:_s.*|},) "([a-zA-Z0-9/+]{27}=)"(?:, .+|\);)?$""".r

  private def reactRefreshSignature(js: String): Option[String] = {
    val all: String =
      js.linesIterator.flatMap {
        case rrSigRegex(h) => Some(h)
        case _             => None
      }.mkString(" / ")

    Option.when(all.nonEmpty)(all)
  }

  private def testOutputFromSig(js: String, expect: JBoolean): Any =
    if (expect == false)
      // "react-refresh not installed"
      "-"
    else
      reactRefreshSignature(js) match {
        case Some(sig) => sig
        case None      => if (expect) "Failed to find the RefreshSig state id" else ()
      }
}
