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

  // ================================================
  // From Miles Sabin's "Shapeless" library.
  //
  //  type ¬[T] = T => Nothing
  //  type ¬¬[T] = ¬[¬[T]]
  //  type ∧[T, U] = T with U
  //  type ∨[T, U] = ¬[¬[T] ∧ ¬[U]]
  //
  //  // Type-lambda for context bound
  //  type |∨|[T, U] = {
  //    type λ[X] = ¬¬[X] <:< (T ∨ U)
  //  }

  // Type inequalities
  trait =:!=[A, B]

  def unexpected : Nothing = sys.error("Unexpected invocation")
  implicit def neq[A, B] : A =:!= B = new =:!=[A, B] {}
  implicit def neqAmbig1[A] : A =:!= A = unexpected
  implicit def neqAmbig2[A] : A =:!= A = unexpected
  // ================================================

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  // ======================
  // +Props, +Children
  // ======================

  case class CtorTC_PC[A, P, U](apply: (A, P, ChildrenArgSeq) => U) extends AnyVal

  @inline implicit class CtorOps_PC[A, P, U](private val self: A)(implicit c: CtorTC_PC[A, P, U]) {
    @inline def apply(props: P)(children: raw.ReactNodeList*): U =
      c.apply(self, props, children)
  }

  implicit def jsCtorTC_PC[P <: js.Object, S <: js.Object, M](implicit ev: P =:!= Null): CtorTC_PC[CompJs3X.Constructor[P, ChildrenArg.Varargs, S, M], P, CompJs3X.Unmounted[P, S, M]] =
    CtorTC_PC(_.applyDirect(_, _))

  // TODO missing Scala

  // ======================
  // +Props, -Children
  // ======================

  case class CtorTC_P[A, P, U](apply: (A, P) => U) extends AnyVal

  @inline implicit class CtorOps_P[A, P, U](private val self: A)(implicit c: CtorTC_P[A, P, U]) {
    @inline def apply(props: P): U =
      c.apply(self, props)
  }

  implicit def jsCtorTC_P[P <: js.Object, S <: js.Object, M](implicit ev: P =:!= Null): CtorTC_P[CompJs3X.Constructor[P, ChildrenArg.None, S, M], P, CompJs3X.Unmounted[P, S, M]] =
    CtorTC_P(_.applyDirect(_, EmptyChildrenArgSeq))

  implicit def scalaCtorTC_P[P, S, B](implicit ev: P =:!= Unit): CtorTC_P[CompScala.Ctor[P, S, B], P, CompScala.Unmounted[P, S, B]] =
    CtorTC_P(_.applyDirect(_, EmptyChildrenArgSeq))

  // ======================
  // -Props, +Children
  // ======================

  case class CtorTC__C[A, U](apply: (A, ChildrenArgSeq) => U) extends AnyVal

  @inline implicit class CtorOps__C[A, U](private val self: A)(implicit c: CtorTC__C[A, U]) {
    @inline def apply(children: raw.ReactNodeList*): U =
      c.apply(self, children)
  }

  implicit def jsCtorTC__C[S <: js.Object, M]: CtorTC__C[CompJs3X.Constructor[Null, ChildrenArg.Varargs, S, M], CompJs3X.Unmounted[Null, S, M]] =
    CtorTC__C(_.applyDirect(null, _))

  // TODO missing Scala

  // ======================
  // -Props, -Children
  // ======================

  case class CtorTC__[A, U](apply: A => U) extends AnyVal

  @inline implicit class CtorOps__[A, U](private val self: A)(implicit c: CtorTC__[A, U]) {
    @inline def apply(): U =
      c.apply(self)
  }

  implicit def jsCtorTC__[S <: js.Object, M]: CtorTC__[CompJs3X.Constructor[Null, ChildrenArg.None, S, M], CompJs3X.Unmounted[Null, S, M]] =
    CtorTC__(_.applyDirect(null, EmptyChildrenArgSeq))

  implicit def scalaCtorTC__[S, B]: CtorTC__[CompScala.Ctor[Unit, S, B], CompScala.Unmounted[Unit, S, B]] =
    CtorTC__(_.applyDirect((), EmptyChildrenArgSeq))

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
