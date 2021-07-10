package japgolly.scalajs.react

import japgolly.scalajs.react.util.DomUtil._
import japgolly.scalajs.react.util.Util.{catchAll, identityFn}
import org.scalajs.dom
import org.scalajs.dom.html
import scala.scalajs.js.|

sealed trait ComponentDom {
  import ComponentDom._

  def mounted: Option[ComponentDom.Mounted]

  /** unsafe! may throw an exception */
  final def asMounted(): ComponentDom.Mounted =
    mounted getOrElse sys.error("DOM node isn't mounted.")

  def toElement: Option[dom.Element] = None
  def toText: Option[dom.Text] = None

  final def toHtml: Option[html.Element] =
    toElement.flatMap(_.domToHtml)

  final def toNode: Option[dom.Node] =
    mounted.map(_.node)

  /** For testing purposes. */
  final def show(sanitiseHtml: String => String = identityFn): String =
    mounted match {
      case Some(Element(e)) => sanitiseHtml(e.outerHTML)
      case Some(Text(t))    => (catchAll(t.wholeText) orElse catchAll(t.textContent) orElse catchAll(t.innerText)).get
      case None             => ""
    }
}

object ComponentDom {

  def apply(i: facade.ReactDOM.DomNode | Null | Unit): ComponentDom =
    (i: Any) match {
      case e: dom.Element => Element(e)
      case t: dom.Text    => Text(t)
      case null | ()      => Unmounted
    }

  def findDOMNode(a: dom.Element | facade.React.ComponentUntyped): ComponentDom = {
    val b: facade.ReactDOM.DomNode | Null =
      try facade.ReactDOM.findDOMNode(a)
      catch {case _: Throwable => null}
    apply(b)
  }

  case object Unmounted extends ComponentDom {
    override def mounted = None
  }

  sealed trait Mounted extends ComponentDom {
    override def mounted = Some(this)

    def node: dom.Node
    def raw: facade.ReactDOM.DomNode

    /** unsafe! may throw an exception */
    final def asElement(): dom.Element =
      this match {
        case Element(e) => e
        case Text(t)    => sys error s"Expected a dom.Element; got $t"
      }

    /** unsafe! may throw an exception */
    final def domCast[N <: dom.raw.Node]: N =
      asElement().domCast[N]

    /** unsafe! may throw an exception */
    final def asHtml(): html.Element =
      asElement().domCast

    /** unsafe! may throw an exception */
    final def asText(): dom.Text =
      this match {
        case Text(t)    => t
        case Element(e) => sys error s"Expected a dom.Text; got $e"
      }

    def fold[A](text: dom.Text => A, element: dom.Element => A): A
  }

  final case class Element(element: dom.Element) extends Mounted {
    override def toElement = Some(element)
    override def node = element
    override def raw = element
    override def fold[A](text: dom.Text => A, f: dom.Element => A) = f(element)
  }

  final case class Text(text: dom.Text) extends Mounted {
    override def toText = Some(text)
    override def node = text
    override def raw = text
    override def fold[A](f: dom.Text => A, element: dom.Element => A) = f(text)
  }

}
