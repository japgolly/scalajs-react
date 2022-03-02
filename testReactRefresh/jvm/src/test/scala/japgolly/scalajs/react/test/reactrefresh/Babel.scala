package japgolly.scalajs.react.test.reactrefresh

import japgolly.microlibs.testutil.TestUtil._
import sourcecode.Line

final case class Babel(before        : String,
                       beforeFilename: String,
                       after         : String) {

  def assertChanged()(implicit l: Line): Unit =
    if (before ==* after) {
      Util.debugShowContent(beforeFilename, before, "\u001b[43;30m")
      fail(s"React Refresh Babel plugin didn't make any changes")
    }

  def assertOutput(is: String)(implicit l: Line): Unit =
    assertMultiline(actual = after, expect = is)
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