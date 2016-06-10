package japgolly.scalajs.react

import japgolly.scalajs.react.internal._
import org.scalajs.dom
/*
import ScalaComponent._

final class ScalaComponent[P, S, B, CT[_, _] <: CtorType[_, _]](val jsInstance: JsComponent.Basic[Box[P], Box[S], CT])
                                                               (implicit pf: Profunctor[CT])
    extends Component[P, CT, Unmounted[P, S, B]] {

  override val ctor: CT[P, Unmounted[P, S, B]] =
    jsInstance.ctor.dimap(Box(_), new Unmounted[P, S, B](_))
}

object ScalaComponent {

  // ===================================================================================================================

  // TODO This is just JsComponent.Unmounted + dimap
  final class Unmounted[P, S, B](val jsInstance: JsComponent.BasicUnmounted[Box[P], Box[S]])
      extends Component.Unmounted[P, Mounted[CallbackTo, P, S, B]] { // TODO Mounted[CallbackTo, P, S, B] ← no

    override def key: Option[Key] =
      jsInstance.key

    override def ref: Option[String] =
      jsInstance.ref

    override def props: P =
      jsInstance.props.a

    override def propsChildren: PropsChildren =
      jsInstance.propsChildren

    override def renderIntoDOM(container: raw.ReactDOM.Container, callback: Callback = Callback.empty): Mounted[CallbackTo, P, S, B] = {
      val rc = raw.ReactDOM.render(jsInstance.rawElement, container, callback.toJsFn)
      ScalaComponentB.mountedFromJs(rc)
      // TODO ↑ it escaped!
      // TODO hey, should use addRawTypes to access the backed and mounted stuff. More honest.
    }
  }

  // ===================================================================================================================

  type BackendScope[P, S] = Mounted[CallbackTo, P, S, Null]

  private[CompScala] final class Mounted0[F[_], P, S, Backend](jsInstance: JsComponent.BasicMounted[Box[P], Box[S]])(implicit F: Effect[F])
      extends Mounted[F, P, S, Backend](jsInstance)(F) {
    var _backend: Backend = _
    override def backend: Backend = _backend
  }

  class MountedD[F[_], P, S, +Backend](val backend: Backend,
                                       jsInstance: JsComponent.BasicMounted[Box[P], Box[S]])(implicit F: Effect[F])
      extends Mounted[F, P, S, Backend](jsInstance)(F)

  abstract class Mounted[F[_], P, S, +Backend](jsInstance: JsComponent.BasicMounted[Box[P], Box[S]])
                                              (override protected final val F: Effect[F])
      extends Component.Mounted[F, P, S] {

//    def direct: Mounted[Effect.Id, P, S, Backend] =
//      new MountedD[Effect.Id, P, S, Backend](backend, jsInstance)

    def backend: Backend

    final def isMounted: F[Boolean] =
      F point jsInstance.isMounted

    final def props: F[P] =
      F point jsInstance.props.a

    final def propsChildren: F[PropsChildren] =
      F point jsInstance.propsChildren

    final def state: F[S] =
      F point jsInstance.state.a

    final def setState(newState: S, callback: Callback = Callback.empty): F[Unit] =
      F point jsInstance.setState(Box(newState), callback)

    final def modState(mod: S => S, callback: Callback = Callback.empty): F[Unit] =
      F point jsInstance.modState(s => Box(mod(s.a)), callback)

    final def getDOMNode: F[dom.Element] =
      F point jsInstance.getDOMNode

    override final def forceUpdate(callback: Callback = Callback.empty): F[Unit] =
      F point jsInstance.forceUpdate(callback)
  }
}
*/