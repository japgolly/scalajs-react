package japgolly.scalajs.react.internal

import japgolly.microlibs.compiletime.MacroUtils
import japgolly.scalajs.react.Reusability
import scala.reflect.macros.blackbox.Context

class ReusabilityMacros(val c: Context) extends MacroUtils {
  import c.universe._

  def derive[A: c.WeakTypeTag]: c.Expr[Reusability[A]] =
    deriveImpl[A](
      logNonReuse = false,
      logCode     = false,
    )

  def deriveDebug[A: c.WeakTypeTag]: c.Expr[Reusability[A]] =
    deriveImpl[A](
      logNonReuse = true,
      logCode     = true,
    )

  def deriveDebugWithArgs[A: c.WeakTypeTag](logNonReuse: c.Expr[Boolean],
                                            logCode    : c.Expr[Boolean]): c.Expr[Reusability[A]] =
    deriveImpl[A](
      logNonReuse = readMacroArg_boolean(logNonReuse),
      logCode     = readMacroArg_boolean(logCode),
    )

  def caseClassExcept[A: c.WeakTypeTag](field1: c.Expr[String], fieldN: c.Expr[String]*): c.Expr[Reusability[A]] =
    caseClassImpl[A](
      logNonReuse  = false,
      logCode      = false,
      exclusions   = field1 :: fieldN.toList,
    )

  def caseClassExceptDebug[A: c.WeakTypeTag](field1: c.Expr[String], fieldN: c.Expr[String]*): c.Expr[Reusability[A]] =
    caseClassImpl[A](
      logNonReuse = true,
      logCode     = true,
      exclusions  = field1 :: fieldN.toList,
    )

