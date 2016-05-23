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
  }

  sealed trait NoSingletonFor[A]
  @inline implicit def noSingletonFor[A]: NoSingletonFor[A] = null
  implicit def singletonFor1[A: Singleton]: NoSingletonFor[A] = null
  implicit def singletonFor2[A: Singleton]: NoSingletonFor[A] = null

  @inline implicit final class BaseCtorOps__[P, U](private val self: BaseCtor[P, ChildrenArg.None, U])(implicit p: Singleton[P]) {
    @inline def apply(): U =
      self.applyDirect(p.value, EmptyChildrenArgSeq)
  }

  @inline implicit final class BaseCtorOpsP_[P, U](private val self: BaseCtor[P, ChildrenArg.None, U])(implicit ev: NoSingletonFor[P]) {
    @inline def apply(props: P): U =
      self.applyDirect(props, EmptyChildrenArgSeq)
  }

  @inline implicit final class BaseCtorOps_C[P, U](private val self: BaseCtor[P, ChildrenArg.Varargs, U])(implicit p: Singleton[P]) {
    @inline def apply(children: raw.ReactNodeList*): U =
      self.applyDirect(p.value, children)
  }

  @inline implicit final class BaseCtorOpsPC[P, U](private val self: BaseCtor[P, ChildrenArg.Varargs, U])(implicit ev: NoSingletonFor[P]) {
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

    type Init[P, C <: ChildrenArg] = DirectCtor[P, C, raw.ReactComponentElement]

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
