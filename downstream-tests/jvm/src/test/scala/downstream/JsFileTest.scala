package downstream

import japgolly.microlibs.testutil.TestUtil._
import japgolly.microlibs.testutil.TestUtilInternals._
import scala.Console._
import scala.io.Source
import utest._
import java.io.File
import sourcecode.Line

object JsFileTest extends TestSuite {

  private object Prop {
    def get(property: String): Option[String] = {
      val o = Option(System.getProperty(property))
      println(s"$CYAN$property$RESET = $YELLOW${o.getOrElse("")}$RESET")
      o
    }

    def get(property: String, default: String): String =
      get(property).getOrElse(default)

    def need(property: String): String =
      get(property).getOrElse(throw new RuntimeException("Property not defined: " + property))
  }

  val content: String = {
    val path = Prop.need("js_file")
    val s = Source.fromFile(new File(path))
    try s.mkString finally s.close()
  }

  val compnameAll  = Prop.get("japgolly.scalajs.react.compname.all", "allow")
  val compnameAuto = Prop.get("japgolly.scalajs.react.compname.auto", "full")
  val cfgClass     = Prop.get("japgolly.scalajs.react.config.class")

  private def failJsContains(substr: String, expect: Boolean): Nothing = {
    val err =
      if (expect)
        s"is supposed to contain [$substr] but it doesn't."
      else
        s"contains '$substr' but shouldn't."
    fail("JS output " + err)
  }

  // private def assertJsContains(substr: String, expect: Boolean = true)(implicit l: Line): Unit = {
  //   val actual = content.contains(substr)
  //   if (actual != expect)
  //     failJsContains(substr, expect)
  // }

  // private val strQuotes = "\"'`".toCharArray.map(_.toString)

  // private def assertJsContainsLiteral(substr: String, expect: Boolean = true)(implicit l: Line): Unit = {
  //   val actual = strQuotes.exists(q => content.contains(q + substr + q))
  //   if (actual != expect)
  //     failJsContains(substr, expect)
  // }

  private def assertLegalValue(str: String)(legal: String*)(implicit l: Line): Unit =
    if (!legal.contains(str))
      fail(s"Illegal value: '$str'. Legal values are: " + legal.sorted.mkString("", ", ", "."))

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

    "carrot" - {
      val t = if (cfgClass.isDefined) "custom" else compnameAll
      contentTest(t, "allow,blank,custom")(
        "allow"  -> "\"CarRot!\"",
        "custom" -> "\"CarRot!-MOD\"",
      )
    }

    "pumpkin" - {
      val t = if (cfgClass.isDefined) "custom" else if (compnameAll == "blank") "blank" else compnameAuto
      contentTest(t, "full,short,blank,custom")(
        "short"  -> "\"Pumpkin\"",
        "full"   -> "\"downstream.Pumpkin\"",
        "custom" -> "\"downstream.Pumpkin-AUTO-MOD\"",
        never    -> "automaticComponentName__T__T"
      )
    }

  }
}