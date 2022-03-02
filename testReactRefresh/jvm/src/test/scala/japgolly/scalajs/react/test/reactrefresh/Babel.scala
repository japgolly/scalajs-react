package japgolly.scalajs.react.test.reactrefresh

import japgolly.microlibs.testutil.TestUtil._
import sourcecode.Line

final case class Babel(before        : String,
                       beforeFilename: String,
                       after         : String) {

  def assertChanged()(implicit l: Line): Unit =
    if (before ==* after) {
      Util.debugShowContent(beforeFilename, before, "\u001b[43;30m")
      fail("React Refresh Babel plugin didn't make any changes")
    }

  def assertRR(expectInstalled: Boolean = true)(implicit l: Line): Unit = {
    val actual = Util.countRR(after)
    val expect = if (expectInstalled) 1 else 0
    if (actual !=* expect) {
      showBadOutput()
      assertEq("RR installations", actual, expect)
    }
  }

  def assertOutput(is: String)(implicit l: Line): Unit =
    assertMultiline(actual = after, expect = is)

  def assertOutputContains(frags: String*)(implicit l: Line): Unit =
    for (frag <- frags)
      if (!after.contains(frag)) {
        showBadOutput()
        fail("Output doesn't contain: " + frag)
      }

  private def showBadOutput(): Unit =
    Util.debugShowContent(s"$beforeFilename <output>", after, "\u001b[43;30m")
}

object Babel {

  def dev(origFilename: String, tempFilename: String): Babel = {
    normaliseToFile(origFilename, tempFilename)
    dev(tempFilename)
  }

  def dev(filename: String): Babel = {
    val before = Util.needFileContent(filename)
    val after  = Node.babel(filename, "--config-file=./babel.dev.json")
    Babel(
      before         = before,
      beforeFilename = filename,
      after          = after,
    )
  }

  private def useCfgNorm = "--config-file=./babel.norm.json"

  def normaliseToStr(srcFilename: String): String =
    Node.babel(srcFilename, useCfgNorm)

  def normaliseToFile(srcFilename: String, tgtFilename: String): Unit =
    Node.babel(srcFilename, useCfgNorm, "-o", tgtFilename)
}