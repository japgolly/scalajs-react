package japgolly.scalajs.react

import japgolly.scalajs.react.{raw => Raw}
import scala.language.`3.0`
import scala.scalajs.js

opaque type PropsChildren = Raw.PropsChildren

object PropsChildren {

  @inline def apply(c: Raw.PropsChildren): PropsChildren =
    c

  def fromRawProps(p: js.Object): PropsChildren = {
    val pp = p.asInstanceOf[js.UndefOr[Raw.PropsWithChildren]]
    apply(pp.map(_.children))
  }

  // ===================================================================================================================

  extension (self: PropsChildren) {

    @inline def raw: Raw.PropsChildren =
      self

    /** Return the only child in children. Throws otherwise. */
    @inline def only(): Raw.React.Node =
      Raw.React.Children.only(self)

    /** Return the total number of components in children, equal to the number of times that a callback passed to map or forEach would be invoked. */
    @inline def count: Int =
      Raw.React.Children.count(self)

    def isEmpty: Boolean =
      count == 0

    @inline def nonEmpty: Boolean =
      !isEmpty

    /** Return the children opaque data structure as a flat array with keys assigned to each child. Useful if you want to manipulate collections of children in your render methods, especially if you want to reorder or slice this.props.children before passing it down. */
    @inline def toJsArray: js.Array[Raw.React.Node] =
      Raw.React.Children.toArray(raw)

    def iterator: Iterator[Raw.React.Node] =
      toJsArray.iterator

    @inline def toList: List[Raw.React.Node] =
      iterator.toList

    @inline def toVector: Vector[Raw.React.Node] =
      iterator.toVector
  }
}
