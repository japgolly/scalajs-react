package japgolly.scalajs.react

import org.scalajs.dom
import japgolly.scalajs.react.internal._

// TODO Make Unmounted <: Component.Unmounted
trait Component[P, CT[_, _] <: CtorType[_, _], Unmounted] {
  val ctor: CT[P, Unmounted]

  // TODO Morph P
  // TODO Morph CT
  // TODO Morph U
}

object Component {

  trait Unmounted[P, Mounted] {
    def key: Option[Key]
    def ref: Option[String]
    def props: P
    def propsChildren: PropsChildren
    def renderIntoDOM(container: raw.ReactDOM.Container, callback: Callback = Callback.empty): Mounted
    //  def mapMounted[MM](f: M => MM): BaseUnmounted[P, S, MM]

    // TODO Morph P
    // TODO Morph S
    // TODO Morph M
  }

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  trait Props[F[_], P] {
    def props: F[P]
    def propsChildren: F[PropsChildren]
    def mapProps[X](f: P => X): Props[F, X]
    // TODO Morph F
  }

  trait State[F[_], S] {
    def state: F[S]
    def setState(newState: S, callback: Callback = Callback.empty): F[Unit]
    def modState(mod: S => S, callback: Callback = Callback.empty): F[Unit]
    def zoomState[X](get: S => X)(set: (S, X) => S): State[F, X]
    // TODO Morph F
  }

  trait Mounted[F[_], P, S] extends Props[F, P] with State[F, S] {
    protected implicit def F: Effect[F]

    def isMounted: F[Boolean]
    def getDOMNode: F[dom.Element]
    def forceUpdate(callback: Callback = Callback.empty): F[Unit]

    override def mapProps[X](f: P => X): Mounted[F, X, S] =
      new Mounted.MorphedP[F, P, X, S](this, f)

    override def zoomState[X](get: S => X)(set: (S, X) => S): Mounted[F, P, X] =
      new Mounted.MorphedS[F, P, S, X](this, get, set)

    // TODO Morph F
  }

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  object Mounted {

    //////////////////////////////////////////////////
    // Delegates                                    //
    //////////////////////////////////////////////////

    trait DelegateG[F[_], P, S] extends Mounted[F, P, S] {
      protected val delegate: Mounted[F, _, _]
      override def isMounted                = delegate.isMounted
      override def getDOMNode               = delegate.getDOMNode
      override def forceUpdate(c: Callback) = delegate.forceUpdate(c)
    }

    trait DelegateP[F[_], P, S] extends Mounted[F, P, S] {
      protected val delegate: Mounted[F, P, _]
      override def props         = delegate.props
      override def propsChildren = delegate.propsChildren
    }

    trait DelegateS[F[_], P, S] extends Mounted[F, P, S] {
      protected val delegate: Mounted[F, _, S]
      override def state                            = delegate.state
      override def modState(f: S => S, c: Callback) = delegate.modState(f, c)
      override def setState(s: S, c: Callback)      = delegate.setState(s, c)
    }

    //////////////////////////////////////////////////
    // Morphisms                                    //
    //////////////////////////////////////////////////

    trait MorphP[F[_], P0, P, S] extends Mounted[F, P, S] {
      protected val delegate: Mounted[F, P0, _]
      protected val pMap: P0 => P
      override def props         = F.map(delegate.props)(pMap)
      override def propsChildren = delegate.propsChildren
    }

    trait MorphS[F[_], P, S0, S] extends Mounted[F, P, S] {
      protected val delegate: Mounted[F, _, S0]
      protected val sGet: S0 => S
      protected val sSet: (S0, S) => S0
      protected def sMod(s0: S0, f: S => S): S0     = sSet(s0, f(sGet(s0)))
      override def state                            = F.map(delegate.state)(sGet)
      override def modState(f: S => S, c: Callback) = delegate.modState(sMod(_, f), c)
      override def setState(s: S, c: Callback)      = delegate.modState(sSet(_, s), c)
    }

    class MorphedP[F[_], P0, P, S](protected val delegate: Mounted[F, P0, S],
                                   protected val pMap: P0 => P)
                                  (implicit protected val F: Effect[F])
      extends DelegateG[F, P, S] with MorphP[F, P0, P, S] with DelegateS[F, P, S] {
      override def mapProps[X](f: P => X) =
        new MorphedP[F, P0, X, S](delegate, f compose pMap)
      override def zoomState[X](get: S => X)(set: (S, X) => S) =
        new MorphedPS[F, P0, P, S, X](delegate, pMap, get, set)
    }

    class MorphedS[F[_], P, S0, S](protected val delegate: Mounted[F, P, S0],
                                   protected val sGet: S0 => S,
                                   protected val sSet: (S0, S) => S0)
                                  (implicit protected val F: Effect[F])
      extends DelegateG[F, P, S] with DelegateP[F, P, S] with MorphS[F, P, S0, S] {
      override def mapProps[X](f: P => X) =
        new MorphedPS[F, P, X, S0, S](delegate, f, sGet, sSet)
      override def zoomState[X](get: S => X)(set: (S, X) => S) =
        new MorphedS[F, P, S0, X](delegate, get compose sGet, (s0, x) => sMod(s0, set(_, x)))
    }

    class MorphedPS[F[_], P0, P, S0, S](protected val delegate: Mounted[F, P0, S0],
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
}