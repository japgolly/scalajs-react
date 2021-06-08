package japgolly.scalajs.react

import scala.language.`3.0`
import scala.scalajs.js

opaque type PropsChildren = facade.PropsChildren

object PropsChildren {

  @inline def apply(c: facade.PropsChildren): PropsChildren =
    c

  def fromRawProps(p: js.Object): PropsChildren = {
    val pp = p.asInstanceOf[js.UndefOr[facade.PropsWithChildren]]
    apply(pp.map(_.children))
  }

  // ===================================================================================================================

  extension (self: PropsChildren) {

    @inline def raw: facade.PropsChildren =
      self

    /** Return the only child in children. Throws otherwise. */
    @inline def only(): facade.React.Node =
      facade.React.Children.only(self)

    /** Return the total number of components in children, equal to the number of times that a callback passed to map or forEach would be invoked. */
    @inline def count: Int =
      facade.React.Children.count(self)

    def isEmpty: Boolean =
      count == 0

    @inline def nonEmpty: Boolean =
      !isEmpty

    /** Return the children opaque data structure as a flat array with keys assigned to each child. Useful if you want to manipulate collections of children in your render methods, especially if you want to reorder or slice this.props.children before passing it down. */
    @inline def toJsArray: js.Array[facade.React.Node] =
      facade.React.Children.toArray(raw)

    def iterator: Iterator[facade.React.Node] =
      toJsArray.iterator

    @inline def toList: List[facade.React.Node] =
      iterator.toList

    @inline def toVector: Vector[facade.React.Node] =
      iterator.toVector
  }
}
