package japgolly.scalajs.react.extra.internal

import scala.reflect.macros.blackbox.Context
import japgolly.scalajs.react.internal.MacroUtils

class PxMacros(val c: Context) extends MacroUtils {
  import c.universe._

  def extract[T: c.WeakTypeTag]: c.Expr[T] = {
    val T = weakTypeOf[T]

    val impl = T.dealias match {
      case TypeRef(_, sym, _) if sym.fullName startsWith "scala.Function" =>
        val argCount = sym.fullName.drop(14).toInt
        val names    = (0 until argCount).map(i => TermName(('a' + i).toChar.toString))
        val params   = names.map(n => ValDef(Modifiers(Flag.PARAM), n, TypeTree(), EmptyTree))
        q"""
          val px: _root_.japgolly.scalajs.react.extra.Px[$T] = ${c.prefix}
          ((..$params) => px.value()(..$names)): $T
        """

      case _ =>
        fail(s"Px.extract works with functions, not $T.")
    }

    c.Expr[T](impl)
  }
}
