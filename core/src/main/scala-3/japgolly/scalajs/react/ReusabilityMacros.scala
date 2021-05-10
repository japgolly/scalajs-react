package japgolly.scalajs.react

import japgolly.microlibs.compiletime.MacroEnv.*
import japgolly.scalajs.react.Reusability
import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*
import scala.language.`3.0`

trait ReusabilityMacros {

  inline def derived[A]: Reusability[A] =
    derive[A]

  /** Generate an instance for A.
    *
    * If A is a sealed trait or sealed abstract class, Reusability is determined by sub-class reusability (which will
    * be derived when it doesn't exist).
    *
    * If A is a case class, Reusability is determined by each field's Reusability.
    */
  inline def derive[A]: Reusability[A] =
    derive()

  /** Generate an instance for A.
    *
    * If A is a sealed trait or sealed abstract class, Reusability is determined by sub-class reusability (which will
    * be derived when it doesn't exist).
    *
    * If A is a case class, Reusability is determined by each field's Reusability.
    *
    * @param logNonReuse Log to the console when and why non-reusable values are detected
    * @param logCode Log to generated Scala code to the screen on compilation.
    */
  inline def derive[A](inline logNonReuse  : Boolean = false,
                       inline logCode      : Boolean = false,
                       inline excludeFields: String  = "",
                      ): Reusability[A] =
    ${
      ReusabilityMacros.derive[A](
        _logNonReuse   = 'logNonReuse,
        _logCode       = 'logCode,
        _excludeFields = 'excludeFields,
        _except1       = null,
        _exceptN       = null,
      )
    }

  /** Same as [[derive]] but with all debugging options enabled. */
  inline def deriveDebug[A]: Reusability[A] =
    derive(logNonReuse = true, logCode = true)

  /** Same as [[derive]] but with debugging options.
    *
    * @param logNonReuse Log to the console when and why non-reusable values are detected
    * @param logCode Log to generated Scala code to the screen on compilation.
    */
  inline def deriveDebug[A](inline logNonReuse: Boolean,
                            inline logCode    : Boolean): Reusability[A] =
    derive(
      logNonReuse = logNonReuse,
      logCode     = logCode,
    )

  /** Generate an instance for a case class by comparing each case field except those specified.
    *
    * Example:
    * ```
    * case class Picture(id: Long, url: String, title: String)
    *
    * implicit val picReuse = Reusability.caseClassExcept[Picture]("url", "title")
    * ```
    *
    * @tparam A The case class type.
    */
  inline def caseClassExcept[A](inline field1: String, inline fieldN: String*): Reusability[A] =
    ${
      ReusabilityMacros.derive[A](
        _logNonReuse   = 'false,
        _logCode       = 'false,
        _excludeFields = null,
        _except1       = 'field1,
        _exceptN       = 'fieldN,
      )
    }

  /** Same as [[caseClassExcept]] but with all debugging options enabled. */
  inline def caseClassExceptDebug[A](inline field1: String, inline fieldN: String*): Reusability[A] =
    ${
      ReusabilityMacros.derive[A](
        _logNonReuse   = 'true,
        _logCode       = 'true,
        _excludeFields = null,
        _except1       = 'field1,
        _exceptN       = 'fieldN,
      )
    }

  /** Same as [[caseClassExcept]] but with debugging options.
    *
    * @param logNonReuse Log to the console when and why non-reusable values are detected
    * @param logCode Log to generated Scala code to the screen on compilation.
    */
  inline def caseClassExceptDebug[A](inline logNonReuse: Boolean,
                                     inline logCode: Boolean)
                                    (inline field1: String, inline fieldN: String*): Reusability[A] =
    ${
      ReusabilityMacros.derive[A](
        _logNonReuse   = 'logNonReuse,
        _logCode       = 'logCode,
        _excludeFields = null,
        _except1       = 'field1,
        _exceptN       = 'fieldN,
      )
    }

}

