package japgolly.scalajs.react

import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.component.builder.{EntryPoint, EntryPointHidden}
import japgolly.scalajs.react.vdom.VdomNode

object ScalaComponent {
  import EntryPointHidden._

  // TODO: https://github.com/lampepfl/dotty/issues/12111
  // export Scala.*
  export Scala.{Vars => _, *}
  type Vars[P, S, B] = Scala.Vars[P, S, B]

  val builder = EntryPoint

  /** Create a component that always displays the same content, never needs to be redrawn, never needs vdom diffing. */
  inline def static(inline name: String)(content: VdomNode): Component[Unit, Unit, Unit, CtorType.Nullary] =
    builder.static(name)(content).build

  /** Create a component that always displays the same content, never needs to be redrawn, never needs vdom diffing. */
  inline def static(content: VdomNode): Component[Unit, Unit, Unit, CtorType.Nullary] =
    static(
      ScalaJsReactConfig.Instance.automaticComponentName(autoNameFull))(
      content)

  val Lifecycle = japgolly.scalajs.react.component.builder.Lifecycle

  /** This is terrible and repulsive but Scala doesn't allow anything less repulsive.
    * We'll keep this correctly modelling the reality for now and soon see if maybe we can use macros to
    * simplify it's creation (and avoid the need to use this explicitly).
    */
  type Config[P, C <: Children, S, B, US <: UpdateSnapshot, US2 <: UpdateSnapshot] =
    japgolly.scalajs.react.component.builder.ComponentBuilder.Config[P, C, S, B, US, US2]

  type Builder[P, C <: Children, S, B, US <: UpdateSnapshot] =
    japgolly.scalajs.react.component.builder.ComponentBuilder.LastStep[P, C, S, B, US]
}

type ScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]] = Scala.Component[P, S, B, CT]

type BackendScope[P, S] = Scala.BackendScope[P, S]
