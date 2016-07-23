package japgolly.scalajs.react.vdom

import scala.annotation.elidable

/**
 * Utility methods related to validating and escaping XML; used internally but
 * potentially useful outside of Scalatags.
 */
private[vdom] object Escaping {

  private[this] lazy val tagRegex = "^[a-z][\\w0-9-]*$".r.pattern

  /**
   * Uses a regex to check if something is a valid tag name.
   */
  private def validTag(s: String) = tagRegex.matcher(s).matches()

  @elidable(elidable.ASSERTION)
  def assertValidTag(s: String): Unit =
    if (!validTag(s))
      throw new IllegalArgumentException(s"Illegal tag name: $s is not a valid XML tag name")

  private[this] lazy val attrNameRegex = "^[a-zA-Z_:][-a-zA-Z0-9_:.]*$".r.pattern

  /**
   * Uses a regex to check if something is a valid attribute name.
   */
  private def validAttrName(s: String) = attrNameRegex.matcher(s).matches()

  @elidable(elidable.ASSERTION)
  def assertValidAttrName(s: String): Unit =
    if (!validAttrName(s))
      throw new IllegalArgumentException(s"Illegal attribute name: $s is not a valid XML attribute name")
}
