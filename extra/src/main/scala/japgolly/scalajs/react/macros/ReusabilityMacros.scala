package japgolly.scalajs.react.macros

import scala.reflect.macros.blackbox.Context
import japgolly.scalajs.react.extra.Reusability

class ReusabilityMacros(val c: Context) extends ReactMacroUtils {
  import c.universe._

  def quietCaseClass[T: c.WeakTypeTag]: c.Expr[Reusability[T]] = implCaseClass[T](false)
  def debugCaseClass[T: c.WeakTypeTag]: c.Expr[Reusability[T]] = implCaseClass[T](true)

  def implCaseClass[T: c.WeakTypeTag](debug: Boolean): c.Expr[Reusability[T]] = {
    val T      = concreteWeakTypeOf[T]
    val params = primaryConstructorParams(T)

    val Reusability  =  q"_root_.japgolly.scalajs.react.extra.Reusability"

    val impl =
      params match {
        case Nil =>
          q"$Reusability.const[$T](true)"

        case param :: Nil =>
          val (n, t) = nameAndType(T, param)
          q"$Reusability.by[$T,$t](_.$n)"

        case _ =>
          var insts = Vector.empty[ValDef]
          var tests = Vector.empty[Tree]

          val reusability = c.typeOf[japgolly.scalajs.react.extra.Reusability[_]]
          for (p <- params) {
            val (n, t) = nameAndType(T, p)
            val fp = TermName(c.freshName())
            insts :+= q"val $fp = ${needInferImplicit(appliedType(reusability, t))}"
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
