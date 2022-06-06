package japgolly.scalajs.react.test.emissions

import japgolly.microlibs.utils.FileUtils
import japgolly.scalajs.react.test.emissions.util._
import japgolly.univeq._
import scala.annotation.nowarn
import scala.util.Try
import utest._
import utest.framework.TestPath

object ReactRefreshTest extends TestSuite {

  override def tests = Tests {

    "js" - {
      "fn" - testJs()
      "hooks" - testJs()
      // "temp" - testJs()
    }

    "sjr" - {
      "HooksWithChildren" - testScala()
      "UseState" - testScala()
    }
  }

  // ===================================================================================================================

  /** Load a JS file in /resources/, run ReactRefresh transforms over it, and confirm the result. */
  protected def testJs()(implicit tp: TestPath) = {
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
      babel.assertOrSaveOutput(expectFilename)
  }

  /** Load scalajs-react output JS, run ReactRefresh transforms over it, and confirm the result. */
  @nowarn("cat=unused")
  protected def showScala(assertRR          : Boolean     = false,
                          assertNoRR        : Boolean     = false,
                          assertBabelChanges: Boolean     = false,
                          showPreBabel      : Boolean     = false,
                          showResult        : Boolean     = true,
                          showDiff          : Boolean     = true,
                          golden            : Boolean     = false,
                          hack              : TestJs.Hack = null,
                          onShow            : TestJs.Hack = TestJs.Hack.humanReadable,
                          expectedFrags     : Seq[String] = Seq.empty)
                         (implicit tp       : TestPath) =
  testScala(
    assertRR           = false,
    assertNoRR         = false,
    assertBabelChanges = false,
    showPreBabel       = showPreBabel,
    showResult         = showResult,
    showDiff           = showDiff,
    golden             = false,
    hack               = hack,
    onShow             = onShow,
    expectedFrags      = expectedFrags,
  )

  /** Load scalajs-react output JS, run ReactRefresh transforms over it, and confirm the result. */
  protected def testScala(assertRR          : Boolean     = true,
                          assertNoRR        : Boolean     = false,
                          assertBabelChanges: Boolean     = true,
                          showPreBabel      : Boolean     = false,
                          showResult        : Boolean     = false,
                          showDiff          : Boolean     = false,
                          golden            : Boolean     = true,
                          hack              : TestJs.Hack = null,
                          onShow            : TestJs.Hack = TestJs.Hack.humanReadable,
                          expectedFrags     : Seq[String] = Seq.empty)
                         (implicit tp       : TestPath) = {

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
      if (assertBabelChanges)
        babel.assertChanged()

      if (assertRR)
        babel.assertRR(true)

      if (assertNoRR)
        babel.assertRR(false)

      babel.assertOutputContains(expectedFrags: _*)

      if (golden)
        utestOutput = babel.assertOrSaveOutput(expectFilename)

      if (utestOutput == ())
        reactRefreshSignature(babel.after) match {
          case Some(sig) => utestOutput = sig
          case None =>
            if (assertRR)
              utestOutput = "Failed to find the RefreshSig state id"
        }

    } finally {
      def show(s: String): String = onShow.runAnon(s).content
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

  private val rrSigHashRegex = """.*, ?"([a-zA-Z0-9/+]{27}=)"\);.*""".r

  private def reactRefreshSignature(js: String): Option[String] =
    js.replace('\n', ' ') match {
      case rrSigHashRegex(h) => Some(h)
      case _                 => None
    }
}