// =====================================================================================================================

object ReusabilityMacros {

  def derive[A](_logNonReuse  : Expr[Boolean],
                _logCode      : Expr[Boolean],
                _excludeFields: Null | Expr[String],
                _except1      : Null | Expr[String],
                _exceptN      : Null | Expr[Seq[String]],
                )
               (using Quotes, Type[A]): Expr[Reusability[A]] = {

    val logNonReuse = _logNonReuse.valueOrError
    val logCode     = _logCode    .valueOrError

    val fieldExclusions = {
      var s = Set.empty[String]
      if _excludeFields != null then
        s ++= _excludeFields.valueOrError.split(',').iterator.map(_.trim).filter(_.nonEmpty)
      if _except1 != null then
        s += _except1.valueOrError
      if _exceptN != null then
        s ++= _exceptN.valueOrError
      FieldExclusions(s)
    }

    val result =
      Preparations(logCode = logCode) { preparations =>
        deriveInner[A](
          preparations    = preparations,
          fieldExclusions = fieldExclusions,
          logNonReuse     = logNonReuse,
        )
      }

    fieldExclusions.failUnused[A]()

    result
  }

  private object FieldExclusions {
    val empty = new FieldExclusions(Set.empty)
  }

  private class FieldExclusions(exclusions: Set[String]) {
    private var unused = exclusions

    def nonEmpty = exclusions.nonEmpty

    def apply(fs: List[Field]): List[Field] =
      if nonEmpty
      then fs.filter(filter)
      else fs

    val filter: Field => Boolean =
      f => {
        val exclude = exclusions.contains(f.name)
        if exclude then unused -= f.name
        !exclude
      }

    def failUnused[A]()(using Quotes, Type[A]): Unit =
      if unused.nonEmpty then {
        val fs = unused.toList.sorted.map("\"" + _ + "\"").mkString(", ")
        val err = s"Failed to derive a Reusability instance for ${Type.show[A]}: Specified fields $fs don't exist."
        quotes.reflect.report.throwError(err)
      }
  }

  private class Preparations(logCode: Boolean)(using q: Quotes) {
    import q.reflect.*

    private val init1  = Init("a" + _)
    private val init2  = Init("b" + _, Flags.Lazy)
    private val lazies = List.newBuilder[Statement]
    private var preps  = Map.empty[TypeRepr, Prepared[_]]
    private val stable = collection.mutable.HashMap.empty[TypeRepr, Expr[Reusability[Any]]]

    private def normalise[A](using t: Type[A]): TypeRepr =
      TypeRepr.of[A].dealias

    def addNow[A: Type](expr: Expr[Reusability[A]]): Prepared[A] = {
      val vd = init1.valDef(expr, extraFlags = Flags.Implicit)
      val p  = Prepared[A](Type.of[A], vd.ref, None)
      preps  = preps.updated(normalise[A], p)
      p
    }

