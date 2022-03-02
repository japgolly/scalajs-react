package japgolly.scalajs.react.test.reactrefresh

import japgolly.microlibs.testutil.TestUtil._
import utest._
import utest.framework.TestPath
import japgolly.microlibs.utils.FileUtils

object ReactRefreshTest extends TestSuite {

  override def tests = Tests {
    "js" - {
      // "demo" - testJsExpectation()
      // "temp" - testJsExpectation()
    }
    "scala" - {
      "UseState1" - testScala(true)()
      // "UseState1" - testScala()("bRrkbXoRYte9aIrMEzyIYQSTFt4=")
      // "UseState2" - testScala()("8pO47wStQLnq12ingXTgdp09akk=")
      // "UseStateMulti" - testScala(true)()
    }
  }

  // ===================================================================================================================

  protected def testJsExpectation()(implicit tp: TestPath) = {
    val dir            = "js-expectations"
    val name           = tp.value.last
    val base           = s"${Props.testResDir}/$dir/$name"
    val inFilename     = s"$base-in.js"
    val expectFilename = s"$base-out.js"
    val tempFilename   = s"${Props.tempDir}/$dir-$name-in.js"
    val babel          = Babel.dev(inFilename, tempFilename)
    val expect         = Util.getFileContent(expectFilename)

    // For temp code, just show the output on screen
    if (name.matches(""".*\bte?mp\b.*""")) {
      Util.debugShowContent(s"$inFilename <output>", babel.after, "\u001b[106;30m")
      ()

    } else
      expect match {

        // If no expectation file exists, create it
        case None =>
          println(s"Expectation JS not found: $expectFilename")
          println("  Saving ...")
          FileUtils.write(expectFilename, babel.after)
          s"Created $expectFilename"

        // Compare against expectation
        case Some(e) =>
          babel.assertOutput(e)
          ()
      }
  }

  // ===================================================================================================================

  protected def testScala(show         : Boolean = false,
                          expectRR     : Boolean = true)
                         (expectedFrags: String*)
                         (implicit tp  : TestPath) = {

    val pkg          = "japgolly.scalajs.react.test.reactrefresh"
    val name         = tp.value.last
    val origFilename = s"${Props.jsOutputDir}/$pkg.$name" + "$.js"
    val tempFilename = s"${Props.tempDir}/$name.js"

    Babel.normaliseToFile(origFilename, tempFilename)
    applyTempHacks(tempFilename)
    val babel = Babel.dev(tempFilename)

    babel.assertChanged()
    babel.assertRR(expectRR)
    babel.assertOutputContains(expectedFrags: _*)

    if (show)
      Util.debugShowContent(origFilename, babel.after, "\u001b[107;30m")

    if (expectRR)
      babel.after.replace('\n', ' ') match {
        case rrSigHashRegex(h) => h
        case _                 => "Failed to find the RefreshSig state id"
      }
    else
      "No RR"
  }

  private val rrSigHashRegex = """.*, ?"([a-zA-Z0-9]{27}=)"\);.*""".r

  protected def applyTempHacks(filename: String): Unit = {
    val before = Util.needFileContent(filename)

    val after = {
      var allow = true
      val exportPat = """^export \{ (\S+) \};?$""".r
      before
        .linesIterator
        .filter(_ => allow)
        .map {
          case exportPat(name) =>
            allow = false
            s"export default $name;"
          case s if !s.startsWith("import ") && s.contains("seState") =>
            "/*↓*/\n" + s
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