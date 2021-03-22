package japgolly.scalajs.react.internal

import japgolly.microlibs.macro_utils.MacroUtils
import scala.annotation.elidable
import scala.reflect.macros.blackbox.Context
import sourcecode.Util

trait AutoComponentName {
  def value: String
}

object AutoComponentName {

  def apply(s: => String): AutoComponentName =
    new AutoComponentName {
      @elidable(elidable.INFO)
      override def value = s
    }

  implicit def materialize: AutoComponentName =
    macro AutoComponentNameMacros.generate
}

final class AutoComponentNameMacros(val c: Context) extends MacroUtils {
  import c.universe._

  def generate: c.Expr[AutoComponentName] = {
    val owner =
      c.internal.enclosingOwner

    val name =
      owner.fullName.trim
        .split("\\.", -1)
        .iterator
        .filterNot(Util.isSyntheticName)
        .map(_.stripSuffix("$"))
        .mkString(".")

    val impl =
      q"_root_.japgolly.scalajs.react.internal.AutoComponentName($name)"

    c.Expr[AutoComponentName](impl)
  }

}
