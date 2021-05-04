package japgolly.scalajs.react

import japgolly.microlibs.macro_utils.MacroUtils
import japgolly.scalajs.react.internal.NewMacroUtils
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

    val fieldExclusions: Set[String] = {
      var s = Set.empty[String]
      if _excludeFields != null then
        s ++= _excludeFields.valueOrError.split(',').iterator.map(_.trim).filter(_.nonEmpty)
      if _except1 != null then
        s += _except1.valueOrError
      if _exceptN != null then
        s ++= _exceptN.valueOrError
      s
    }

    var invalidFieldExclusions = fieldExclusions

    var result: Expr[Reusability[A]] = null
    def log(msg: => Any) = if logCode then println(msg) //catch {case t: Throwable => t.printStackTrace}
    log("="*120)
    log(s"Beginning derivation of Reusability[${Type.show[A]}]")
    log(s"Field exclusions: ${fieldExclusions.size}")
    if fieldExclusions.nonEmpty then
      log(fieldExclusions.toArray.sortInPlace.iterator.map("  - " + _).mkString("\n"))

    type Clause = MacroUtils.Fn2Clause[A, A, Boolean]

    def newInstance(f: Clause): Quotes ?=> Expr[Reusability[A]] =
      '{ new Reusability[A]((x, y) => ${f('x, 'y)}) }

    Expr.summon[Mirror.Of[A]] match {

      // Product
      case Some('{ $m: Mirror.ProductOf[A] { type MirroredElemTypes = types } }) =>

        var fields = NewMacroUtils.mirrorFields(m)

        if fieldExclusions.nonEmpty then
          fields = fields.filter { f =>
            val exclude = fieldExclusions.contains(f.name)
            if exclude then invalidFieldExclusions -= f.name
            !exclude
          }

        result = NewMacroUtils.withCachedGivens[Reusability, Reusability[A]](fields) { lookup =>

          val clauses =
            fields.map[Clause](f => (x, y) => {
              val r = lookup(f)
              '{ $r.test(${f.onProduct(x)}, ${f.onProduct(y)}) }
            })

          MacroUtils.mergeFn2s(
            fs    = clauses,
            empty = Left(Expr(true)),
            merge = (x, y) => '{ $x && $y },
            outer = newInstance,
          )
        }

      // Sum
      case Some('{ $m: Mirror.SumOf[A] { type MirroredElemTypes = types } }) =>
        result = MacroUtils.buidTypeClassForSum[Reusability, A](m) { b =>
          newInstance { (x, y) => '{
              val o = ${b.ordinal(x)}
              (o == ${b.ordinal(y)}) && ${b.tc('o)}.test($x, $y)
            }
          }
        }

      case _ =>
    }

    if result == null then {
      val err = s"Don't know how to derive a Reusability instance for ${Type.show[A]}: Mirror not found."
      log(err)
      log("="*120)
      quotes.reflect.report.throwError(err)
    } else if invalidFieldExclusions.nonEmpty then {
      val fs = invalidFieldExclusions.toList.sorted.map("\"" + _ + "\"").mkString(", ")
      val err = s"Failed to derive a Reusability instance for ${Type.show[A]}: Specified fields $fs don't exist."
      log(err)
      log("="*120)
      quotes.reflect.report.throwError(err)
    } else {
      log(result.show)
      log("="*120)
      result
    }
  }

}
