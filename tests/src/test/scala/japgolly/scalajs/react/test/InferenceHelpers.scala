package japgolly.scalajs.react.test

import japgolly.scalajs.react._
import scala.scalajs.js

object InferenceHelpers {

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

  type Render = ScalaComponent.Lifecycle.RenderScope[CallbackTo, AsyncCallback, P, S, B]
  type Backend = BackendScope[P, S]
  type JsMounted = JsComponent.Mounted[JP, JS]
  type ScalaMountedId = ScalaComponent.MountedImpure[P, S, B]
  type ScalaMountedCB = ScalaComponent.MountedPure[P, S, B]
  type StateAccessP = StateAccessPure[S]
  type StateAccessI = StateAccessImpure[S]
}
