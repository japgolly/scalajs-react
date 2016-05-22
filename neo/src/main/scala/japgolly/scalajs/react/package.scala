package japgolly.scalajs

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
}