  def caseClassExceptDebugWithArgs[A: c.WeakTypeTag](logNonReuse: c.Expr[Boolean],
                                                     logCode    : c.Expr[Boolean])
                                                    (field1: c.Expr[String], fieldN: c.Expr[String]*): c.Expr[Reusability[A]] =
    caseClassImpl[A](
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

  private class Preparations(logCode: Boolean) {
    private val init1  = new Init("a$" + _)
    private val init2  = new Init("b$" + _, lazyVals = true)
    private val lazies = List.newBuilder[Tree]
    private var preps  = Map.empty[Type, Prepared[_]]
    private val stable = collection.mutable.HashMap.empty[Type, TermName]

    private def normalise(t: Type): Type =
      t.dealias

    def addNow[A: c.WeakTypeTag](complete: Tree): Prepared[A] = {
      val typ = weakTypeOf[A]
      val ra = ReusabilityA(typ)
      val prep = TermName(init1.newName())
      init1 += q"implicit val $prep: $ra = $complete"
      val p = Prepared[A](typ, prep, None)
      preps = preps.updated(normalise(p.typ), p)
      p
    }

    def addDeferred[A: c.WeakTypeTag](complete: => Tree): Prepared[A] = {
      val typ = weakTypeOf[A]
      val ra = ReusabilityA(typ)
      val prep = init1.varDef(ra, q"new Reusability(null)")
      lazies += q"""implicit lazy val ${TermName("_" + prep.decodedName)}: $ra = $Reusability.suspend($prep)"""
      lazy val completed: c.Expr[Reusability[A]] = {
        val inner = complete
        val impl = q"$prep = $inner"
        c.Expr[Reusability[A]](impl)
      }
      val p = Prepared[A](typ, prep, Some(() => completed))
      preps = preps.updated(normalise(p.typ), p)
      p
    }

    /** @param typ Just `A`, not `Reusable[A]` */
    def get(typ: Type): Option[TermName] = {
      val t = normalise(typ)
      preps.get(t).map(_.varName)
    }

    /** @param typ Just `A`, not `Reusable[A]` */
    def need(typ: Type): TermName =
      get(typ).getOrElse(throw new IllegalStateException(s"Prepared type for ${normalise(typ)} not found!"))

    def getStablisedImplicitInstance(t: Type): TermName =
      getStablisedImplicitInstance(t, q"${ReusabilityA(t)}")

    def getStablisedImplicitInstance(t: Type, impl: => Tree): TermName = {
      def target = get(t) getOrElse init2.valImp(impl)
      stable.getOrElseUpdate(t, target)
    }

    def stabliseInstance(t: Type, impl: Tree): TermName =
      init2.valDef(t, impl)

    def result[A: WeakTypeTag](finalResult: Prepared[A]): c.Expr[Reusability[A]] = {
      init1 ++= lazies.result()
      val impl =
        init1.wrap {
          init2.wrap {
            type R = c.Expr[Reusability[_]]
            val allPreps = preps.valuesIterator.flatMap[R](_.complete.map(_())).toList
            q"..$allPreps; ${finalResult.varName}"
          }
        }

      if (logCode)
        printCode(ReusabilityA(weakTypeTag[A].tpe), impl)

      c.Expr[Reusability[A]](impl)
    }
  }

  private object Preparations {
    def apply[A: WeakTypeTag](logCode: Boolean)(f: Preparations => Prepared[A]): c.Expr[Reusability[A]] = {
      val preparations = new Preparations(logCode)
      val prepared     = f(preparations)
      preparations.result(prepared)
    }
  }

  /** @param typ Just `A`, not `Reusable[A]` */
  private case class Prepared[A](typ: Type, varName: TermName, complete: Option[() => c.Expr[Reusability[A]]])

  private def deriveImpl[A: c.WeakTypeTag](logNonReuse: Boolean,
                                           logCode    : Boolean): c.Expr[Reusability[A]] =
    Preparations(logCode = logCode) { preparations =>
      deriveImplInner[A](
        logNonReuse  = logNonReuse,
        preparations = preparations,
      )
    }

  private def caseClassImpl[A: c.WeakTypeTag](logNonReuse: Boolean,
                                              logCode    : Boolean,
                                              exclusions : Seq[c.Expr[String]]): c.Expr[Reusability[A]] =
    Preparations(logCode = logCode) { preparations =>
      caseClassImplInner[A](
        logNonReuse  = logNonReuse,
        preparations = preparations,
        exclusions   = exclusions,
      )
    }

  // ===================================================================================================================

  private def deriveImplInner[A: c.WeakTypeTag](logNonReuse: Boolean,
                                                preparations: Preparations): Prepared[A] = {
    val A = weakTypeOf[A]
    if (A.dealias.typeSymbol.isAbstract)
      sumTypeImplInner[A](
        derive       = true,
        logNonReuse  = logNonReuse,
        preparations = preparations,
      )
    else
      caseClassImplInner[A](
        logNonReuse  = logNonReuse,
        preparations = preparations,
        exclusions   = Nil,
      )
  }

  private def sumTypeImplInner[A: c.WeakTypeTag](derive      : Boolean,
                                                 logNonReuse : Boolean,
                                                 preparations: Preparations): Prepared[A] = {
    val A     = weakTypeOf[A]
    val bind1 = TermName("bound1")
    val bind2 = TermName("bound2")

    def mkCase(t0: Type, impl: Either[TermName, Tree]): CaseDef = {
      val t = fixAdtTypeForCaseDef(t0)
      impl match {
        case Right(i) if showCode(i) contains ".Reusability.always[" => // Hilarious (ab)use of meta-programming!
          cq"_: $t => y.isInstanceOf[$t]"
        case _ =>
          val instance    = impl.fold(identityFn, preparations.stabliseInstance(ReusabilityA(t0), _))
          val test        = q"$instance.test($bind1,$bind2)"
          var onWrongType = nop
          if (logNonReuse) {
            val header = nonReuseHeader(A)
            val msg = nonReuseDesc(s"values have different types", q"x", q"y")
            onWrongType = q"""println($header + $msg)"""
          }
          cq"$bind1: $t => y match { case $bind2: $t => $test; case _ => $onWrongType; false }"
      }
    }

    def prepareToDeriveSubType(t: Type): Prepared[t.type] =
      deriveImplInner[t.type](logNonReuse = logNonReuse, preparations = preparations)(c.WeakTypeTag(t))

    val nonRecursiveCases: Map[Type, CaseDef] =
      crawlADT[(Type, CaseDef)](
        A,
        (_, s) => tryInferImplicit(ReusabilityA(s)).map(i => s -> mkCase(s, Right(i))),
        (_, s) => if (derive) {
          prepareToDeriveSubType(s)
          Nil
        } else
          fail(s"Reusability[$s] not found.")
      ).toMap

    preparations.addDeferred[A] {

      val cases: Vector[CaseDef] =
        crawlADT[CaseDef](
          A,
          (_, s) => nonRecursiveCases.get(s),
          (_, s) => if (derive) {
            val name = preparations.need(s)
            mkCase(s, Left(name)) :: Nil
          } else
            fail(s"Reusability[$s] not found."))

      q"$Reusability[$A]((x,y) => x match { case ..$cases })"
    }
  }

  private def caseClassImplInner[A: c.WeakTypeTag](logNonReuse : Boolean,
                                                   preparations: Preparations,
                                                   exclusions  : Seq[c.Expr[String]]): Prepared[A] = {
    val A              = caseClassType[A]
    val fieldsAndTypes = primaryConstructorParamsExcluding(A, exclusions)

    fieldsAndTypes match {
      case Nil =>
        preparations.addNow[A] {
          q"$Reusability.always[$A]"
        }

      case (n, t) :: Nil if !logNonReuse =>
        preparations.addDeferred[A] {
          preparations.get(t) match {
            case Some(impVar) => q"$Reusability.by[$A,$t](_.$n)($impVar)"
            case None         => q"$Reusability.by[$A,$t](_.$n)"
          }
        }

      case _ =>
        preparations.addDeferred[A] {
          var tests = Vector.empty[Tree]

          for ((n, t) <- fieldsAndTypes) {
            val fp = preparations.getStablisedImplicitInstance(t)

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
            val header  = nonReuseHeader(A)
            val output  = q"""failures.sorted.mkString($header, "\n", "")"""
            val logRes  = q"if (failures.nonEmpty) println($output)"
            q"""$Reusability[$A]((a,b) => {
                $start
                $testAll
                $logRes
                failures.isEmpty
              })"""
          } else {
            val testExpr = tests.reduce((a,b) => q"$a && $b")
            q"$Reusability[$A]((a,b) => $testExpr)"
          }
        }
    }
  }
}
