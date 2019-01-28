package japgolly.scalajs.react.internal

import scala.reflect.macros.blackbox.Context
import japgolly.scalajs.react.Reusability

class ReusabilityMacros(val c: Context) extends MacroUtils {
  import c.universe._

  def quietDerive[T: c.WeakTypeTag]: c.Expr[Reusability[T]] =
    implDerive[T](false)

  def debugDerive[T: c.WeakTypeTag]: c.Expr[Reusability[T]] =
    implDerive[T](true)

  def quietCaseClass[T: c.WeakTypeTag]: c.Expr[Reusability[T]] =
    implCaseClass[T](Nil, false)

  def debugCaseClass[T: c.WeakTypeTag]: c.Expr[Reusability[T]] =
    implCaseClass[T](Nil, true)

  def quietCaseClassExcept[T: c.WeakTypeTag](field1: c.Expr[scala.Symbol], fieldN: c.Expr[scala.Symbol]*): c.Expr[Reusability[T]] =
    implCaseClass[T](field1 :: fieldN.toList, false)

  def debugCaseClassExcept[T: c.WeakTypeTag](field1: c.Expr[scala.Symbol], fieldN: c.Expr[scala.Symbol]*): c.Expr[Reusability[T]] =
    implCaseClass[T](field1 :: fieldN.toList, true)

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  private val Reusability = q"_root_.japgolly.scalajs.react.Reusability"

  private def ReusabilityA(t: Type): Type =
    appliedType(c.typeOf[japgolly.scalajs.react.Reusability[_]], t)

  private def implSumType[T: c.WeakTypeTag](debug: Boolean, derive: Boolean): c.Expr[Reusability[T]] = {
    val T     = weakTypeOf[T]
    val init  = new Init("i$" + _)
    val bind1 = TermName("bound1")
    val bind2 = TermName("bound2")

    def mkCase(t0: Type, impl: Tree): CaseDef = {
      val t = fixAdtTypeForCaseDef(t0)
      if (showCode(impl) contains ".Reusability.always[") // Hilarious (ab)use of meta-programming!
        cq"_: $t => y.isInstanceOf[$t]"
      else {
        val instance = init.valDef(impl)
        cq"$bind1: $t => y match { case $bind2: $t => $instance.test($bind1,$bind2); case _ => false }"
      }
    }

    def findForSubType(t: Type): Option[CaseDef] =
      tryInferImplicit(ReusabilityA(t)).map(mkCase(t, _))

    def deriveSubType(t: Type): CaseDef = {
      val impl = implDerive(false)(c.WeakTypeTag(t)).tree
      mkCase(t, impl)
    }

    val cases: Vector[CaseDef] =
      crawlADT[CaseDef](
        T,
        s => findForSubType(s.toType),
        s => if (derive)
          Vector.empty :+ deriveSubType(s.toType)
        else
          fail(s"Reusability[$s] not found."))

    val impl: Tree =
      init.wrap(q"$Reusability[$T]((x,y) => x match { case ..$cases })")

    if (debug) println("\n" + showCode(impl) + "\n")
    c.Expr[Reusability[T]](impl)
  }

  private def implCaseClass[T: c.WeakTypeTag](exclusions: Seq[c.Expr[scala.Symbol]], debug: Boolean): c.Expr[Reusability[T]] = {
    val T              = caseClassType[T]
    val fieldsAndTypes = primaryConstructorParamsExcluding(T, exclusions)

    val impl =
      fieldsAndTypes match {
        case Nil =>
          q"$Reusability.always[$T]"

        case (n, t) :: Nil =>
          q"$Reusability.by[$T,$t](_.$n)"

        case _ =>
          val init  = new Init("i$" + _)
          var tests = Vector.empty[Tree]

          val memo = collection.mutable.HashMap.empty[Type, TermName]
          for ((n, t) <- fieldsAndTypes) {
            val fp = memo.getOrElseUpdate(t, init.valImp(ReusabilityA(t)))
            tests :+= q"$fp.test(a.$n, b.$n)"
          }

          val testExpr = tests.reduce((a,b) => q"$a && $b")
          init.wrap(q"$Reusability[$T]((a,b) => $testExpr)")
      }

    if (debug) println("\n" + showCode(impl) + "\n")
    c.Expr[Reusability[T]](impl)
  }

  private def implDerive[T: c.WeakTypeTag](debug: Boolean): c.Expr[Reusability[T]] = {
    val T = weakTypeOf[T]
    if (T.dealias.typeSymbol.isAbstract)
      implSumType(debug, true)
    else
      implCaseClass(Nil, debug)
  }
}