    def addDeferred[A: Type](complete: => Expr[Reusability[A]]): Prepared[A] = {
      import Flags.*
      val name    = init1.newName()
      val theVar  = init1.valDef('{new Reusability[A](null)}, name = name, extraFlags = Mutable)
      val theVal  = init1.valDef('{Reusability.suspend(${theVar.ref})}, name = s"_$name", extraFlags = Implicit | Lazy, onInit = false)
      lazies     += theVal.valDef
      lazy val ac = theVar.assign(complete)
      val p       = Prepared[A](Type.of[A], theVar.ref, Some(() => ac))
      preps       = preps.updated(normalise[A], p)
      p
    }

    /** @param typ Just `A`, not `Reusable[A]` */
    def get[A](using t: Type[A]): Option[Prepared[A]] = {
      val t = normalise[A]
      preps.get(t).map(_.subst[A])
    }

    /** @param typ Just `A`, not `Reusable[A]` */
    def need[A](using t: Type[A]): Prepared[A] =
      get[A].getOrElse(throw new IllegalStateException(s"Prepared type for ${normalise[A].show} not found!"))

    def getOrSummonLater[A](using t: Type[A]): Expr[Reusability[A]] =
      get[A] match {
        case Some(p) => p.varRef
        case None    => Expr.summonLater[Reusability[A]]
      }

    def getStablisedImplicitInstance[A: Type]: Expr[Reusability[A]] = {
      def target: Expr[Reusability[A]] =
        get[A] match {
          case Some(p) => p.varRef
          case None    => init2.valDef(Expr.summonLater[Reusability[A]]).ref
        }
      stable
        .getOrElseUpdate(TypeRepr.of[A], target.asExprOfFAny)
        .asExprOfF[A]
    }

    def stabliseInstance[A: Type](e: Expr[Reusability[A]]): Expr[Reusability[A]] =
      init2.valDef(e).ref

    def result[A: Type](finalResult: Prepared[A]): Expr[Reusability[A]] = {
      init1 ++= lazies.result()
      val result: Expr[Reusability[A]] =
        init1.wrapExpr {
          init2.wrapExpr {
            val allPreps = preps.valuesIterator.flatMap(_.complete.map(_())).toList
            Expr.block(allPreps, finalResult.varRef)
          }
        }
      if (logCode)
        println(s"\nDerived ${result.showType}:\n${result.show.replace(".apply(","(").replace("scala.", "")}\n")
      result
    }
  }

  private object Preparations {
    def apply[A: Type](logCode: Boolean)(f: Preparations => Prepared[A])(using Quotes): Expr[Reusability[A]] = {
      val preparations = new Preparations(logCode)
      val prepared     = f(preparations)
      preparations.result(prepared)
    }
  }

  /** @param typ Just `A`, not `Reusable[A]` */
  private case class Prepared[A](typ: Type[A], varRef: Expr[Reusability[A]], complete: Option[() => Expr[Unit]]) {
    def subst[B] = this.asInstanceOf[Prepared[B]]
  }

  // ===================================================================================================================

  private def deriveInner[A: Type](preparations   : Preparations,
                                   fieldExclusions: FieldExclusions,
                                   logNonReuse    : Boolean,
                                  )(using Quotes): Prepared[A] =
    Expr.summonOrError[Mirror.Of[A]] match {

      case '{ $m: Mirror.ProductOf[A] } =>
        deriveInnerCaseClass[A](
          preparations    = preparations,
          fieldExclusions = fieldExclusions,
          fields          = Fields.fromMirror(m),
          logNonReuse     = logNonReuse,
        )

      case '{ $m: Mirror.SumOf[A] } =>
        deriveInnerSumType[A](
          preparations = preparations,
          mirror       = m,
          derive       = true,
          logNonReuse  = logNonReuse,
        )
    }

  private def deriveInnerSumType[A: Type](preparations: Preparations,
                                          mirror      : Expr[Mirror.SumOf[A]],
                                          derive      : Boolean,
                                          logNonReuse : Boolean,
                                         )(using Quotes): Prepared[A] = {

    type Test = (A, A) => Boolean

    def mkTest[B: Type](e: Expr[Reusability[B]], stablise: Boolean): Expr[Test] = {
      val instance =
        if stablise
        then preparations.stabliseInstance(Expr.summonLater[Reusability[B]])
        else e
      '{ (a: A, b: A) => $instance.test(a.asInstanceOf[B], b.asInstanceOf[B]) }
    }


    val fields            = Fields.fromMirror(mirror)
    val fieldCount        = fields.size
    val nonRecursiveCases = Array.fill[Option[Expr[Test]]](fieldCount)(None)

    for (f <- fields) {
      import f.{Type => F, typeInstance}
      Expr.summon[Reusability[F]] match {
        case Some(rf) =>
          val test = mkTest[F](rf, stablise = true)
          nonRecursiveCases(f.idx) = Some(test)
        case None =>
          if derive then
            deriveInner[F](
              preparations    = preparations,
              fieldExclusions = FieldExclusions.empty,
              logNonReuse     = logNonReuse,
            )
          else {
            Expr.summonOrError[Reusability[F]]
            ???
          }
      }
    }

    preparations.addDeferred[A] {

      val tests = Array.fill[Expr[Test]](fieldCount)(null)

      for (f <- fields) {
        import f.{Type => F, idx => i, typeInstance}
        val test: Expr[Test] =
          nonRecursiveCases(i).getOrElse {
            if derive then {
              val p = preparations.need[F]
              mkTest[F](p.varRef, stablise = false)
            } else {
              Expr.summonOrError[Reusability[F]]
              ???
            }
          }
        tests(i) = test
      }

      val testArray = MacroUtils.mkArrayExpr(tests.toIndexedSeq)

      '{
        val m = $mirror
        val tests = $testArray
        Reusability[A] { (a, b) =>
          val o = m.ordinal(a)
          (o == m.ordinal(b)) && tests(o)(a, b)
        }
      }
    }
  }

  private def deriveInnerCaseClass[A: Type](preparations   : Preparations,
                                            fields         : List[Field],
                                            fieldExclusions: FieldExclusions,
                                            logNonReuse    : Boolean,
                                           )(using Quotes): Prepared[A] =
    fieldExclusions(fields) match {

      case Nil =>
        preparations.addNow[A] {
          '{ Reusability.always[A] }
        }

      case f :: Nil if !logNonReuse =>
        import f.{Type => F, typeInstance}
        preparations.addDeferred[A] {
          val imp = preparations.getOrSummonLater[F]
          '{ Reusability.by[A, F](a => ${f.onProduct('a)})($imp) }
        }

