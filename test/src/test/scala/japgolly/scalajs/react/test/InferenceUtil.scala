package japgolly.scalajs.react.test

import japgolly.scalajs.react._
import scala.annotation.nowarn
import scala.scalajs.js

@nowarn("cat=unused")
object InferenceUtil {

  def assertType[A] = new TestDsl[A]
  def assertTypeOf[A](a: => A) = assertType[A]

  class TestDsl[A] {
    def apply[B](f: A => B) = assertType[B]
    def is  [B](implicit ev: A =:= B): Unit = ()
    def is_<[B](implicit ev: A <:< B): Unit = ()
    def is_>[B](implicit ev: B <:< A): Unit = ()
    def isImplicitly[B](implicit ev: A => B): Unit = ()
  }

  trait Big
  trait Medium extends Big
  trait Small extends Medium

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
