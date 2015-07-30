package japgolly.scalajs.react.macros

import scala.reflect.macros.blackbox.Context
import japgolly.scalajs.react.extra.Reusability
import ReactMacroUtils._

object ReusabilityMacros {

  def quietCaseClass[T: c.WeakTypeTag](c: Context): c.Expr[Reusability[T]] = implCaseClass[T](c, false)
  def debugCaseClass[T: c.WeakTypeTag](c: Context): c.Expr[Reusability[T]] = implCaseClass[T](c, true)

  def implCaseClass[T: c.WeakTypeTag](c: Context, debug: Boolean): c.Expr[Reusability[T]] = {
    import c.universe._

    val T      = concreteWeakTypeOf[T](c)
    val params = primaryConstructorParams(c)

    val Reusability  =  q"_root_.japgolly.scalajs.react.extra.Reusability"
    val ReusabilityT = tq"_root_.japgolly.scalajs.react.extra.Reusability"

    val impl =
      params match {
        case Nil =>
          q"$Reusability.const[$T](true)"

        case param :: Nil =>
          val (n, t) = nameAndType(c)(param)
          q"$Reusability.by[$T,$t](_.$n)"

        case _ =>
          var insts = Vector.empty[ValDef]
          var tests = Vector.empty[Tree]

          for (p <- params) {
            val (n, t) = nameAndType[T](c)(p)
            val fp = TermName(c.freshName())
            insts :+= q"val $fp = implicitly[$ReusabilityT[$t]]"
            tests :+= q"$fp.test(a.$n, b.$n)"
          }

          val testExpr = tests.reduce((a,b) => q"$a && $b")

          q""" {
            ..$insts
            $Reusability.fn[$T]((a,b) => $testExpr)
          } """
      }

    if (debug) println("\n" + impl + "\n")
    c.Expr[Reusability[T]](impl)
  }
}
