package japgolly.scalajs.react.test.internal

import japgolly.scalajs.react.internal.ConfigMacros
import japgolly.scalajs.react.test.internal.ReactTestUtilsConfigTypes._
import scala.reflect.macros.blackbox.Context

object ReactTestUtilsConfigMacros {
  def aroundReact: AroundReact =
    macro ReactTestUtilsConfigMacrosCls.aroundReact
}

class ReactTestUtilsConfigMacrosCls(override val c: Context) extends ConfigMacros(c) {
  import ReactTestUtilsConfigKeys._
  import c.universe._

  private def types = q"japgolly.scalajs.react.test.internal.ReactTestUtilsConfigTypes"

  def aroundReact: c.Expr[AroundReact] = {
    def fatal   = c.Expr[AroundReact](q"$types.AroundReact.fatalReactWarnings")
    def noop    = c.Expr[AroundReact](q"$types.AroundReact.id")
    def default = noop
    readConfig(KeyWarningsReact) match {
      case Some("fatal") => fatal
      case Some("warn")  => noop
      case None          => default
      case Some(x)       =>
        warn(s"Invalid value for $KeyWarningsReact: $x.\nValid values are: fatal | warn.")
        default
    }
  }
}
