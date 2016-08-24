package japgolly.scalajs.react.test

import japgolly.scalajs.react._

object InferenceUtil {
//  import scalaz.{Monad, ~>}

  def test[A] = new {
    def apply[B](f: A => B) = new {
      def expect[C](implicit ev: B =:= C): Unit = ()
      def expect_<[C](implicit ev: B <:< C): Unit = ()
      def expect_>[C](implicit ev: C <:< B): Unit = ()
    }
    def expect_<[C](implicit ev: A <:< C): Unit = ()
    def expect_>[C](implicit ev: C <:< A): Unit = ()
    def usableAs[B](implicit ev: A => B): Unit = ()
  }

  trait Big
  trait Medium <: Big
  trait Small <: Medium

  trait M[A]
  trait P
  trait S
  trait T
  trait A
  trait B
  type U = Unit
//  @js.native trait N extends TopNode
//  val c = null.asInstanceOf[ReactComponentM[Unit, S, Unit, N]]
  val bs = null.asInstanceOf[BackendScope[P, S]]

  def st_s(s: S, t: T): S = ???

//  implicit val mMonad = null.asInstanceOf[Monad[M] with (M ~> CallbackTo)]

}
