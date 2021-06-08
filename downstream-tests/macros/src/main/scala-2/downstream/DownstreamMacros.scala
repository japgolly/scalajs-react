package downstream

import japgolly.microlibs.compiletime._
import scala.reflect.macros.blackbox.Context

object DownstreamMacros {
  def mainInit: Unit = macro DownstreamMacros.mainInitImpl
}

class DownstreamMacros(val c: Context) extends MacroUtils {
  import c.universe._

  private def sysProp(key: String): Option[String] =
    Option(System.getProperty(key, null))

  private def reusabilityDev = sysProp("downstream_tests.reusability.dev")

  def mainInitImpl: c.Tree = {
    var stmts = List.empty[Tree]
    reusabilityDev match {
      case Some("disable") => stmts ::= q"japgolly.scalajs.react.Reusability.disableGloballyInDev()"
      case Some("overlay") => stmts ::= q"japgolly.scalajs.react.extra.ReusabilityOverlay.overrideGloballyInDev()"
      case _               => ()
    }
    q"..$stmts; ()"
  }
}
