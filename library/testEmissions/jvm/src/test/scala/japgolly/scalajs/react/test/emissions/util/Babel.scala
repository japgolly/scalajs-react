package japgolly.scalajs.react.test.emissions.util

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

  def assertOutput(expect: String)(implicit l: Line): Unit = {
    val name = beforeFilename.replaceFirst("^.+/(.+)\\.js$", "$1")
    Util.assertJs(name, after, expect)
  }

  def assertOutputContains(frags: String*)(implicit l: Line): Unit =
    for (frag <- frags)
      if (!after.contains(frag)) {
        showBadOutput()
        fail("Output doesn't contain: " + frag)
      }

  private def showBadOutput(): Unit =
    Util.debugShowContent(s"$beforeFilename <output>", after, "\u001b[43;30m")

  def assertOrSaveOutput(filename: String)(implicit l: Line): Any =
    Util.readOrCreateFile(filename, after) match {
      case None    => s"Created $filename"
      case Some(s) => assertOutput(s)
    }

  def assertOrSaveOutput(filename: String, preCmpHack: TestJs.Hack)(implicit l: Line): Any =
    Util.readOrCreateFile(filename, after) match {
      case None    => s"Created $filename"
      case Some(e) =>
        val name = filename.replaceFirst("^.+/(.+?)(-out[23]?)?\\.js$", "$1")
        Util.assertJs(name, after, e, preCmpHack)
    }
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
    if (Util.exists(srcFilename))
      Node.babel(srcFilename, useCfgNorm)
    else
      throw new RuntimeException("File not found: " + srcFilename)

  def normaliseToFile(srcFilename: String, tgtFilename: String): Unit =
    if (Util.exists(srcFilename))
      Node.babel(srcFilename, useCfgNorm, "-o", tgtFilename)
    else
      throw new RuntimeException("File not found: " + srcFilename)
}
