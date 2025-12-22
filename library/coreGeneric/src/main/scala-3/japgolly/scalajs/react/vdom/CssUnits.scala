package japgolly.scalajs.react.vdom

import japgolly.microlibs.compiletime.MacroEnv.*
import scala.language.`3.0`
import scala.quoted.*

object CssUnits {

  inline def addSuffix[N](inline n: N, inline suffix: String)(using inline ev: Numeric[N]): String =
    ${ combine('n, 'suffix) }

  private def combine(n: Expr[Any], suffix: Expr[String])(using Quotes): Expr[String] = {
    import quotes.reflect.*
    n.asTerm match {
      case Inlined(_, _, Literal(c)) =>
        val v = c.value
        val s = if v == 0 then "0" else v.toString + suffix.valueOrAbort
        Expr.inlineConst(s)

      case _ =>
        '{ combineAtRuntime(${n}.toString, $suffix) }
    }
  }

  def combineAtRuntime(n: String, suffix: String): String =
    if (n == "0") n else (n + suffix)
}

trait CssUnitsOps {
  import CssUnits.addSuffix

  extension [N](n: N)(using inline ev: Numeric[N]) {

    /**
     * Relative to the viewing device. For screen display, typically one device
     * pixel (dot) of the display.
     *
     * For printers and very high resolution screens one CSS pixel implies
     * multiple device pixels, so that the number of pixel per inch stays around
     * 96.
     */
    inline def px = addSuffix(n, "px")

    /** One point which is 1/72 of an inch. */
    inline def pt = addSuffix(n, "pt")

    /** One millimeter. */
    inline def mm = addSuffix(n, "mm")

    /** One centimeter 10 millimeters. */
    inline def cm = addSuffix(n, "cm")

    /** One inch 2.54 centimeters. */
    inline def in = addSuffix(n, "in")

    /** One pica which is 12 points. */
    inline def pc = addSuffix(n, "pc")

    /**
     * This unit represents the calculated font-size of the element. If used on
     * the font-size property itself, it represents the inherited font-size
     * of the element.
     */
    inline def em = addSuffix(n, "em")

    /**
     * This unit represents the width, or more precisely the advance measure, of
     * the glyph '0' zero, the Unicode character U+0030 in the element's font.
     */
    inline def ch = addSuffix(n, "ch")

    /**
     * This unit represents the x-height of the element's font. On fonts with the
     * 'x' letter, this is generally the height of lowercase letters in the font;
     * 1ex ≈ 0.5em in many fonts.
     */
    inline def ex = addSuffix(n, "ex")

    /**
     * This unit represents the font-size of the root element e.g. the font-size
     * of the html element. When used on the font-size on this root element,
     * it represents its initial value.
     */
    inline def rem = addSuffix(n, "rem")

    /**
     * An angle in degrees. One full circle is 360deg. E.g. 0deg, 90deg, 360deg.
     */
    inline def deg = addSuffix(n, "deg")

    /**
     * An angle in gradians. One full circle is 400grad. E.g. 0grad, 100grad,
     * 400grad.
     */
    inline def grad = addSuffix(n, "grad")

    /**
     * An angle in radians.  One full circle is 2π radians which approximates
     * to 6.2832rad. 1rad is 180/π degrees. E.g. 0rad, 1.0708rad, 6.2832rad.
     */
    inline def rad = addSuffix(n, "rad")

    /**
     * The number of turns the angle is. One full circle is 1turn. E.g. 0turn,
     * 0.25turn, 1turn.
     */
    inline def turn = addSuffix(n, "turn")

    /** A percentage value */
    inline def pct = addSuffix(n, "%")

    /** A percentage value */
    inline def `%` = addSuffix(n, "%")
  }
}
