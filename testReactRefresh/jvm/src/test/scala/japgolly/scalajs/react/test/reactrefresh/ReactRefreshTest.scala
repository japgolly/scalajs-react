package japgolly.scalajs.react.test.reactrefresh

import japgolly.microlibs.testutil.TestUtil._
import utest._
import utest.framework.TestPath
import japgolly.microlibs.utils.FileUtils

object ReactRefreshTest extends TestSuite {

  override def tests = Tests {
    "js" - {
      "demo" - testJsExpectation()
      // "temp" - testJsExpectation()
    }
    "scala" - {
      "Counter" - testScala()
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

  protected def testScala()(implicit tp: TestPath) = {
    val pkg          = "japgolly.scalajs.react.test.reactrefresh"
    val name         = tp.value.last
    val origFilename = s"${Props.jsOutputDir}/$pkg.$name" + "$.js"
    val tempFilename = s"${Props.tempDir}/$name.js"

    Babel.normaliseToFile(origFilename, tempFilename)
    applyTempHacks(tempFilename)

    val babel = Babel.dev(tempFilename)

    babel.assertChanged()
    // assertMultiline(babel.before, babel.after) // TODO: temp
    Util.debugShowContent(origFilename, babel.after, Console.RESET)
  }

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