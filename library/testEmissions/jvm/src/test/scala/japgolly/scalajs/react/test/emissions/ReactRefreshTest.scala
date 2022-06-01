package japgolly.scalajs.react.test.emissions

import japgolly.microlibs.utils.FileUtils
import japgolly.scalajs.react.test.emissions.util._
import scala.util.Try
import utest._
import utest.framework.TestPath

object ReactRefreshTest extends TestSuite {

  override def tests = Tests {

    "js" - {
      "demo" - testJs()
      // "temp" - testJs()
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
  protected def testScala(assertRR          : Boolean     = true,
                          assertNoRR        : Boolean     = false,
                          assertBabelChanges: Boolean     = true,
                          showResult        : Boolean     = false,
                          showPreBabel      : Boolean     = false,
                          golden            : Boolean     = true,
                          hack              : TestJs.Hack = null,
                          expectedFrags     : Seq[String] = Seq.empty)
                         (implicit tp       : TestPath) = {

    val pkg            = Props.rootPkg
    val name           = tp.value.last
    val actualFilename = s"${Props.jsOutputDir}/$pkg.$name" + "$.js"
    val tempFilename   = s"${Props.tempDir}/$name.js"
    val expectFilename = s"${Props.testResDir}/${Props.resSubdirScalaRR}/$name-out.js"
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

      if (utestOutput == () && assertRR)
        utestOutput =
          babel.after.replace('\n', ' ') match {
            case rrSigHashRegex(h) => h
            case _                 => "Failed to find the RefreshSig state id"
          }

    } finally {
      if (showPreBabel)
        Util.debugShowContent(s"$name.scala JS pre-babel", babel.before, "\u001b[107;30m")
      if (showResult)
        Util.debugShowContent(s"$name.scala JS post-babel", babel.after, "\u001b[107;30m")
    }

    utestOutput
  }

  private val rrSigHashRegex = """.*, ?"([a-zA-Z0-9/+]{27}=)"\);.*""".r
}
