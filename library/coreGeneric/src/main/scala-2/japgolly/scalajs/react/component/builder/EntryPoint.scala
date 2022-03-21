package japgolly.scalajs.react.component.builder

import japgolly.scalajs.react.component.builder.ComponentBuilder._
import japgolly.scalajs.react.internal.ScalaJsReactConfigMacros
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, UpdateSnapshot}

object EntryPoint {

  /** Begin creating a component. */
  def apply[Props]: Step1[Props] =
    macro ScalaJsReactConfigMacros.entrypointApplyAuto[Props]

  /** Begin creating a component. */
  def apply[Props](displayName: String): Step1[Props] =
    macro ScalaJsReactConfigMacros.entrypointApplyManual[Props]

  /** Partially builds a component that always displays the same content, never needs to be redrawn, never needs vdom diffing.
    * The builder is returned and can be customised futher before finally being built.
    */
  def static(displayName: => String)(content: VdomNode): LastStep[Unit, Children.None, Unit, Unit, UpdateSnapshot.None] =
    macro ScalaJsReactConfigMacros.entrypointStaticManual

  /** Partially builds a component that always displays the same content, never needs to be redrawn, never needs vdom diffing.
    * The builder is returned and can be customised futher before finally being built.
    */
  def static(content: VdomNode): LastStep[Unit, Children.None, Unit, Unit, UpdateSnapshot.None] =
    macro ScalaJsReactConfigMacros.entrypointStaticAuto
}