      case filteredFields =>
        preparations.addDeferred[A] {
          import quotes.reflect.*

          var tests                = Vector.empty[Expr[(A, A) => Boolean]]
          var testsLoggingNonReuse = Vector.empty[Expr[(A, A) => Unit]]
          val failures             = typedValDef[List[String]]("failures", Flags.Mutable)('{Nil})

          for (f <- filteredFields) {
            import f.{Type => F, typeInstance}
            val fp = preparations.getStablisedImplicitInstance[F]

            val test = '{ (a: A, b: A) => $fp.test(${f.onProduct('a)}, ${f.onProduct('b)}) }
            tests :+= test

            if logNonReuse then
              testsLoggingNonReuse :+= '{ (a: A, b: A) =>
                if !${test('a, 'b)} then
                  ${failures.modify(fs => '{ $fs :+ ${nonReuseDesc(s".${f.name} values not reusable", 'a, 'b)}})}
              }
          }

          if logNonReuse then {
            val header = nonReuseHeader[A]
            '{
              Reusability[A]((a, b) =>
                ${ failures.use(f => '{
                  ${ testsLoggingNonReuse.iterator.map(_('a, 'b)).reduce((x, y) => '{$x; $y}) }
                  if $f.nonEmpty then
                    println($f.sorted.mkString($header, "\n", ""))
                  $f.isEmpty
                })}
              )
            }
          } else
            '{
              Reusability[A]((a, b) =>
                ${ tests.iterator.map(_('a, 'b)).reduce((x, y) => '{$x && $y}) }
              )
            }
        }
    }

  private def nonReuseHeader[A: Type](using Quotes): Expr[String] =
    Expr.inlineConst(s"Instance of ${Type.show[A]} not-reusable for the following reasons:\n")

  private def nonReuseDesc[A: Type](desc: String, a: Expr[A], b: Expr[A])(using Quotes): Expr[String] = {
    val msg1 = Expr.inlineConst(desc + "\n  A: ")
    val msg2 = Expr.inlineConst("\n  B: ")
    '{ "  " + ($msg1 + $a + $msg2 + $b).replace("\n", "\n  ") }
  }

}
