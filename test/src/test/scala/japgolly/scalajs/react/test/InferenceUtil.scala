package japgolly.scalajs.react.test

import scala.reflect.ClassTag
import scala.scalajs.js
import scalaz.{Monad, ~>}
import japgolly.scalajs.react._

object InferenceUtil {

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

  def testExpr[A](a: => A)(implicit t: ClassTag[A]) = new {
    override def toString = t.toString()
    def expect[C](implicit ev: A =:= C): Unit = ()
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

  @js.native trait JP extends js.Object
  @js.native trait JS extends js.Object
  @js.native trait JT extends js.Object

  def S: S = ???

//  @js.native trait N extends TopNode
//  val c = null.asInstanceOf[ReactComponentM[Unit, S, Unit, N]]
  val bs = null.asInstanceOf[BackendScope[P, S]]

  def st_s(s: S, t: T): S = ???

  type Render = ScalaComponent.Lifecycle.RenderScope[P, S, B]
  type Backend = BackendScope[P, S]
  type JsMounted = JsComponent.Mounted[JP, JS]
  type ScalaMountedId = ScalaComponent.MountedImpure[P, S, B]
  type ScalaMountedCB = ScalaComponent.MountedPure[P, S, B]
  type StateAccessP = StateAccessPure[S]
  type StateAccessI = StateAccessImpure[S]
}
