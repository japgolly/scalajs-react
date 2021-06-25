package japgolly.scalajs.react.component.builder

import japgolly.microlibs.compiletime.MacroEnv.*
import japgolly.scalajs.react.component.builder.ComponentBuilder._
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Children, ScalaJsReactConfig, UpdateSnapshot}
import scala.quoted.*

object EntryPoint {
  import EntryPointHidden._

  /** Begin creating a component. */
  inline def apply[Props]: Step1[Props] =
    apply[Props](ScalaJsReactConfig.Instance.automaticComponentName(autoNameFull))

  /** Begin creating a component. */
  inline def apply[Props](inline displayName: String): Step1[Props] =
    new Step1[Props](ScalaJsReactConfig.Instance.modifyComponentName(displayName))

  /** Partially builds a component that always displays the same content, never needs to be redrawn, never needs vdom diffing.
    * The builder is returned and can be customised futher before finally being built.
    */
  inline def static(inline displayName: String)(content: VdomNode): LastStep[Unit, Children.None, Unit, Unit, UpdateSnapshot.None] =
    apply[Unit](displayName)
      .noBackend
      .renderStatic(content)
      .shouldComponentUpdateConst(false)

  /** Partially builds a component that always displays the same content, never needs to be redrawn, never needs vdom diffing.
    * The builder is returned and can be customised futher before finally being built.
    */
  inline def static(content: VdomNode): LastStep[Unit, Children.None, Unit, Unit, UpdateSnapshot.None] =
    static(autoNameFull)(content)
}

// =====================================================================================================================

object EntryPointHidden {

  transparent inline def autoNameFull: String =
    ${ autoNameFullExpr }

  def autoNameFullExpr(using Quotes): Expr[String] = {
    import quotes.reflect.*

    var owner = Symbol.spliceOwner

    while (owner.flags.is(Flags.Synthetic))
      owner = owner.owner

    def reduceToType(): Unit =
      if !owner.isType then {
        val next = owner.maybeOwner
        if (!next.isNoSymbol) {
          owner = next
          reduceToType()
        }
      }
    reduceToType()

    val name =
      owner.fullName.trim
        .split("\\.", -1)
        .iterator
        .filterNot(sourcecode.Util.isSyntheticName)
        .map(_.stripSuffix("$"))
        .mkString(".")

    // println(s"owner = [${owner.fullName}], name = [$name]")

    Expr.inlineConst(name)
  }
}