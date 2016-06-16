package japgolly.scalajs.react

import org.scalajs.dom
import scala.scalajs.js
import japgolly.scalajs.react.internal._
import ScalaComponent._

final class ScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]](val js: JsComp[P, S, B, CT])
                                                               (implicit pf: Profunctor[CT])
  extends Component[P, CT, Unmounted[P, S, B]] {

  override val ctor: CT[P, Unmounted[P, S, B]] =
    js.ctor.dimap(Box(_), _.mapProps(_.a).mapMounted(_.raw.mounted))
}

object ScalaComponent {

  @js.native
  trait Vars[P, S, B] extends js.Object {
    var mounted : Mounted[P, S, B]
    var mountedC: MountedC[P, S, B]
    var backend : B
  }

  type JsComp[P, S, B, CT[-p, +u] <: CtorType[p, u]] =
    JsComponent[Box[P], Box[S], CT, JsComponent.MountedWithRawType[Box[P], Box[S], Vars[P, S, B]]]

  type JsMounted[P, S, B] =
    JsComponent.MountedWithRawType[Box[P], Box[S], Vars[P, S, B]]

  type Unmounted   [P, S, B] = Component.Unmounted[P, Mounted[P, S, B]]
  type Mounted     [P, S, B] = MountedF[Effect.Id, P, S, B]
  type MountedC    [P, S, B] = MountedF[CallbackTo, P, S, B]
  type BackendScope[P, S]    = Component.Mounted[CallbackTo, P, S]

  final class MountedF[F[+_], P, S, B](val js: JsMounted[P, S, B])(implicit override protected val F: Effect[F])
      extends Component.Mounted[F, P, S] {

    def backend: F[B] =
      F point js.raw.backend

    override def isMounted: F[Boolean] =
      F point js.isMounted

    override def props: F[P] =
      F point js.props.a

    override def propsChildren: F[PropsChildren] =
      F point js.propsChildren

    override def state: F[S] =
      F point js.state.a

    override def setState(newState: S, callback: Callback = Callback.empty): F[Unit] =
      F point js.setState(Box(newState), callback)

    override def modState(mod: S => S, callback: Callback = Callback.empty): F[Unit] =
      F point js.modState(s => Box(mod(s.a)), callback)

    override def getDOMNode: F[dom.Element] =
      F point js.getDOMNode

    override def forceUpdate(callback: Callback = Callback.empty): F[Unit] =
      F point js.forceUpdate(callback)
  }
}
