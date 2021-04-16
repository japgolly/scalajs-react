package japgolly.scalajs.react

import japgolly.scalajs.react.internal.{ReusabilityMacros => M}

trait ReusabilityMacros {

  /** Generate an instance for A.
    *
    * If A is a sealed trait or sealed abstract class, Reusability is determined by sub-class reusability (which will
    * be derived when it doesn't exist).
    *
    * If A is a case class, Reusability is determined by each field's Reusability.
    */
  def derive[A]: Reusability[A] =
    macro M.derive[A]

  /** Same as [[derive]] but with all debugging options enabled. */
  def deriveDebug[A]: Reusability[A] =
    macro M.deriveDebug[A]

  /** Same as [[derive]] but with debugging options.
    *
    * @param logNonReuse Log to the console when and why non-reusable values are detected
    * @param logCode Log to generated Scala code to the screen on compilation.
    */
  def deriveDebug[A](logNonReuse: Boolean, logCode: Boolean): Reusability[A] =
    macro M.deriveDebugWithArgs[A]

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
  def caseClassExcept[A](field1: String, fieldN: String*): Reusability[A] =
    macro M.caseClassExcept[A]

  /** Same as [[caseClassExcept]] but with all debugging options enabled. */
  def caseClassExceptDebug[A](field1: String, fieldN: String*): Reusability[A] =
    macro M.caseClassExceptDebug[A]

  /** Same as [[caseClassExcept]] but with debugging options.
    *
    * @param logNonReuse Log to the console when and why non-reusable values are detected
    * @param logCode Log to generated Scala code to the screen on compilation.
    */
  def caseClassExceptDebug[A](logNonReuse: Boolean, logCode: Boolean)
                             (field1: String, fieldN: String*): Reusability[A] =
    macro M.caseClassExceptDebugWithArgs[A]

}
