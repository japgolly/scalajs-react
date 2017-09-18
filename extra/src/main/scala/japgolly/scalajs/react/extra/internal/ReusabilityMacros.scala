package japgolly.scalajs.react.extra.internal

import scala.reflect.macros.blackbox.Context
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.internal.MacroUtils

class ReusabilityMacros(val c: Context) extends MacroUtils {
  import c.universe._

  def quietCaseClass[T: c.WeakTypeTag]: c.Expr[Reusability[T]] =
    implCaseClass[T](Nil, false)

  def debugCaseClass[T: c.WeakTypeTag]: c.Expr[Reusability[T]] =
    implCaseClass[T](Nil, true)

  def quietCaseClassExcept[T: c.WeakTypeTag](field1: c.Expr[scala.Symbol], fieldN: c.Expr[scala.Symbol]*): c.Expr[Reusability[T]] =
    implCaseClass[T](field1 :: fieldN.toList, false)

  def debugCaseClassExcept[T: c.WeakTypeTag](field1: c.Expr[scala.Symbol], fieldN: c.Expr[scala.Symbol]*): c.Expr[Reusability[T]] =
    implCaseClass[T](field1 :: fieldN.toList, true)

  private def implCaseClass[T: c.WeakTypeTag](exclusions: Seq[c.Expr[scala.Symbol]], debug: Boolean): c.Expr[Reusability[T]] = {
    val T              = caseClassType[T]
    val fieldsAndTypes = primaryConstructorParamsExcluding(T, exclusions)
    val Reusability    = q"_root_.japgolly.scalajs.react.extra.Reusability"

    val impl =
      fieldsAndTypes match {
        case Nil =>
          q"$Reusability.const[$T](true)"

        case (n, t) :: Nil =>
          q"$Reusability.by[$T,$t](_.$n)"

        case _ =>
          var insts = Vector.empty[ValDef]
          var tests = Vector.empty[Tree]

          val reusability = c.typeOf[japgolly.scalajs.react.extra.Reusability[_]]
          val memo = collection.mutable.HashMap.empty[Type, TermName]
          for ((n, t) <- fieldsAndTypes) {
            val fp = memo.getOrElseUpdate(t, {
              val tmp = TermName(c.freshName())
              insts :+= q"val $tmp = ${needInferImplicit(appliedType(reusability, t))}"
              tmp
            })
            tests :+= q"$fp.test(a.$n, b.$n)"
          }

          val testExpr = tests.reduce((a,b) => q"$a && $b")

          q""" {
            ..$insts
            $Reusability[$T]((a,b) => $testExpr)
          } """
      }

    if (debug) println("\n" + showCode(impl) + "\n")
    c.Expr[Reusability[T]](impl)
  }
}
