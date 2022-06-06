package japgolly.scalajs.react.test.emissions

import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react.test.emissions.util._
import utest._
import utest.framework.TestPath

object EmissionTest extends TestSuite {

  override def tests = Tests {

    // "UseState" - testScala(hack = TestJs.Hack { _
    //   .dropLinesUntil(_ endsWith " = function (props) {")
    //   .takeLinesTo(_ == "};")
    // }.disable, golden = false)

  }

  // ===================================================================================================================

  /** Load scalajs-react output JS, and confirm the result. */
  protected def testScala(showResult   : Boolean     = false,
                          golden       : Boolean     = true,
                          normalise    : Boolean     = true,
                          hack         : TestJs.Hack = null,
                          expectedFrags: Seq[String] = Seq.empty)
                         (implicit tp  : TestPath) = {

    val pkg            = Props.rootPkg
    val name           = tp.value.last
    val actualFilename = s"${Props.jsOutputDir}/$pkg.$name" + "$.js"
    val tempFilename   = s"${Props.tempDir}/$name.js"
    val expectFilename = s"${Props.testResDir}/${Props.resSubdirScala}/$name-out${Props.scalaMajorVer}.js"
    var utestOutput    = () : Any

    val actual = {

      val filename =
        if (normalise) {
          Babel.normaliseToFile(actualFilename, tempFilename)
          tempFilename
        } else
          actualFilename

      val js = new TestJs(name = name, filename = filename)

      // Make output much more readable, we're not testing the validity of SJS here
      if (normalise)
        TestJs.Hack.humanReadable.run(js)

      if (hack ne null)
        hack.run(js)

      js.content
    }

    try {
      assertContainsAll(actual, expectedFrags: _*)

      if (golden)
        Util.useOrCreateFile(expectFilename, actual, assertMultiline(actual, _)) match {
          case None    => utestOutput = s"Created $expectFilename"
          case Some(_) =>
        }

    } finally {
      val didNothing = !golden && expectedFrags.isEmpty
      if (showResult || didNothing)
        Util.debugShowContent(s"$name.scala JS", actual, "\u001b[107;30m", rrFlags = false)
    }

    utestOutput
  }

}
