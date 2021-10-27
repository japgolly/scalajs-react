package japgolly.scalajs.react.util

import org.scalajs.dom
import org.scalajs.dom.html

object DomUtil {

  /** Extensions to plain old DOM. */
  @inline implicit final class ReactExt_DomNode(private val n: dom.Node) extends AnyVal {

    @inline def domCast[N <: dom.Node]: N =
      n.asInstanceOf[N]

    @inline def domAsHtml: html.Element =
      domCast

    def domToHtml: Option[html.Element] =
      n match {
        case e: html.Element => Some(e)
        case _               => None
      }
  }

}

trait DomUtil {
  import DomUtil._

  @inline final implicit def ReactExt_DomNode(n: dom.Node): ReactExt_DomNode =
    new ReactExt_DomNode(n)
}
