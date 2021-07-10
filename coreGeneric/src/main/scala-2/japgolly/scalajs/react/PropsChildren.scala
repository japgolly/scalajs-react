package japgolly.scalajs.react

import scala.scalajs.js

object PropsChildren {
  def apply(c: js.UndefOr[facade.PropsChildren]): PropsChildren =
    new PropsChildren(c.asInstanceOf[js.Any])

  def fromRawProps(p: js.Object): PropsChildren = {
    val pp = p.asInstanceOf[js.UndefOr[facade.PropsWithChildren]]
    apply(pp.map(_.children))
  }
}

final class PropsChildren private[PropsChildren](private val self: js.Any) extends AnyVal {
  def raw: facade.PropsChildren =
    self.asInstanceOf[facade.PropsChildren]

  override def toString: String =
    iterator.mkString("PropsChildren(", ", ", ")")

  /** Return the only child in children. Throws otherwise. */
  def only(): facade.React.Node =
    facade.React.Children.only(raw)

  /** Return the total number of components in children, equal to the number of times that a callback passed to map or forEach would be invoked. */
  def count: Int =
    facade.React.Children.count(raw)

  def isEmpty: Boolean =
    count == 0

  @inline def nonEmpty: Boolean =
    !isEmpty

  def iterator: Iterator[facade.React.Node] =
    toJsArray.iterator

  /** Return the children opaque data structure as a flat array with keys assigned to each child. Useful if you want to manipulate collections of children in your render methods, especially if you want to reorder or slice this.props.children before passing it down. */
  def toJsArray: js.Array[facade.React.Node] =
    facade.React.Children.toArray(raw)

  def toList: List[facade.React.Node] =
    iterator.toList

  def toVector: Vector[facade.React.Node] =
    iterator.toVector
}

