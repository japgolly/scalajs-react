package scalatags

//import org.scalajs.dom
import scala.scalajs.js

//import org.scalajs.ReactDom
import scala.annotation.unchecked.uncheckedVariance
import scalatags.generic.Modifier



//object ReactWarmup {
//
//  type Builder = js.Any
//  type Output = js.Any
//  type FragT = js.Any
//
//  val h2 : scalatags.generic.TypedTag[Builder, Output, FragT]
//}

//object ReactDomTypes {
//  type ReactBuilder = js.Any
//  type ReactOutput = js.Any
//  type ReactFragT = js.Any
//}
//import ReactDomTypes._

/*
object ReactDom extends generic.Bundle[ReactBuilder, ReactOutput, ReactFragT] with LowPriorityImplicits{
  object all extends StringTags with Attrs with Styles with jsdom.Tags with DataConverters
  object short extends StringTags with Util with DataConverters with generic.AbstractShort[ReactBuilder, ReactOutput, ReactFragT]{
    object * extends StringTags with Attrs with Styles
  }

  object attrs extends StringTags with Attrs
  object tags extends StringTags with jsdom.Tags
  object tags2 extends StringTags with jsdom.Tags2
  object styles extends StringTags with Styles
  object styles2 extends StringTags with Styles2
  object svgTags extends StringTags with jsdom.SvgTags
  object svgStyles extends StringTags with SvgStyles

  trait StringTags extends Util{ self =>
    type ConcreteHtmlTag[T <: ReactDom] = TypedTag[T]

    protected[this] implicit def stringAttr = new GenericAttr[String]
    protected[this] implicit def stringStyle = new GenericStyle[String]

    def makeAbstractTypedTag[T <: ReactDom](tag: String, void: Boolean): TypedTag[T] = {
      TypedTag(tag, Nil, void)
    }
  }

  implicit def byteFrag(v: Byte) = new StringFrag(v.toString)
  implicit def shortFrag(v: Short) = new StringFrag(v.toString)
  implicit def intFrag(v: Int) = new StringFrag(v.toString)
  implicit def longFrag(v: Long) = new StringFrag(v.toString)
  implicit def floatFrag(v: Float) = new StringFrag(v.toString)
  implicit def doubleFrag(v: Double) = new StringFrag(v.toString)
  implicit def stringFrag(v: String) = new StringFrag(v)

  object StringFrag extends Companion[StringFrag]
  case class StringFrag(v: String) extends Frag{
    //def render: dom.Text = dom.document.createTextNode(v)
    def render: dom.Text = dom.document.createTextNode(v)
  }

  def raw(s: String) = new RawFrag(s)

  object RawFrag extends Companion[RawFrag]
  case class RawFrag(v: String) extends Modifier{
    def applyTo(elem: ReactDom): Unit = {
      elem.insertAdjacentHTML("beforeend", v)
    }
  }

  class GenericAttr[T] extends AttrValue[T]{
    def apply(t: ReactDom, a: Attr, v: T): Unit = {
      t.setAttribute(a.name, v.toString)
    }
  }
  implicit def stringAttr = new GenericAttr[String]
  implicit def booleanAttr= new GenericAttr[Boolean]
  implicit def numericAttr[T: Numeric] = new GenericAttr[T]

  class GenericStyle[T] extends StyleValue[T]{
    def apply(t: ReactDom, s: Style, v: T): Unit = {
      t.asInstanceOf[dom.HTMLElement]
       .style
       .setProperty(s.cssName, v.toString)
    }
  }
  implicit def stringStyle = new GenericStyle[String]
  implicit def booleanStyle = new GenericStyle[Boolean]
  implicit def numericStyle[T: Numeric] = new GenericStyle[T]

  case class TypedTag[+Output <: ReactDom](tag: String = "",
                                              modifiers: List[Seq[Modifier]],
                                              void: Boolean = false)
                                              extends generic.TypedTag[ReactDom, Output, ReactNode]
                                              with Frag{
    // unchecked because Scala 2.10.4 seems to not like this, even though
    // 2.11.1 works just fine. I trust that 2.11.1 is more correct than 2.10.4
    // and so just force this.
    protected[this] type Self = TypedTag[Output @uncheckedVariance]

    def render: Output = {
      val elem = dom.document.createElement(tag)
      build(elem)
      elem.asInstanceOf[Output]
    }
    /**
     * Trivial override, not strictly necessary, but it makes IntelliJ happy...
     */
    def apply(xs: Modifier*): TypedTag[Output] = {
      this.copy(tag = tag, void = void, modifiers = xs :: modifiers)
    }
    override def toString = render.outerHTML
  }
//  type HtmlTag = TypedTag[dom.HTMLElement]
//  val HtmlTag = TypedTag
//  type SvgTag = TypedTag[dom.SVGElement]
//  val SvgTag = TypedTag
  type Tag = TypedTag[ReactDom]
  val Tag = TypedTag

  type Frag = DomFrag
  trait DomFrag extends generic.Frag[ReactDom, ReactDom, ReactNode]{
    def render: ReactNode
    def applyTo(b: ReactDom) = b.appendChild(this.render)
  }
}

trait LowPriorityImplicits{
  implicit object bindJsAny extends generic.AttrValue[ReactDom, js.Any]{
    def apply(t: ReactDom, a: generic.Attr, v: js.Any): Unit = {
      t.asInstanceOf[js.Dynamic].updateDynamic(a.name)(v)
    }
  }
  implicit def bindJsAnyLike[T <% js.Any] = new generic.AttrValue[ReactDom, T]{
    def apply(t: ReactDom, a: generic.Attr, v: T): Unit = {
      t.asInstanceOf[js.Dynamic].updateDynamic(a.name)(v)
    }
  }
  implicit class bindElement(e: ReactDom) extends generic.Modifier[ReactDom] {
    override def applyTo(t: Element) = t.appendChild(e)
  }
}
*/