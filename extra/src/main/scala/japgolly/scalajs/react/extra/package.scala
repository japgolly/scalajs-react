package japgolly.scalajs.react

import org.scalajs.dom
import org.scalajs.dom.html

package object extra {

  type ~=>[-A, +B] = ReusableFn[A, B]

  @inline implicit final class ReactExtrasExt_Any[A](private val self: A) extends AnyVal {
    @inline def ~=~(a: A)(implicit r: Reusability[A]): Boolean = r.test(self, a)
    @inline def ~/~(a: A)(implicit r: Reusability[A]): Boolean = !r.test(self, a)
  }

  /** Extensions to plain old DOM. */
  @inline implicit final class ReactExtrasExt_DomNode(private val n: dom.raw.Node) extends AnyVal {

    @inline def domCast[N <: dom.raw.Node]: N =
      n.asInstanceOf[N]

    @inline def domAsHtml: html.Element =
      domCast

    def domToHtml: Option[html.Element] =
      n match {
        case e: html.Element => Some(e)
        case _ => None
      }
  }

}