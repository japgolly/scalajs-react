package downstream

import japgolly.microlibs.testutil.TestUtil._
import scala.Console._
import scala.io.Source
import utest._
import java.io.File
import sourcecode.Line

object JsFileTest extends TestSuite {

  private object Prop {
    private def get(property: String): Option[String] = {
      val o = Option(System.getProperty(property))
      println(s"$CYAN$property$RESET = $YELLOW${o.getOrElse("")}$RESET")
      o
    }

    def get(property: String, default: String): String =
      get(property).getOrElse(default)

    def need(property: String): String =
      get(property).getOrElse(throw new RuntimeException("Property not defined: " + property))
  }

  lazy val compnameAuto = Prop.get("japgolly.scalajs.react.compname.auto", "full")

  lazy val content: String = {
    val path = Prop.need("js_file")
    val s = Source.fromFile(new File(path))
    try s.mkString finally s.close()
  }

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

  private val strQuotes = "\"'`".toCharArray.map(_.toString)

  private def assertJsContainsLiteral(substr: String, expect: Boolean = true)(implicit l: Line): Unit = {
    val actual = strQuotes.exists(q => content.contains(q + substr + q))
    if (actual != expect)
      failJsContains(substr, expect)
  }

  private def assertLegalValue(str: String)(legal: String*)(implicit l: Line): Unit =
    if (!legal.contains(str))
      fail(s"Illegal value: '$str'. Legal values are: " + legal.sorted.mkString("", ", ", "."))

  private def contentTest(prop: String, legalValuesByComma: String)(propValueToSubstr: (String, String)*)(implicit l: Line): Unit = {
    assertLegalValue(prop)(legalValuesByComma.split(','): _*)
    var errors = List.empty[String]
    for ((pv, substr) <- propValueToSubstr) {
      val expect = prop == pv
      val actual = strQuotes.exists(q => content.contains(q + substr + q))
      val pass   = actual == expect
      val result = if (pass) s"${GREEN}pass$RESET" else s"${RED_B}${WHITE}FAIL$RESET"
      val should = if (expect) "should" else "shouldn't"
      println(s"  [$result] JS $should contain $MAGENTA'$substr'$RESET")
      if (!pass) errors ::= s"JS $should contain '$substr'"
    }
    if (errors.nonEmpty)
      fail(errors.sorted.mkString(", "))
  }

  override def tests = Tests {
    content // load here first

    "compnameAuto" -
      contentTest(compnameAuto, "full,short,blank")(
        "short" -> "Pumpkin",
        "full"  -> "downstream.Pumpkin",
      )
  }
}