package japgolly.scalajs.react

import scalajs.js

object PropsChildren {
  @inline def apply(c: raw.PropsChildren): PropsChildren =
    new PropsChildren(c.asInstanceOf[js.Any])
}

final class PropsChildren private[PropsChildren](private val self: js.Any) extends AnyVal {
  @inline def rawChildren: raw.PropsChildren =
    self.asInstanceOf[raw.PropsChildren]

//    /** Invoke fn on every immediate child contained within children with this set to context. If children is a nested object or array it will be traversed: fn will never be passed the container objects. If children is null or undefined returns null or undefined rather than an empty object. */
//    def map(c: PropsChildren, fn: MapFn): js.UndefOr[js.Object] = js.native
//
//    /** Like React.Children.map() but does not return an object. */
//    def forEach(c: PropsChildren, fn: MapFn): Unit = js.native

  /** Return the only child in children. Throws otherwise. */
  @inline def only_! : raw.ReactNode =
    raw.React.Children.only(rawChildren)

  /** Return the total number of components in children, equal to the number of times that a callback passed to map or forEach would be invoked. */
  @inline def count: Int =
    raw.React.Children.count(rawChildren)

  def isEmpty: Boolean =
    count == 0

  @inline def nonEmpty: Boolean =
    !isEmpty

  /** Return the children opaque data structure as a flat array with keys assigned to each child. Useful if you want to manipulate collections of children in your render methods, especially if you want to reorder or slice this.props.children before passing it down. */
  @inline def toJsArray: js.Array[raw.ReactNode] =
    raw.React.Children.toArray(rawChildren)

  def toSeq: Seq[raw.ReactNode] =
    toJsArray
}

