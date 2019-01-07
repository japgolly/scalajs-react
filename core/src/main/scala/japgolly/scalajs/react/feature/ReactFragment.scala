package japgolly.scalajs.react.feature

import japgolly.scalajs.react.Key
import japgolly.scalajs.react.vdom._

object ReactFragment {

  /** Unlike [[VdomArray]],
    *
    * - This is immutable.
    * - Elements may, but needn't have keys.
    * - The result can be assigned a key.
    */
  def apply(ns: VdomNode*): VdomElement =
    complete(mkFrag(ns, None))

  /** Unlike [[VdomArray]],
    *
    * - This is immutable.
    * - Elements may, but needn't have keys.
    * - The result can be assigned a key.
    */
  def withKey(key: Key)(ns: VdomNode*): VdomElement =
    complete(mkFrag(ns, Some(key)))

  private def mkFrag(ns: Seq[VdomNode], key: Option[Key]): VdomFragment =
    if (ns.isEmpty)
      new VdomFragment(Vector.empty, key)
    else
      ns.reduce(_ ~ _) match {
        case f: VdomFragment if key.isEmpty => f
        case f: VdomFragment                => new VdomFragment(f.nodes, key)
        case n                              => new VdomFragment(Vector.empty[VdomNode] :+ n, key)
      }

  private def complete(f: VdomFragment): VdomElement =
    VdomElement(f.rawElement)
}
