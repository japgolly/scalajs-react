package japgolly.scalajs.react

import org.scalajs.dom

/*
  type Constructor_NoProps[S <: js.Object] = CompJs3X.Constructor_NoProps[S, Mounted[Null, S]]
  type Constructor[P <: js.Object, S <: js.Object] = CompJs3X.Constructor[P, S, Mounted[P, S]]
  type Unmounted  [P <: js.Object, S <: js.Object] = CompJs3X.Unmounted  [P, S, Mounted[P, S]]
  type Mounted    [P <: js.Object, S <: js.Object] = CompJs3X.Mounted    [P, S, raw.ReactComponent]


  case class Constructor[P <: js.Object, S <: js.Object, M](rawCls: raw.ReactClass)(m: raw.ReactComponent => M) {
  case class Constructor_NoProps[S <: js.Object, M](rawCls: raw.ReactClass)(m: raw.ReactComponent => M) {
  class Unmounted[P <: js.Object, S <: js.Object, M](val rawElement: raw.ReactComponentElement, m: raw.ReactComponent => M) {
  trait Mounted[P <: js.Object, S <: js.Object, Raw <: raw.ReactComponent] {


  case class Ctor[P, S, B](jsInstance: CompJs3.Constructor[Box[P], Box[S]]) {
  case class Ctor_NoProps[S, B](jsInstance: CompJs3.Constructor[Box[Unit], Box[S]]) {
  class Unmounted[P, S, B](jsInstance: CompJs3.Unmounted[Box[P], Box[S]]) {
  class Mounted[F[_], P, S, +Backend](jsInstance: CompJs3.Mounted[Box[P], Box[S]])

  type BackendScope[P, S] = Mounted[CallbackTo, P, S, Null]
*/

trait BaseUnmounted[P, S, M] {
  def key: Option[Key]
  def ref: Option[String]
  def props: P
  def propsChildren: raw.ReactNodeList
  def renderIntoDOM(container: raw.ReactDOM.Container, callback: Callback = Callback.empty): M

//  def mapMounted[MM](f: M => MM): BaseUnmounted[P, S, MM]
}

trait CompProps[F[_], P] {
  def props: F[P]
  def propsChildren: F[raw.ReactNodeList]
}

trait CompState[F[_], S] {
  def state: F[S]
  def setState(newState: S, callback: Callback = Callback.empty): F[Unit]
  def modState(mod: S => S, callback: Callback = Callback.empty): F[Unit]
}

trait BaseMounted[F[_], P, S] extends CompProps[F, P] with CompState[F, S] {
  def isMounted: F[Boolean]
  def getDOMNode: F[dom.Element]
  def forceUpdate(callback: Callback = Callback.empty): F[Unit]
}

/*
//trait MountedTC[M[_, _]] {
//  def isMounted [F[_], P, S](m: M[P, S])(implicit F: Effect[F]): F[Boolean]
//  def getDOMNode[F[_], P, S](m: M[P, S])(implicit F: Effect[F]): F[dom.Element]
//  def props     [F[_], P, S](m: M[P, S])(implicit F: Effect[F]): F[P]
//  def children  [F[_], P, S](m: M[P, S])(implicit F: Effect[F]): F[raw.ReactNodeList]
//  def state     [F[_], P, S](m: M[P, S])(implicit F: Effect[F]): F[S]
//  def setState  [F[_], P, S](m: M[P, S])(newState: S, callback: Callback = Callback.empty)(implicit F: Effect[F]): F[Unit]
//  def modState  [F[_], P, S](m: M[P, S])(mod: S => S, callback: Callback = Callback.empty)(implicit F: Effect[F]): F[Unit]
//}

abstract class MountedTCF[F[_], M[_, _]](protected val F: Effect[F]) {
//trait MountedTCF[F[_], M[_, _]] {
//  protected val F: Effect[F]
  def isMounted [P, S](m: M[P, S]): F[Boolean]
  def getDOMNode[P, S](m: M[P, S]): F[dom.Element]
  def props     [P, S](m: M[P, S]): F[P]
  def children  [P, S](m: M[P, S]): F[raw.ReactNodeList]
  def state     [P, S](m: M[P, S]): F[S]
  def setState  [P, S](m: M[P, S])(newState: S, callback: Callback = Callback.empty): F[Unit]
  def modState  [P, S](m: M[P, S])(mod: S => S, callback: Callback = Callback.empty): F[Unit]
}
*/
