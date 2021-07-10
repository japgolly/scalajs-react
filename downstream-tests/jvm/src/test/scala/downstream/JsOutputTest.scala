package downstream

import japgolly.microlibs.testutil.TestUtil._
import japgolly.microlibs.testutil.TestUtilInternals._
import scala.Console._
import sourcecode.Line
import utest._

object JsOutputTest extends TestSuite {
  import Props._

  private def assertLegalValue(str: String)(legal: String*)(implicit l: Line): Unit =
    if (!legal.contains(str))
      fail(s"Illegal value: '$str'. Legal values are: " + legal.sorted.mkString("", ", ", "."))

  private def contentTest(prop: String, legalValuesByComma: String)
                         (propValueToSubstr: (String, String)*)
                         (implicit l: Line): Unit = {
    assertLegalValue(prop)(legalValuesByComma.split(',').toSeq: _*)

    val expectToSubstr =
      propValueToSubstr.map { case (pv, substr) =>
        val expect = (pv != null) && prop.matches(pv)
        expect -> substr
      }

    contentTest(expectToSubstr: _*)
  }

  private def never: String =
    null

  private def fgrep(term: String): Unit = {
    println(s"> fgrep '$term'")
    content
      .linesIterator
      .zipWithIndex
      .filter(_._1.contains(term))
      .map { case (s, l) => s"$GREEN$l:$RESET " + s.replace(term, MAGENTA_B + WHITE + term + RESET) }
      .foreach(println)
  }

  private def contentTest(expectToSubstr: (Boolean, String)*)(implicit l: Line): Unit = {
    System.out.flush()
    System.err.flush()
    var errors = List.empty[String]
    for ((expect, substr) <- expectToSubstr) {
      val actual = content.contains(substr)
      val pass   = actual == expect
      val result = if (pass) s"${GREEN}pass$RESET" else s"${RED_B}${WHITE}FAIL$RESET"
      val should = if (expect) "should" else "shouldn't"
      val strCol = if (expect) (GREEN + BRIGHT_GREEN) else BRIGHT_BLACK
      println(s"[$result] JS $should contain $strCol$substr$RESET")
      if (!pass) errors ::= s"JS $should contain $substr"
    }
    System.out.flush()
    if (errors.nonEmpty) {
      for ((_, substr) <- expectToSubstr)
        fgrep(substr)
      fail(errors.sorted.mkString(", "))
    }
  }

  override def tests = Tests {

    "size" - "%,d bytes".format(content.length)

    "carrot" - {
      val t = if (dsCfg1) "custom" else compnameAll
      contentTest(t, "allow,blank,custom")(
        "allow"  -> "\"CarRot!\"",
        "custom" -> "\"CarRot!-MOD\"",
      )
    }

    "pumpkin" - {
      val t = if (dsCfg1) "custom" else if (compnameAll == "blank") "blank" else compnameAuto
      contentTest(t, "full,short,blank,custom")(
        "short"  -> "\"Pumpkin\"",
        "full"   -> "\"downstream.Pumpkin\"",
        "custom" -> "\"downstream.Pumpkin-AUTO-MOD\"",
        never    -> "automaticComponentName__T__T"
      )
    }

    "ReusabilityOverlay" - {
      val expect = fastOptJS && reusabilityDev.contains("overlay") && configClass.isEmpty
      contentTest(expect -> "ReusabilityOverlay")
    }

    "devAssertWarn" - contentTest(
      true      -> "http://some.url",
      fastOptJS -> "Consider using BaseUrl.fromWindowOrigin",
    )

  }
}
