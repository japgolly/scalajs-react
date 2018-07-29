package japgolly.scalajs.react.component.builder

import japgolly.scalajs.react.{Children, UpdateSnapshot}
import japgolly.scalajs.react.vdom.VdomNode
import Builder._

object EntryPoint {

  /** Begin creating a component. */
  def apply[Props](displayName: String) =
    new Step1[Props](displayName)

  /** Partially builds a component that always displays the same content, never needs to be redrawn, never needs vdom diffing.
    * The builder is returned and can be customised futher before finally being built.
    */
  def static(displayName: String)(content: VdomNode): Step4[Unit, Children.None, Unit, Unit, UpdateSnapshot.None] =
    apply[Unit](displayName)
      .renderStatic(content)
      .shouldComponentUpdateConst(false)
}
