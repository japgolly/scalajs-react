package japgolly.scalajs.react

import org.scalajs.dom
import scala.scalajs.js
import japgolly.scalajs.react.internal._
import ScalaComponent._

final class ScalaComponent[P, S, B, CT[_, _] <: CtorType[_, _]](val jsInstance: JsComp[P, S, B, CT])
                                                               (implicit pf: Profunctor[CT])
  extends Component[P, CT, Unmounted[P, S, B]] {

  override val ctor: CT[P, Unmounted[P, S, B]] =
    jsInstance.ctor.dimap(Box(_), _.mapProps(_.a).mapMounted(_.rawInstance.mounted))
}

object ScalaComponent {

  @js.native
  trait Vars[P, S, B] extends js.Object {
    var mounted : Mounted[P, S, B]
    var mountedC: MountedC[P, S, B]
    var backend : B
  }

  type JsComp[P, S, B, CT[_, _] <: CtorType[_, _]] =
    JsComponent[Box[P], Box[S], CT, JsComponent.MountedWithRawType[Box[P], Box[S], Vars[P, S, B]]]

  type JsMounted[P, S, B] =
    JsComponent.MountedWithRawType[Box[P], Box[S], Vars[P, S, B]]

  type Unmounted[P, S, B] = Component.Unmounted[P, Mounted[P, S, B]]
  type Mounted  [P, S, B] = MountedF[Effect.Id, P, S, B]
  type MountedC [P, S, B] = MountedF[CallbackTo, P, S, B]
  type BackendScope[P, S] = Component.Mounted[CallbackTo, P, S]

  final class MountedF[F[_], P, S, B](val jsInstance: JsMounted[P, S, B])(implicit override protected val F: Effect[F])
      extends Component.Mounted[F, P, S] {

    def backend: F[B] =
      F point jsInstance.rawInstance.backend

    override def isMounted: F[Boolean] =
      F point jsInstance.isMounted

    override def props: F[P] =
      F point jsInstance.props.a

    override def propsChildren: F[PropsChildren] =
      F point jsInstance.propsChildren

    override def state: F[S] =
      F point jsInstance.state.a

    override def setState(newState: S, callback: Callback = Callback.empty): F[Unit] =
      F point jsInstance.setState(Box(newState), callback)

    override def modState(mod: S => S, callback: Callback = Callback.empty): F[Unit] =
      F point jsInstance.modState(s => Box(mod(s.a)), callback)

    override def getDOMNode: F[dom.Element] =
      F point jsInstance.getDOMNode

    override def forceUpdate(callback: Callback = Callback.empty): F[Unit] =
      F point jsInstance.forceUpdate(callback)
  }
}
