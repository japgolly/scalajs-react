package japgolly.scalajs.react.component.builder

import japgolly.scalajs.react.component.builder.AutoComponentName
import japgolly.scalajs.react.component.builder.ComponentBuilder._
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, ScalaJsReactConfig, UpdateSnapshot}
import scala.annotation.elidable

object EntryPoint {

  @elidable(elidable.INFO)
  private def optionallyRetainName(name: => String): String =
    ScalaJsReactConfig.componentNameModifier(name)

  /** Begin creating a component. */
  def apply[Props](implicit name: AutoComponentName): Step1[Props] =
    apply[Props](name.value)

  /** Begin creating a component. */
  def apply[Props](displayName: => String): Step1[Props] =
    new Step1[Props](optionallyRetainName(displayName))

  /** Partially builds a component that always displays the same content, never needs to be redrawn, never needs vdom diffing.
    * The builder is returned and can be customised futher before finally being built.
    */
  def static(displayName: => String)(content: VdomNode): Step4[Unit, Children.None, Unit, Unit, UpdateSnapshot.None] =
    apply[Unit](displayName)
      .renderStatic(content)
      .shouldComponentUpdateConst(false)

  /** Partially builds a component that always displays the same content, never needs to be redrawn, never needs vdom diffing.
    * The builder is returned and can be customised futher before finally being built.
    */
  def static(content: VdomNode)(implicit name: AutoComponentName): Step4[Unit, Children.None, Unit, Unit, UpdateSnapshot.None] =
    static(name.value)(content)
}
