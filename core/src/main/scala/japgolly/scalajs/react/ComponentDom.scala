package japgolly.scalajs.react

import japgolly.scalajs.react.{raw => Raw}
import org.scalajs.dom
import org.scalajs.dom.html
import scala.scalajs.js.|

sealed trait ComponentDom {

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

  @deprecated("Either use .toHtml, or call .mounted and safely handle None before .asHtml()", "1.3.0")
  final def domAsHtml: html.Element =
    asMounted().asHtml()

  @deprecated("Use .toHtml", "1.3.0")
  def domToHtml: Option[html.Element] =
    toHtml

  @deprecated("Call .mounted and safely handle None before .raw", "1.3.0")
  def rawDomNode: Raw.ReactDOM.DomNode =
    asMounted().raw

  @deprecated("Use .toText", "1.3.0")
  final def left = toText

  @deprecated("Use .toElement", "1.3.0")
  final def right = toElement

  // These are commented out because the deprecation cascades to usages of the implementations in ComponentDom.Mounted
  // which is not what I want. Those versions are fine.

  //  @deprecated("Either use .toElement, or call .mounted and safely handle None before .asElement", "1.3.0")
  //  def asElement(): dom.Element =
  //    asMounted().asElement()
  //
  //  @deprecated("Either use .toText, or call .mounted and safely handle None before .asText", "1.3.0")
  //  def asText(): dom.Text =
  //    asMounted().asText()
  //
  //  @deprecated("Call .mounted and safely handle None before .domCast", "1.3.0")
  //  def domCast[N <: dom.raw.Node]: N =
  //    asMounted().domCast[N]
  //
  //  @deprecated("Call .mounted and safely handle None before .fold", "1.3.0")
  //  def fold[A](text: dom.Text => A, element: dom.Element => A): A =
  //    asMounted().fold(text, element)
}

object ComponentDom {

  def apply(i: Raw.ReactDOM.DomNode | Null | Unit): ComponentDom =
    (i: Any) match {
      case e: dom.Element => Element(e)
      case t: dom.Text    => Text(t)
      case null | ()      => Unmounted
    }

  def findDOMNode(a: dom.Element | Raw.React.ComponentUntyped): ComponentDom = {
    val b: Raw.ReactDOM.DomNode | Null =
      try Raw.ReactDOM.findDOMNode(a)
      catch {case t: Throwable => null}
    apply(b)
  }

  case object Unmounted extends ComponentDom {
    override def mounted = None
  }

  sealed trait Mounted extends ComponentDom {
    override def mounted = Some(this)

    def node: dom.Node
    def raw: Raw.ReactDOM.DomNode

    @deprecated("Use .raw", "1.3.0")
    final override def rawDomNode: Raw.ReactDOM.DomNode = raw

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
