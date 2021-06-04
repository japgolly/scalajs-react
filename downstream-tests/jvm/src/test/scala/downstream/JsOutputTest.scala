package downstream

import japgolly.microlibs.testutil.TestUtil._
import japgolly.microlibs.testutil.TestUtilInternals._
import scala.Console._
import sourcecode.Line
import utest._

object JsOutputTest extends TestSuite {
  import Props._

  private def failJsContains(substr: String, expect: Boolean): Nothing = {
    val err =
      if (expect)
        s"is supposed to contain [$substr] but it doesn't."
      else
        s"contains '$substr' but shouldn't."
    fail("JS output " + err)
  }

  private def assertLegalValue(str: String)(legal: String*)(implicit l: Line): Unit =
    if (!legal.contains(str))
      fail(s"Illegal value: '$str'. Legal values are: " + legal.sorted.mkString("", ", ", "."))

  private def contentTest(expect: Boolean, substr: String)(implicit l: Line): Unit =
    contentTest("a", "a,b")((if (expect) "a" else "b") -> substr)

  private def contentTest(prop: String, legalValuesByComma: String)
                         (propValueToSubstr: (String, String)*)
                         (implicit l: Line): Unit = {
    System.out.flush()
    System.err.flush()
    assertLegalValue(prop)(legalValuesByComma.split(','): _*)
    var errors = List.empty[String]
    for ((pv, substr) <- propValueToSubstr) {
      val expect = (pv != null) && prop.matches(pv)
      val actual = content.contains(substr)
      val pass   = actual == expect
      val result = if (pass) s"${GREEN}pass$RESET" else s"${RED_B}${WHITE}FAIL$RESET"
      val should = if (expect) "should" else "shouldn't"
      val strCol = if (expect) BRIGHT_GREEN else BRIGHT_BLACK
      println(s"[$result] JS $should contain $strCol$substr$RESET")
      if (!pass) errors ::= s"JS $should contain $substr"
    }
    System.out.flush()
    if (errors.nonEmpty) {
      for ((_, substr) <- propValueToSubstr)
        fgrep(substr)
      fail(errors.sorted.mkString(", "))
    }
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
      contentTest(expect, "ReusabilityOverlay")
    }

  }
}