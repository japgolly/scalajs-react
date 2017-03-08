package japgolly.scalajs.react.vdom

import scala.annotation.elidable

/**
 * Utility methods related to validating and escaping XML; used internally but
 * potentially useful outside of Scalatags.
 */
private[vdom] object Escaping {

  @elidable(elidable.ASSERTION)
  def assertValidTag(s: String): Unit =
    if (!s.matches("^[a-z][\\w0-9-]*$"))
      throw new IllegalArgumentException(s"Illegal tag name: $s is not a valid XML tag name")

  @elidable(elidable.ASSERTION)
  def assertValidAttrName(s: String): Unit =
    if (!s.matches("^[a-zA-Z_:][-a-zA-Z0-9_:.]*$"))
      throw new IllegalArgumentException(s"Illegal attribute name: $s is not a valid XML attribute name")
}
