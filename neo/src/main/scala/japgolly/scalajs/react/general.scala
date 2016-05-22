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
  def mapProps[X](f: P => X): CompProps[F, X]
}

trait CompState[F[_], S] {
  def state: F[S]
  def setState(newState: S, callback: Callback = Callback.empty): F[Unit]
  def modState(mod: S => S, callback: Callback = Callback.empty): F[Unit]
  def zoomState[X](get: S => X)(set: (S, X) => S): CompState[F, X]
}

trait MountedBase[F[_], P, S] extends CompProps[F, P] with CompState[F, S] {
  protected implicit def F: Effect[F]

  def isMounted: F[Boolean]
  def getDOMNode: F[dom.Element]
  def forceUpdate(callback: Callback = Callback.empty): F[Unit]

  override def mapProps[X](f: P => X): MountedBase[F, X, S] =
    new MountedBase.MorphedP[F, P, X, S](this, f)

  override def zoomState[X](get: S => X)(set: (S, X) => S): MountedBase[F, P, X] =
    new MountedBase.MorphedS[F, P, S, X](this, get, set)
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

object MountedBase {

  //////////////////////////////////////////////////
  // Delegates                                    //
  //////////////////////////////////////////////////

  trait DelegateG[F[_], P, S] extends MountedBase[F, P, S] {
    protected val delegate: MountedBase[F, _, _]
    override def isMounted                = delegate.isMounted
    override def getDOMNode               = delegate.getDOMNode
    override def forceUpdate(c: Callback) = delegate.forceUpdate(c)
  }

  trait DelegateP[F[_], P, S] extends MountedBase[F, P, S] {
    protected val delegate: MountedBase[F, P, _]
    override def props         = delegate.props
    override def propsChildren = delegate.propsChildren
  }

  trait DelegateS[F[_], P, S] extends MountedBase[F, P, S] {
    protected val delegate: MountedBase[F, _, S]
    override def state                            = delegate.state
    override def modState(f: S => S, c: Callback) = delegate.modState(f, c)
    override def setState(s: S, c: Callback)      = delegate.setState(s, c)
  }

  //////////////////////////////////////////////////
  // Morphisms                                    //
  //////////////////////////////////////////////////

  trait MorphP[F[_], P0, P, S] extends MountedBase[F, P, S] {
    protected val delegate: MountedBase[F, P0, _]
    protected val pMap: P0 => P
    override def props         = F.map(delegate.props)(pMap)
    override def propsChildren = delegate.propsChildren
  }

  trait MorphS[F[_], P, S0, S] extends MountedBase[F, P, S] {
    protected val delegate: MountedBase[F, _, S0]
    protected val sGet: S0 => S
    protected val sSet: (S0, S) => S0
    protected def sMod(s0: S0, f: S => S): S0     = sSet(s0, f(sGet(s0)))
    override def state                            = F.map(delegate.state)(sGet)
    override def modState(f: S => S, c: Callback) = delegate.modState(sMod(_, f), c)
    override def setState(s: S, c: Callback)      = delegate.modState(sSet(_, s), c)
  }

  class MorphedP[F[_], P0, P, S](protected val delegate: MountedBase[F, P0, S],
                                 protected val pMap: P0 => P)
                                (implicit protected val F: Effect[F])
    extends DelegateG[F, P, S] with MorphP[F, P0, P, S] with DelegateS[F, P, S] {
    override def mapProps[X](f: P => X) =
      new MorphedP[F, P0, X, S](delegate, f compose pMap)
    override def zoomState[X](get: S => X)(set: (S, X) => S) =
      new MorphedPS[F, P0, P, S, X](delegate, pMap, get, set)
  }

  class MorphedS[F[_], P, S0, S](protected val delegate: MountedBase[F, P, S0],
                                 protected val sGet: S0 => S,
                                 protected val sSet: (S0, S) => S0)
                                (implicit protected val F: Effect[F])
    extends DelegateG[F, P, S] with DelegateP[F, P, S] with MorphS[F, P, S0, S] {
    override def mapProps[X](f: P => X) =
      new MorphedPS[F, P, X, S0, S](delegate, f, sGet, sSet)
    override def zoomState[X](get: S => X)(set: (S, X) => S) =
      new MorphedS[F, P, S0, X](delegate, get compose sGet, (s0, x) => sMod(s0, set(_, x)))
  }

  class MorphedPS[F[_], P0, P, S0, S](protected val delegate: MountedBase[F, P0, S0],
                                      protected val pMap: P0 => P,
                                      protected val sGet: S0 => S,
                                      protected val sSet: (S0, S) => S0)
                                     (implicit protected val F: Effect[F])
    extends DelegateG[F, P, S] with MorphP[F, P0, P, S] with MorphS[F, P, S0, S] {
    override def mapProps[X](f: P => X) =
      new MorphedPS[F, P0, X, S0, S](delegate, f compose pMap, sGet, sSet)
    override def zoomState[X](get: S => X)(set: (S, X) => S) =
      new MorphedPS[F, P0, P, S0, X](delegate, pMap, get compose sGet, (s0, x) => sMod(s0, set(_, x)))
  }


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
