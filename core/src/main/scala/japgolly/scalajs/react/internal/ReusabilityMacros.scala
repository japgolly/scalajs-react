package japgolly.scalajs.react.internal

import scala.reflect.macros.blackbox.Context
import japgolly.scalajs.react.Reusability

class ReusabilityMacros(val c: Context) extends MacroUtils {
  import c.universe._

  def derive[T: c.WeakTypeTag]: c.Expr[Reusability[T]] =
    deriveImpl[T](
      logNonReuse = false,
      logCode     = false,
    )

  def deriveDebug[T: c.WeakTypeTag]: c.Expr[Reusability[T]] =
    deriveImpl[T](
      logNonReuse = true,
      logCode     = true,
    )

  def deriveDebugWithArgs[T: c.WeakTypeTag](logNonReuse: c.Expr[Boolean],
                                            logCode    : c.Expr[Boolean]): c.Expr[Reusability[T]] =
    deriveImpl[T](
      logNonReuse = readMacroArg_boolean(logNonReuse),
      logCode     = readMacroArg_boolean(logCode),
    )

  def caseClassExcept[T: c.WeakTypeTag](field1: c.Expr[String], fieldN: c.Expr[String]*): c.Expr[Reusability[T]] =
    caseClassImpl[T](
      logNonReuse  = false,
      logCode      = false,
      exclusions   = field1 :: fieldN.toList,
    )

  def caseClassExceptDebug[T: c.WeakTypeTag](field1: c.Expr[String], fieldN: c.Expr[String]*): c.Expr[Reusability[T]] =
    caseClassImpl[T](
      logNonReuse = true,
      logCode     = true,
      exclusions  = field1 :: fieldN.toList,
    )

  def caseClassExceptDebugWithArgs[T: c.WeakTypeTag](logNonReuse: c.Expr[Boolean],
                                                     logCode    : c.Expr[Boolean])
                                                    (field1: c.Expr[String], fieldN: c.Expr[String]*): c.Expr[Reusability[T]] =
    caseClassImpl[T](
      logNonReuse = readMacroArg_boolean(logNonReuse),
      logCode     = readMacroArg_boolean(logCode),
      exclusions  = field1 :: fieldN.toList,
    )

  // ===================================================================================================================

  private val Reusability = q"_root_.japgolly.scalajs.react.Reusability"

  private def ReusabilityA(t: Type): Type =
    appliedType(c.typeOf[japgolly.scalajs.react.Reusability[_]], t)

  private def printCode(typ: Type, tree: Tree): Unit =
    println(s"Generated Reusability[$typ]:\n${showCode(tree)}\n")

  private val nop: Tree = q"()"

  private def nonReuseHeader(t: Type) =
    s"Instance of $t not-reusable for the following reasons:\n"

  private def nonReuseDesc(desc: String, a: Tree, b: Tree) = {
    val msg1 = desc + "\n  A: "
    val msg2 = "\n  B: "
    q""""  " + ($msg1 + $a + $msg2 + $b).replace("\n", "\n  ")"""
  }

  private def deriveImpl[T: c.WeakTypeTag](logNonReuse: Boolean,
                                           logCode    : Boolean): c.Expr[Reusability[T]] = {

    val T = weakTypeOf[T]
    if (T.dealias.typeSymbol.isAbstract)
      sumTypeImpl(
        derive      = true,
        logCode     = logCode,
        logNonReuse = logNonReuse,
      )
    else
      caseClassImpl(
        logCode     = logCode,
        logNonReuse = logNonReuse,
        exclusions  = Nil,
      )
  }

  private def sumTypeImpl[T: c.WeakTypeTag](derive     : Boolean,
                                            logCode    : Boolean,
                                            logNonReuse: Boolean): c.Expr[Reusability[T]] = {
    val T     = weakTypeOf[T]
    val init  = new Init("i$" + _)
    val bind1 = TermName("bound1")
    val bind2 = TermName("bound2")

    def mkCase(t0: Type, impl: Tree): CaseDef = {
      val t = fixAdtTypeForCaseDef(t0)
      if (showCode(impl) contains ".Reusability.always[") // Hilarious (ab)use of meta-programming!
        cq"_: $t => y.isInstanceOf[$t]"
      else {
        val instance    = init.valDef(impl)
        val test        = q"$instance.test($bind1,$bind2)"
        var onWrongType = nop
        if (logNonReuse) {
          val header = nonReuseHeader(T)
          val msg = nonReuseDesc(s"values have different types", q"x", q"y")
          onWrongType = q"""println($header + $msg)"""
        }
        cq"$bind1: $t => y match { case $bind2: $t => $test; case _ => $onWrongType; false }"
      }
    }

    def findForSubType(t: Type): Option[CaseDef] =
      tryInferImplicit(ReusabilityA(t)).map(mkCase(t, _))

    def deriveSubType(t: Type): CaseDef = {
      val impl = deriveImpl(logCode = logCode, logNonReuse = logNonReuse)(c.WeakTypeTag(t)).tree
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

    if (logCode)
      printCode(T, impl)

    c.Expr[Reusability[T]](impl)
  }

  private def caseClassImpl[T: c.WeakTypeTag](logNonReuse: Boolean,
                                              logCode    : Boolean,
                                              exclusions : Seq[c.Expr[String]]): c.Expr[Reusability[T]] = {
    val T              = caseClassType[T]
    val fieldsAndTypes = primaryConstructorParamsExcluding(T, exclusions)

    val impl =
      fieldsAndTypes match {
        case Nil =>
          q"$Reusability.always[$T]"

        case (n, t) :: Nil if !logNonReuse =>
          q"$Reusability.by[$T,$t](_.$n)"

        case _ =>
          val init  = new Init("i$" + _)
          var tests = Vector.empty[Tree]

          val memo = collection.mutable.HashMap.empty[Type, TermName]
          for ((n, t) <- fieldsAndTypes) {
            val fp = memo.getOrElseUpdate(t, init.valImp(ReusabilityA(t)))

            val test = q"$fp.test(a.$n, b.$n)"
            val body =
              if (logNonReuse) {
                val msg = nonReuseDesc(s".${n.decodedName} values not reusable", q"a.$n", q"b.$n")
                q"if (!$test) failures ::= ($msg)"
              } else
                test
            tests :+= body
          }

          if (logNonReuse) {
            val start   = q"var failures = _root_.scala.List.empty[String]"
            val testAll = tests.reduce((a,b) => q"$a; $b")
            val header  = nonReuseHeader(T)
            val output  = q"""failures.sorted.mkString($header, "\n", "")"""
            val logRes  = q"if (failures.nonEmpty) println($output)"
            init.wrap(q"""$Reusability[$T]((a,b) => {
                $start
                $testAll
                $logRes
                failures.isEmpty
              })""")
          } else {
            val testExpr = tests.reduce((a,b) => q"$a && $b")
            init.wrap(q"$Reusability[$T]((a,b) => $testExpr)")
          }
      }

    if (logCode)
      printCode(T, impl)

    c.Expr[Reusability[T]](impl)
  }
}
