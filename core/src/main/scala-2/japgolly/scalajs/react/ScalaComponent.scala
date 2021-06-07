package japgolly.scalajs.react

import japgolly.scalajs.react.internal.ScalaJsReactConfigMacros
import japgolly.scalajs.react.vdom.VdomNode

object ScalaComponent {
  import component.Scala

  type BackendScope   [P, S]                                = Scala.BackendScope[P, S]
  type Component      [P, S, B, CT[-p,+u] <: CtorType[p,u]] = Scala.Component[P, S, B, CT]
  type JsComponent    [P, S, B, CT[-p,+u] <: CtorType[p,u]] = Scala.JsComponent[P, S, B, CT]
  type JsMounted      [P, S, B]                             = Scala.JsMounted[P, S, B]
  type JsUnmounted    [P, S, B]                             = Scala.JsUnmounted[P, S, B]
  type Mounted        [F[_], P, S, B]                       = Scala.Mounted[F, P, S, B]
  type MountedImpure  [P, S, B]                             = Scala.MountedImpure[P, S, B]
  type MountedPure    [P, S, B]                             = Scala.MountedPure[P, S, B]
  type MountedRoot    [F[_], P, S, B]                       = Scala.MountedRoot[F, P, S, B]
  type MountedSimple  [F[_], P, S, B]                       = Scala.MountedSimple[F, P, S, B]
  type MountedWithRoot[F[_], P1, S1, B, P0, S0]             = Scala.MountedWithRoot[F, P1, S1, B, P0, S0]
  type RawMounted     [P, S, B]                             = Scala.RawMounted[P, S, B]
  type Unmounted      [P, S, B]                             = Scala.Unmounted[P, S, B]
  type Vars           [P, S, B]                             = Scala.Vars[P, S, B]

  @inline def mountedRoot[P, S, B](x: JsMounted [P, S, B]) = Scala.mountedRoot(x)
  @inline def mountRaw   [P, S, B](x: RawMounted[P, S, B]) = Scala.mountRaw(x)

  // ===================================================================================================================

  val builder = japgolly.scalajs.react.component.builder.EntryPoint

  /** Create a component that always displays the same content, never needs to be redrawn, never needs vdom diffing. */
  def static(displayName: => String)(content: VdomNode): Component[Unit, Unit, Unit, CtorType.Nullary] =
    macro ScalaJsReactConfigMacros.componentStaticManual

  /** Create a component that always displays the same content, never needs to be redrawn, never needs vdom diffing. */
  def static(content: VdomNode): Component[Unit, Unit, Unit, CtorType.Nullary] =
    macro ScalaJsReactConfigMacros.componentStaticAuto

  val Lifecycle = japgolly.scalajs.react.component.builder.Lifecycle

  /** This is terrible and repulsive but Scala doesn't allow anything less repulsive.
    * We'll keep this correctly modelling the reality for now and soon see if maybe we can use macros to
    * simplify it's creation (and avoid the need to use this explicitly).
    */
  type Config[P, C <: Children, S, B, US <: UpdateSnapshot, US2 <: UpdateSnapshot] =
    japgolly.scalajs.react.component.builder.ComponentBuilder.Config[P, C, S, B, US, US2]

}
