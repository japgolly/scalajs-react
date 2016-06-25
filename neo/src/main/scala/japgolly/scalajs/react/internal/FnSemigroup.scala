package japgolly.scalajs.react.internal

import japgolly.scalajs.react.Callback

import scala.scalajs.js

case class Semigroup[A](append: (A, => A) => A) extends AnyVal {

//  def appendF[I](f: I => A, g: I => A): I => A =
//    a => append(f(a), g(a))
//
//  def appendUF[I](uf: js.UndefOr[I => A], g: I => A): I => A =
//    uf.fold(g)(appendF(_, g))
//
//  def appendOF[I](uf: Option[I => A], g: I => A): I => A =
//    uf.fold(g)(appendF(_, g))

}

object Semigroup {
//  implicit val unit     = Semigroup[Unit    ]((_, b) => b)
//  implicit val callback = Semigroup[Callback](_ >> _)
           val either   = Semigroup[Boolean ](_ || _)
}

//abstract class FnSemigroup[B] {
//  def append[A](f: A => B, g: A => B): A => B
//  def append[A](f: A => B, g: A => B): A => B
//}

object FnSemigroup {

//  final class Ops[A, B](private val f: A => B) extends AnyVal {
//    @inline def |+|(g: A => B)(implicit s: FnSemigroup[B]): A => B =
//      s.append(f, g)
//  }
//
//  @inline implicit def Ops[A, B](f: A => B)(implicit unusedHereNicerForIDEs: FnSemigroup[B]): Ops[A, B] =
//    new Ops(f)

  // ===================================================================================================================

//  implicit object UnitInstance extends FnSemigroup[Unit] {
//    override def append[A](f: A => Unit, g: A => Unit): A => Unit =
//      a => {f(a); g(a)}
//  }
//
//  implicit object CallbackInstance extends FnSemigroup[Callback] {
//    override def append[A](f: A => Callback, g: A => Callback): A => Callback =
//      a => f(a) >> g(a)
//  }

//  def merge[A, B](f: A => B, g: A => B)(implicit s: Semigroup[B]): A => B =
//    a => s.append(f(a), g(a))

}

/*
  final class FnComposer[R](compose: (=> R, => R) => R) {
    def apply[A](uf: UndefOr[A => R], g: A => R) =
      uf.fold(g)(f => a => compose(f(a), g(a)))

    def apply[A, B](uf: UndefOr[(A, B) => R], g: (A, B) => R) =
      uf.fold(g)(f => (a, b) => compose(f(a, b), g(a, b)))

    def apply[A, B, C](uf: UndefOr[(A, B, C) => R], g: (A, B, C) => R) =
      uf.fold(g)(f => (a, b, c) => compose(f(a, b, c), g(a, b, c)))
  }

  val fcUnit   = new FnComposer[Callback]           (_ >> _)
  val fcEither = new FnComposer[CallbackTo[Boolean]](_ || _)
*/