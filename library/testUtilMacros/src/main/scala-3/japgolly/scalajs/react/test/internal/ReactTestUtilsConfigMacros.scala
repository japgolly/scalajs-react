package japgolly.scalajs.react.test.internal

import japgolly.microlibs.compiletime.*
import japgolly.scalajs.react.internal.CompileTimeConfig

object ReactTestUtilsConfigMacros {
  import ReactTestUtilsConfigKeys._
  import ReactTestUtilsConfigTypes._

  inline def aroundReact: AroundReact =
    inline CompileTimeConfig.getTrimLowerCaseNonBlank(KeyWarningsReact) match {
      case Some("fatal") => AroundReact.fatalReactWarnings
      case Some("warn")  => AroundReact.id
      case None          => AroundReact.id
      case Some(x)       =>
        InlineUtils.warn(s"Invalid value for $KeyWarningsReact: $x.\nValid values are: fatal | warn.")
        AroundReact.id
    }
}
