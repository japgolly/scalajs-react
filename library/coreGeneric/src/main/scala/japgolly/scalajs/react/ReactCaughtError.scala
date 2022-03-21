package japgolly.scalajs.react

import scala.scalajs.js
import scala.scalajs.js.JavaScriptException

/** The data captured by React when it catches an error for the `componentDidCatch` event.
  *
  * @param rawError The JS error. Usually a [[js.Error]].
  *                 If you threw an error using `throw js.JavaScriptException("OMG")` then is value is just
  *                 the argument `"OMG"`.
  * @since 1.6.0
  */
final case class ReactCaughtError(rawError: js.Any, rawInfo: facade.React.ErrorInfo) {

  override def toString: String =
    s"ReactCaughtError($rawErrorString)"

  def rawErrorString: String =
    try "" + rawError catch {case _: Throwable => ""}

  @inline def componentStack: String =
    rawInfo.componentStack

  def dynError: js.Dynamic =
    rawError.asInstanceOf[js.Dynamic]

  val typeOfError: String =
    js.typeOf(rawError)

  def stackTraceElements: Array[StackTraceElement] =
    (rawError: Any) match {
      case e: JavaScriptException => e.getStackTrace
      case t: Throwable           => t.getStackTrace
      case _                      => Array.empty
    }

  def stack: String =
    stackTraceElements.iterator.map(_.toString).filter(_.nonEmpty).mkString("\n")

  val jsError: Either[Any, js.Error] =
    rawError match {
      case e: js.Error => Right(e)
      case a           => Left(a)
    }

  def name: String =
    jsError.fold(_ => typeOfError, _.name)

  def message: String =
    jsError.fold(_ => rawErrorString, _.message)
}
