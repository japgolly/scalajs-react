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

  case class CtorTC_P[A, P, U](apply: (A, P) => U) extends AnyVal
  @inline implicit class CtorOps_P[A, P, U](private val self: A)(implicit c: CtorTC_P[A, P, U]) {
    @inline def apply(props: P): U =
      c.apply(self, props)
  }

  case class CtorTC__[A, U](apply: A => U) extends AnyVal
  @inline implicit class CtorOps__[A, U](private val self: A)(implicit c: CtorTC__[A, U]) {
    @inline def apply(): U =
      c.apply(self)
  }

//  C <: ChildrenArg

  private val emptyChildrenArgSeq: ChildrenArgSeq =
    Seq.empty

  implicit def jsCtorTC_P[P <: js.Object, S <: js.Object, M](implicit ev: P =:!= Null): CtorTC_P[CompJs3X.Constructor[P, ChildrenArg.None, S, M], P, CompJs3X.Unmounted[P, S, M]] =
    CtorTC_P(_.applyDirect(_, emptyChildrenArgSeq))

  implicit def jsCtorTC__[S <: js.Object, M]: CtorTC__[CompJs3X.Constructor[Null, ChildrenArg.None, S, M], CompJs3X.Unmounted[Null, S, M]] =
    CtorTC__(_.applyDirect(null, emptyChildrenArgSeq))

  implicit def scalaCtorTC_P[P, S, B](implicit ev: P =:!= Unit): CtorTC_P[CompScala.Ctor[P, S, B], P, CompScala.Unmounted[P, S, B]] =
    CtorTC_P(_.applyDirect(_, emptyChildrenArgSeq))

  implicit def scalaCtorTC__[S, B]: CtorTC__[CompScala.Ctor[Unit, S, B], CompScala.Unmounted[Unit, S, B]] =
    CtorTC__(_.applyDirect((), emptyChildrenArgSeq))

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  type ChildrenArgSeq = Seq[raw.ReactNodeList]

  case class CtorTC_PC[A, P, U](apply: (A, P, ChildrenArgSeq) => U) extends AnyVal
  @inline implicit class CtorOps_PC[A, P, U](private val self: A)(implicit c: CtorTC_PC[A, P, U]) {
    @inline def apply(props: P)(children: raw.ReactNodeList*): U =
      c.apply(self, props, children)
  }
  implicit def jsCtorTC_PC[P <: js.Object, S <: js.Object, M](implicit ev: P =:!= Null): CtorTC_PC[CompJs3X.Constructor[P, ChildrenArg.Varargs, S, M], P, CompJs3X.Unmounted[P, S, M]] =
    CtorTC_PC(_.applyDirect(_, _))

  case class CtorTC__C[A, U](apply: (A, ChildrenArgSeq) => U) extends AnyVal
  @inline implicit class CtorOps__C[A, U](private val self: A)(implicit c: CtorTC__C[A, U]) {
    @inline def apply(children: raw.ReactNodeList*): U =
      c.apply(self, children)
  }
  implicit def jsCtorTC__C[S <: js.Object, M]: CtorTC__C[CompJs3X.Constructor[Null, ChildrenArg.Varargs, S, M], CompJs3X.Unmounted[Null, S, M]] =
    CtorTC__C(_.applyDirect(null, _))

  sealed trait ChildrenArg
  object ChildrenArg {
    sealed trait None       extends ChildrenArg
    sealed trait Varargs    extends ChildrenArg
    // sealed trait ZeroOrMore extends ChildrenArg
    // sealed trait One        extends ChildrenArg
    // sealed trait OneOrMore  extends ChildrenArg
  }

  import CompJs3X.DirectCtor
  import CompJs3X.Unmounted

//  class JsCtor2[P <: js.Object, C <: ChildrenArg, S <: js.Object, M](val rawCls: raw.ReactClass,
//                                                                      val directCtor: DirectCtor[P, raw.ReactComponentElement],
//                                                                      wrapMount: raw.ReactComponent => M) {
//
////    def mapMounted[MM](f: M => MM): Constructor[P, S, MM] =
////      new Constructor(rawCls, directCtor, f compose wrapMount)
//
//    val directCtorU: DirectCtor[P, Unmounted[P, S, M]] =
//      directCtor.rmap(new Unmounted[P, S, M](_, wrapMount))
//
//    val applyDirect: P => Unmounted[P, S, M] =
//      directCtorU(rawCls)
//  }

}
