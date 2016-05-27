package japgolly.scalajs

import scalajs.js
import scalajs.js.|

/*
Bad approaches
==============
* Building Types via conjunction - too hard to map
* JS + implicit ops - extern JS types can't be changed
* PSBN = annoying. PS usually enough.


[ ] Prevent certain lifecycle methods being called in certain scopes.
[ ] Make easy to add functionality (such as Id/CallbackTo, S zoom, P map).
[ ] All components: Id/Callback.
[ ] All components: S zoom.
[ ] All components: P map.
[ ] Typify PropsChildren.
[ ] Easily facade JS components.
[ ] Easily facade JS ES6 components.
[ ] Create ES6 components in Scala.
*/

package object react {

  type Callback = CallbackTo[Unit]

  type Key = String | Boolean | raw.JsNumber

  def orNullToOption[A](an: A | Null): Option[A] =
    Option(an.asInstanceOf[A])

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

  sealed trait ChildrenArg
  object ChildrenArg {
    sealed trait None       extends ChildrenArg
    sealed trait Varargs    extends ChildrenArg
  }

  type ChildrenArgSeq = Seq[raw.ReactNodeList]

  private val EmptyChildrenArgSeq: ChildrenArgSeq =
    Seq.empty

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  final case class Singleton[A](value: A) extends AnyVal
  object Singleton {
    implicit val Null = Singleton[Null](null)
    implicit val Unit = Singleton(())
    implicit val BoxUnit = Singleton(Box.Unit)

    sealed trait Not[A]
    @inline implicit def noSingletonFor[A]: Not[A] = null
    implicit def singletonFor1[A: Singleton]: Not[A] = null
    implicit def singletonFor2[A: Singleton]: Not[A] = null
  }

  @inline implicit final class BaseCtorOps__[P, U](private val self: BaseCtor[P, ChildrenArg.None, U])(implicit p: Singleton[P]) {
    @inline def apply(): U =
      self.applyDirect(p.value, EmptyChildrenArgSeq)
  }

  @inline implicit final class BaseCtorOpsP_[P, U](private val self: BaseCtor[P, ChildrenArg.None, U])(implicit ev: Singleton.Not[P]) {
    @inline def apply(props: P): U =
      self.applyDirect(props, EmptyChildrenArgSeq)
  }

  @inline implicit final class BaseCtorOps_C[P, U](private val self: BaseCtor[P, ChildrenArg.Varargs, U])(implicit p: Singleton[P]) {
    @inline def apply(children: raw.ReactNodeList*): U =
      self.applyDirect(p.value, children)
  }

  @inline implicit final class BaseCtorOpsPC[P, U](private val self: BaseCtor[P, ChildrenArg.Varargs, U])(implicit ev: Singleton.Not[P]) {
    @inline def apply(props: P)(children: raw.ReactNodeList*): U =
      self.applyDirect(props, children)
  }

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  abstract class DirectCtor[P, C <: ChildrenArg, O] {
    def apply(cls: raw.ReactClass): (P, ChildrenArgSeq) => O

    def lmap[X](f: X => P): DirectCtor[X, C, O]
    def rmap[X](f: O => X): DirectCtor[P, C, X]

    def dimap[X, Y](f: X => P)(g: O => Y): DirectCtor[X, C, Y] =
      lmap(f).rmap(g)
  }

  object DirectCtor extends DirectCtor_LowPri {
    def apply[P, C <: ChildrenArg, O](run: (raw.ReactClass, P, ChildrenArgSeq) => O): DirectCtor[P, C, O] =
      new DirectCtor[P, C, O] {
        override def apply(c: raw.ReactClass) = run(c, _, _)
        override def lmap[X](f: X => P)       = DirectCtor[X, C, O]((rc, x, c) => run(rc, f(x), c))
        override def rmap[X](f: O => X)       = DirectCtor[P, C, X]((rc, p, c) => f(run(rc, p, c)))
      }

    def const[P, O](run: raw.ReactClass => O): DirectCtor[P, ChildrenArg.None, O] =
      new DirectCtor[P, ChildrenArg.None, O] {
        override def apply(c: raw.ReactClass) = {val i = run(c); (_, _) => i}
        override def lmap[X](f: X => P)       = const[X, O](run)
        override def rmap[X](f: O => X)       = const[P, X](f compose run)
        override def toString                 = "DirectCtor.const"
      }

    type Init[P <: js.Object, C <: ChildrenArg] = DirectCtor[P, C, raw.ReactComponentElement]

    def constProps[P <: js.Object](props: P): Init[P, ChildrenArg.None] =
      const(raw.React.createElement(_, props))

    implicit val PropsNull: Init[Null, ChildrenArg.None] =
      constProps(null)

    implicit val PropsBoxUnit: Init[Box[Unit], ChildrenArg.None] =
      constProps(Box.Unit)
  }

  trait DirectCtor_LowPri {
    import DirectCtor.Init

    // Scala's contravariant implicit search is stupid
    private val AskPropsInstance: Init[js.Object, ChildrenArg.None] =
      DirectCtor((rc, p, _) => raw.React.createElement(rc, p))

    implicit def askProps[P <: js.Object]: Init[P, ChildrenArg.None] =
      AskPropsInstance.asInstanceOf[Init[P, ChildrenArg.None]]

    // Scala's contravariant implicit search is stupid
    private val AskPropsCInstance: Init[js.Object, ChildrenArg.Varargs] =
      DirectCtor((rc, p, c) => raw.React.createElement(rc, p, c: _*))

    implicit def askPropsC[P <: js.Object]: Init[P, ChildrenArg.Varargs] =
      AskPropsCInstance.asInstanceOf[Init[P, ChildrenArg.Varargs]]
  }


}
