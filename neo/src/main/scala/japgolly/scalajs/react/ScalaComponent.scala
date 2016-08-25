package japgolly.scalajs.react

import org.scalajs.dom
import scala.scalajs.js
import japgolly.scalajs.react.internal._
import ScalaComponent._

final class ScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]](val js: JsComp[P, S, B, CT])
                                                                 (implicit pf: Profunctor[CT])
  extends Component[P, CT, Unmounted[P, S, B]] {

  override val ctor: CT[P, Unmounted[P, S, B]] =
    js.ctor.dimap(Box(_), _.mapProps(_.unbox).mapMounted(_.raw.mounted))
}

object ScalaComponent {

  @inline def build[P](name: String) =
    new ScalaComponentB.Step1[P](name)

  @js.native
  trait Vars[P, S, B] extends js.Object {
    var mounted  : Mounted[P, S, B]
    var mountedCB: MountedCB[P, S, B]
    var backend  : B
  }

  type JsComp[P, S, B, CT[-p, +u] <: CtorType[p, u]] =
    JsComponent[Box[P], Box[S], CT, JsMounted[P, S, B]]

  type JsMounted[P, S, B] =
    JsComponent.MountedWithRawType[Box[P], Box[S], Vars[P, S, B]]

  type Unmounted   [P, S, B] = Component.Unmounted[P, Mounted[P, S, B]]
  type Mounted     [P, S, B] = MountedF[Effect.Id, P, S, B]
  type MountedCB   [P, S, B] = MountedF[CallbackTo, P, S, B]
  type BackendScope[P, S]    = Component.Mounted[CallbackTo, P, S]

  final class MountedF[F[+_], P, S, B](val js: JsMounted[P, S, B])(implicit override protected val F: Effect[F])
      extends Component.Mounted[F, P, S] {

    // B instead of F[B] because
    // 1. Builder takes a MountedCB but needs immediate access to this.
    // 2. It never changes once initialised.
    // Note: Keep this is def instead of val because the builder sets it after creation.
    def backend: B =
      js.raw.backend

    override def isMounted: F[Boolean] =
      F point js.isMounted

    override def props: F[P] =
      F point js.props.unbox

    override def propsChildren: F[PropsChildren] =
      F point js.propsChildren

    override def state: F[S] =
      F point js.state.unbox

    override def setState(newState: S, callback: Callback = Callback.empty): F[Unit] =
      F point js.setState(Box(newState), callback)

    override def modState(mod: S => S, callback: Callback = Callback.empty): F[Unit] =
      F point js.modState(s => Box(mod(s.unbox)), callback)

    override def getDOMNode: F[dom.Element] =
      F point js.getDOMNode

    override def forceUpdate(callback: Callback = Callback.empty): F[Unit] =
      F point js.forceUpdate(callback)
  }
}
