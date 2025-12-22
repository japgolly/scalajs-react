package japgolly.scalajs.react

import japgolly.scalajs.react.util.DomUtil._
import japgolly.scalajs.react.util.Util.identityFn
import org.scalajs.dom
import org.scalajs.dom.html
import scala.scalajs.js.|

sealed trait ComponentDom {
  import ComponentDom._

  def mounted: Option[ComponentDom.Mounted]

  /** unsafe! may throw an exception */
  final def asMounted(): ComponentDom.Mounted =
    mounted getOrElse sys.error("DOM node isn't mounted.")

  def toElement: Option[dom.Element] =
    None

  final def toHtml: Option[html.Element] =
    toElement.flatMap(_.domToHtml)

  final def toNode: Option[dom.Node] =
    mounted.map(_.node)

  final def toText: Option[dom.Text] =
    mounted.flatMap {
      case Node(t: dom.Text) => Some(t)
      case _                 => None
    }

  /** For testing purposes. */
  final def show(sanitiseHtml: String => String = identityFn): String =
    mounted match {
      case Some(Element(e)) => sanitiseHtml(e.outerHTML)
      case Some(Node(n))    => n.nodeValue
      case None             => ""
    }
}

object ComponentDom {

  def apply(i: facade.ReactDOM.DomNode | Null | Unit): ComponentDom =
    (i: Any) match {
      case e: dom.Element => Element(e)
      case n: dom.Node    => Node(n)
      case null | ()      => Unmounted
    }

  @deprecated("findDOMNode is deprecated and will be removed in the next major release. Instead, add a ref directly to the element you want to reference.", "3.0.0 / React v18")
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
        case x          => throw new RuntimeException(s"Expected a dom.Element; got ${x.raw}")
      }

    /** unsafe! may throw an exception */
    final def domCast[N <: dom.Node]: N =
      asElement().domCast[N]

    /** unsafe! may throw an exception */
    final def asHtml(): html.Element =
      asElement().domCast

    /** unsafe! may throw an exception */
    final def asText(): dom.Text =
      node match {
        case t: dom.Text => t
        case n           => throw new RuntimeException(s"Expected a dom.Text; got $n")
      }

    @deprecated("Call .node and pattern match as needed", "3.0.0")
    def fold[A](text: dom.Text => A, element: dom.Element => A): A
  }

  final case class Element(element: dom.Element) extends Mounted {
    override def toElement = Some(element)
    override def node = element
    override def raw = element
    @deprecated("Call .node and pattern match as needed", "3.0.0")
    override def fold[A](text: dom.Text => A, f: dom.Element => A) = f(element)
  }

  final case class Node(node: dom.Node) extends Mounted {
    override def raw = node
    @deprecated("Call .node and pattern match as needed", "3.0.0")
    override def fold[A](text: dom.Text => A, element: dom.Element => A) =
      node match {
        case t: dom.Text => text(t)
        case _           => throw new RuntimeException(s"The .fold method is now deprecated and doesn't support non-Text, non-Element nodes")
      }
  }

  @deprecated("Use Node instead", "3.0.0") type Text = Node
  @deprecated("Use Node instead", "3.0.0") val  Text = Node
}
