package japgolly.scalajs.react.test.reactrefresh

import japgolly.microlibs.testutil.TestUtil._
import japgolly.microlibs.utils.FileUtils
import utest._
import utest.framework.TestPath

object ReactRefreshTest extends TestSuite {

  override def tests = Tests {
    "js" - {
      "demo" - testJs()
      // "temp" - testJs()
    }
    "scala" - {
      // "TEMPPPPPPP" - testScala(showBefore = true, show = false, expectRR = false, rememberOutput = false)()
      "Test"  - testScala(showBefore = true, show = false, expectRR = false, rememberOutput = false)()
      // "RewritePoC2"  - testScala(showBefore = false, show = true, expectRR = true, rememberOutput = false)()
      // "RewritePoC12" - testScala(showBefore = false, show = true, expectRR = true, rememberOutput = false)()
      // "UseState1" - testScala(show = true, expectRR = false, rememberOutput = true)()
      // "UseState1" - testScala()("bRrkbXoRYte9aIrMEzyIYQSTFt4=")
      // "UseState2" - testScala()("8pO47wStQLnq12ingXTgdp09akk=")
      // "UseStateMulti" - testScala(true)()
    }
  }

  // ===================================================================================================================

  protected def testJs()(implicit tp: TestPath) = {
    val dir            = "js"
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
      babel.rememberOrAssertOutput(expectFilename)
  }

  // ===================================================================================================================

  protected def testScala(show          : Boolean = false,
                          showBefore    : Boolean = false,
                          expectRR      : Boolean = true,
                          rememberOutput: Boolean = false,
                        )(expectedFrags: String*)
                         (implicit tp  : TestPath) = {

    val pkg            = "demo"
    val name           = tp.value.last
    val origFilename   = s"${Props.jsOutputDir}/$pkg.$name" + "$.js"
    val tempFilename   = s"${Props.tempDir}/$name.js"
    val expectFilename = s"${Props.testResDir}/sjs/$name-out.js"
    var testOutcome    = () : Any

    Babel.normaliseToFile(origFilename, tempFilename)
    applyTempHacks(name, tempFilename)
    val babel = Babel.dev(tempFilename)

    try {
      babel.assertChanged()
      babel.assertRR(expectRR)
      babel.assertOutputContains(expectedFrags: _*)

      if (rememberOutput)
        testOutcome = babel.rememberOrAssertOutput(expectFilename)

      if (testOutcome == () && expectRR)
        testOutcome =
          babel.after.replace('\n', ' ') match {
            case rrSigHashRegex(h) => h
            case _                 => "Failed to find the RefreshSig state id"
          }

    } finally {
      if (showBefore)
        Util.debugShowContent(s"$name.scala pre-babel js", babel.before, "\u001b[107;30m")
      if (show)
        Util.debugShowContent(s"$name.scala post-babel js", babel.after, "\u001b[107;30m")
    }

    testOutcome
  }

  private val rrSigHashRegex = """.*, ?"([a-zA-Z0-9/+]{27}=)"\);.*""".r

  protected def applyTempHacks(name: String, filename: String): Unit = {
    val before = Util.needFileContent(filename)

    var after = before

    // if (name startsWith "RewritePoC")
    //   after = after.replaceAll("""\(this\$\d+ => """, "").replaceAll("""\)\(this(?:\$\d+)?\)""", "")

    after = {
      var allow = true
      val exportPat = """^export \{ (\S+) \};?$""".r
      after
        .linesIterator
        .filter(_ => allow)
        .map {
          case exportPat(name) =>
            allow = false
            s"export default $name;"
          // case s if !s.startsWith("import ") && s.contains("seState") =>
          //   "/*↓*/\n" + s
          case s =>
            s
        }
        .mkString("\n")
    }

    if (before !=* after) {
      FileUtils.write(filename, after)
      // val after2 = Babel.normaliseToStr(filename)
      // FileUtils.write(filename, after2)
    }
  }
}