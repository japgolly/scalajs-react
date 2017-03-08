package japgolly.scalajs.react.vdom

/**
 * Extends numbers to provide a bunch of useful methods, allowing you to write
 * CSS-lengths in a nice syntax without resorting to strings.
 */
final class CssUnits private[vdom] (private val _n: Any) extends AnyVal {

  private def addSuffix(suffix: String): String = {
    val s = _n.toString
    if (s == "0") s else s + suffix
  }

  /**
   * Relative to the viewing device. For screen display, typically one device
   * pixel (dot) of the display.
   *
   * For printers and very high resolution screens one CSS pixel implies
   * multiple device pixels, so that the number of pixel per inch stays around
   * 96.
   */
  def px = addSuffix("px")

  /** One point which is 1/72 of an inch. */
  def pt = addSuffix("pt")

  /** One millimeter. */
  def mm = addSuffix("mm")

  /** One centimeter 10 millimeters. */
  def cm = addSuffix("cm")

  /** One inch 2.54 centimeters. */
  def in = addSuffix("in")

  /** One pica which is 12 points. */
  def pc = addSuffix("pc")

  /**
   * This unit represents the calculated font-size of the element. If used on
   * the font-size property itself, it represents the inherited font-size
   * of the element.
   */
  def em = addSuffix("em")

  /**
   * This unit represents the width, or more precisely the advance measure, of
   * the glyph '0' zero, the Unicode character U+0030 in the element's font.
   */
  def ch = addSuffix("ch")

  /**
   * This unit represents the x-height of the element's font. On fonts with the
   * 'x' letter, this is generally the height of lowercase letters in the font;
   * 1ex ≈ 0.5em in many fonts.
   */
  def ex = addSuffix("ex")

  /**
   * This unit represents the font-size of the root element e.g. the font-size
   * of the html element. When used on the font-size on this root element,
   * it represents its initial value.
   */
  def rem = addSuffix("rem")

  /**
   * An angle in degrees. One full circle is 360deg. E.g. 0deg, 90deg, 360deg.
   */
  def deg = addSuffix("deg")

  /**
   * An angle in gradians. One full circle is 400grad. E.g. 0grad, 100grad,
   * 400grad.
   */
  def grad = addSuffix("grad")

  /**
   * An angle in radians.  One full circle is 2π radians which approximates
   * to 6.2832rad. 1rad is 180/π degrees. E.g. 0rad, 1.0708rad, 6.2832rad.
   */
  def rad = addSuffix("rad")

  /**
   * The number of turns the angle is. One full circle is 1turn. E.g. 0turn,
   * 0.25turn, 1turn.
   */
  def turn = addSuffix("turn")

  /** A percentage value */
  def pct = addSuffix("%")

  /** A percentage value */
  def `%` = addSuffix("%")
}