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
  case class CtorTC__[A, U](apply: A => U) extends AnyVal

  @inline implicit class CtorOps_P[A, P, U](private val self: A)(implicit c: CtorTC_P[A, P, U]) {
    @inline def apply(props: P): U =
      c.apply(self, props)
  }
  @inline implicit class CtorOps__[A, U](private val self: A)(implicit c: CtorTC__[A, U]) {
    @inline def apply(): U =
      c.apply(self)
  }

  implicit def jsCtorTC_P[P <: js.Object, S <: js.Object, M](implicit ev: P =:!= Null): CtorTC_P[CompJs3X.Constructor[P, S, M], P, CompJs3X.Unmounted[P, S, M]] =
    CtorTC_P(_ applyDirect _)

  implicit def jsCtorTC__[S <: js.Object, M]: CtorTC__[CompJs3X.Constructor[Null, S, M], CompJs3X.Unmounted[Null, S, M]] =
    CtorTC__(_ applyDirect null)

  implicit def scalaCtorTC_P[P, S, B](implicit ev: P =:!= Unit): CtorTC_P[CompScala.Ctor[P, S, B], P, CompScala.Unmounted[P, S, B]] =
    CtorTC_P(_ applyDirect _)

  implicit def scalaCtorTC__[S, B]: CtorTC__[CompScala.Ctor[Unit, S, B], CompScala.Unmounted[Unit, S, B]] =
    CtorTC__(_.applyDirect(()))
}
