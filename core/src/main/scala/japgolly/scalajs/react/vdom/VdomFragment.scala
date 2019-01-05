package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{raw => Raw}
import scala.scalajs.js

/** Unlike [[VdomArray]],
  *
  * - This is immutable.
  * - Elements may, but needn't have keys.
  * - The result can be assigned a key.
  */
final class VdomFragment(val nodes: Vector[VdomNode], val key: Option[Raw.React.Key]) extends VdomElement {

  override lazy val rawElement: Raw.React.Element = {
    val props: js.Object =
      key match {
        case Some(jsKey) => js.Dynamic.literal("key" -> jsKey.asInstanceOf[js.Any])
        case None        => null
      }
    Raw.React.createElement(Raw.React.Fragment, props, nodes.map(_.rawNode): _*)
  }

  override def ~(next: VdomNode): VdomFragment =
    next match {

      case f: VdomFragment =>
        val newNodes = nodes ++ f.nodes
        val newKey   = f.key orElse key
        new VdomFragment(newNodes, newKey)

      case _ =>
        val newNodes = nodes :+ next
        new VdomFragment(newNodes, key)
    }
}

object VdomFragment {

  // This is designed to be called from VdomNode~ from only
  // left is assumed to not be a VdomFragment
  @inline private[vdom] def two(left: VdomNode, right: VdomNode): VdomFragment =
    right match {

      case f: VdomFragment =>
        val newNodes = left +: f.nodes
        new VdomFragment(newNodes, f.key)

      case _ =>
        val newNodes = Vector.empty[VdomNode] :+ left :+ right
        new VdomFragment(newNodes, None)
    }
}