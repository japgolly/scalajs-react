package japgolly.scalajs.react

import scalajs.js
import japgolly.scalajs.react.{raw => Raw}

object PropsChildren {
  def apply(c: js.UndefOr[Raw.PropsChildren]): PropsChildren =
    new PropsChildren(c.asInstanceOf[js.Any])

  def fromRawProps(p: js.Object): PropsChildren = {
    val pp = p.asInstanceOf[js.UndefOr[Raw.PropsWithChildren]]
    apply(pp.map(_.children))
  }
}

final class PropsChildren private[PropsChildren](private val self: js.Any) extends AnyVal {
  def raw: Raw.PropsChildren =
    self.asInstanceOf[Raw.PropsChildren]

  override def toString: String =
    iterator.mkString("PropsChildren(", ", ", ")")

//    /** Invoke fn on every immediate child contained within children with this set to context. If children is a nested object or array it will be traversed: fn will never be passed the container objects. If children is null or undefined returns null or undefined rather than an empty object. */
//    def map(c: PropsChildren, fn: MapFn): js.UndefOr[js.Object] = js.native
//
//    /** Like React.Children.map() but does not return an object. */
//    def forEach(c: PropsChildren, fn: MapFn): Unit = js.native

  /** Return the only child in children. Throws otherwise. */
  def only(): Raw.ReactNode =
    Raw.React.Children.only(raw)

  /** Return the total number of components in children, equal to the number of times that a callback passed to map or forEach would be invoked. */
  def count: Int =
    Raw.React.Children.count(raw)

  def isEmpty: Boolean =
    count == 0

  @inline def nonEmpty: Boolean =
    !isEmpty

  def iterator: Iterator[Raw.ReactNode] =
    toJsArray.iterator

  /** Return the children opaque data structure as a flat array with keys assigned to each child. Useful if you want to manipulate collections of children in your render methods, especially if you want to reorder or slice this.props.children before passing it down. */
  def toJsArray: js.Array[Raw.ReactNode] =
    Raw.React.Children.toArray(raw)

  def toSeq: Seq[Raw.ReactNode] =
    toJsArray
}

