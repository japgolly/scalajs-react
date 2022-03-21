package japgolly.scalajs.react.internal

import scala.reflect.macros.blackbox.Context

object ScalaJsReactConfigMacros {
  final val KeyCompNameAll = "japgolly.scalajs.react.component.names.all"
  final val KeyCompNameAuto = "japgolly.scalajs.react.component.names.implicit"
}

class ScalaJsReactConfigMacros(override val c: Context) extends ConfigMacros(c) {
  import ScalaJsReactConfigMacros._
  import c.universe._

  def automaticComponentName(displayName: c.Expr[String]): c.Expr[String] =
    modStr(displayName)(_automaticComponentName(_))

  private def _automaticComponentName(fullName: String): String = {
    def stripComponentSuffix(name: String): String =
      name.replaceFirst("(?i)\\.?comp(?:onent)?$", "")

    def stripPath(name: String): String =
      name.replaceFirst("^.+\\.", "")

    val name =
      fullName
        .trim
        .split("\\.", -1)
        .iterator
        .filterNot(sourcecode.Util.isSyntheticName)
        .map(_.stripSuffix("$"))
        .mkString(".")

    readConfig(KeyCompNameAuto) match {
      case Some("blank") => ""
      case Some("short") => stripPath(stripComponentSuffix(name))
      case Some("full")  => stripComponentSuffix(name)
      case None          => stripComponentSuffix(name)
      case Some(x)       =>
        warn(s"Invalid value for $KeyCompNameAuto: $x.\nValid values are: full | short | blank.")
        stripComponentSuffix(name)
    }
  }

  def modifyComponentName(displayName: c.Expr[String]): c.Expr[String] =
    modStr(displayName) { name =>
      readConfig(KeyCompNameAll) match {
        case Some("blank") => ""
        case Some("allow") => name
        case None          => name
        case Some(x)       =>
          warn(s"Invalid value for $KeyCompNameAll: $x.\nValid values are: allow | blank.")
          name
      }
    }

  private def _automaticComponentName(): String =
    _automaticComponentName(c.internal.enclosingOwner.owner.fullName)

  private def autoName: c.Expr[String] =
    _automaticComponentName()

  def entrypointApplyAuto[Props: c.WeakTypeTag]: c.Tree = {
    val Props = weakTypeOf[Props]
    q"_root_.japgolly.scalajs.react.component.builder.EntryPoint.apply[$Props]($autoName)"
  }

  def entrypointApplyManual[Props: c.WeakTypeTag](displayName: c.Tree): c.Tree = {
    val Props = weakTypeOf[Props]
    q"""
      new _root_.japgolly.scalajs.react.component.builder.ComponentBuilder.Step1[$Props](
        _root_.japgolly.scalajs.react.ScalaJsReactConfig.Instance.modifyComponentName($displayName)
      )
    """
  }

  def entrypointStaticAuto(content: c.Tree): c.Tree =
    q"_root_.japgolly.scalajs.react.component.builder.EntryPoint.static($autoName)($content)"

  def entrypointStaticManual(displayName: c.Tree)(content: c.Tree): c.Tree =
    q"""
      _root_.japgolly.scalajs.react.component.builder.ComponentBuilder.static(
        _root_.japgolly.scalajs.react.ScalaJsReactConfig.Instance.modifyComponentName($displayName),
        $content
      )
    """

  def componentStaticAuto(content: c.Tree): c.Tree =
    q"_root_.japgolly.scalajs.react.component.builder.EntryPoint.static($autoName)($content).build"

  def componentStaticManual(displayName: c.Tree)(content: c.Tree): c.Tree =
    q"_root_.japgolly.scalajs.react.component.builder.EntryPoint.static($displayName)($content).build"

  def vdomNodeStatic(vdom: c.Tree): c.Tree =
    q"""_root_.japgolly.scalajs.react.ScalaComponent.static("")($vdom).ctor()"""

  def vdomElementStatic(vdom: c.Tree): c.Tree =
    q"""_root_.japgolly.scalajs.react.ScalaComponent.static("")($vdom).ctor()"""

}
