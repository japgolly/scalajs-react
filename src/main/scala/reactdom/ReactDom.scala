package golly.react.scalatags

import _root_.scalatags._
import _root_.scalatags.generic._
import org.scalajs.dom
import scala.annotation.unchecked.uncheckedVariance
import scala.scalajs.js
import golly.react.{SyntheticEvent, Ref, ProxyConstructorU}

object ReactDom extends Bundle[ReactBuilder, ReactOutput, ReactFragT] {

  object all extends StringTags with Attrs with Styles with ReactTags with DataConverters with ExtraAttrs
  object short extends StringTags with Util with DataConverters with generic.AbstractShort[ReactBuilder, ReactOutput, ReactFragT]{
    object * extends StringTags with Attrs with Styles
  }

  object attrs extends StringTags with Attrs with ExtraAttrs
  object tags extends StringTags with ReactTags
  object tags2 extends StringTags with ReactTags2
  object styles extends StringTags with Styles
  object styles2 extends StringTags with Styles2
  object svgTags extends StringTags with ReactSvgTags
  object svgStyles extends StringTags with SvgStyles

  trait StringTags extends Util{ self =>
    type ConcreteHtmlTag[T <: ReactOutput] = TypedTag[T]

    protected[this] implicit def stringAttr = ReactDom.stringAttr
    protected[this] implicit def stringStyle = ReactDom.stringStyle

    def makeAbstractTypedTag[T <: ReactOutput](tag: String, void: Boolean): TypedTag[T] =
      TypedTag(tag, Nil, void)
  }

  implicit def byteFrag(v: Byte) = new StringFrag(v.toString)
  implicit def shortFrag(v: Short) = new StringFrag(v.toString)
  implicit def intFrag(v: Int) = new StringFrag(v.toString)
  implicit def longFrag(v: Long) = new StringFrag(v.toString)
  implicit def floatFrag(v: Float) = new StringFrag(v.toString)
  implicit def doubleFrag(v: Double) = new StringFrag(v.toString)
  implicit def stringFrag(v: String) = new StringFrag(v)

  object StringFrag extends Companion[StringFrag]
  case class StringFrag(v: String) extends Frag {
    def render: ReactFragT = v
  }

  def raw(s: String) = new RawFrag(s)

  object RawFrag extends Companion[RawFrag]
  case class RawFrag(v: String) extends Modifier {
    def render: ReactFragT = v
    def applyTo(t: ReactBuilder): Unit = t.appendChild(this.render)
  }

  class GenericAttr[T](f: T => js.Any) extends AttrValue[T]{
    def apply(t: ReactBuilder, a: Attr, v: T): Unit =
      t.addAttr(a.name, f(v))
  }
  implicit val stringAttr : AttrValue[String]   = new GenericAttr[String](s => s)
  implicit val booleanAttr  = new GenericAttr[Boolean](b => b)
  implicit val jsThisFnAttr = new GenericAttr[js.ThisFunction](f => f)
  implicit val jsFnAttr = new GenericAttr[js.Function](f => f)
  implicit def numericAttr[T: Numeric] = new GenericAttr[T](_.toString)
  implicit def reactRefAttr[T <: Ref[_]] = new GenericAttr[T](_.name)

  class GenericStyle[T] extends StyleValue[T]{
    def apply(b: ReactBuilder, s: Style, v: T): Unit = {
      b.addStyle(s.cssName, v.toString)
    }
  }
  implicit val stringStyle = new GenericStyle[String]
  implicit val booleanStyle = new GenericStyle[Boolean]
  implicit def numericStyle[T: Numeric] = new GenericStyle[T]

  implicit def proxyConstructorFrag(c: ProxyConstructorU): ReactDom.Modifier = new ReactDom.Modifier {
    override def applyTo(t: ReactBuilder): Unit = t.appendChild(c)
  }

  case class TypedTag[+Output <: ReactOutput](tag: String = "",
                                         modifiers: List[Seq[Modifier]],
                                         void: Boolean = false)
    extends generic.TypedTag[ReactBuilder, Output, ReactFragT]
    with Frag{
    // unchecked because Scala 2.10.4 seems to not like this, even though
    // 2.11.1 works just fine. I trust that 2.11.1 is more correct than 2.10.4
    // and so just force this.
    protected[this] type Self = TypedTag[Output @uncheckedVariance]

    def render: Output = {
      val b = new ReactBuilder()
      build(b)
      b.render(tag).asInstanceOf[Output]
    }

    def apply(xs: Modifier*): TypedTag[Output] =
      this.copy(tag=tag, void = void, modifiers = xs :: modifiers)

    /** Converts an ScalaTag fragment into an html string */
    override def toString = render.toString
  }

  type Tag = TypedTag[ReactOutput]
  val Tag = TypedTag

  type Frag = ReactDomFrag
  trait ReactDomFrag extends generic.Frag[ReactBuilder, ReactOutput, ReactFragT]{
    def render: ReactFragT
    def applyTo(b: ReactBuilder): Unit = b.appendChild(this.render)
  }

  trait ExtraAttrs extends Util {
    val onBlur      = "onBlur"     .attr
    val onChange    = "onChange"   .attr
    val onClick     = "onClick"    .attr
    val onFocus     = "onFocus"    .attr
    val onKeyDown   = "onKeyDown"  .attr
    val onKeyPress  = "onKeyPress" .attr
    val onKeyUp     = "onKeyUp"    .attr
    val onLoad      = "onLoad"     .attr
    val onMouseDown = "onMouseDown".attr
    val onMouseMove = "onMouseMove".attr
    val onMouseOut  = "onMouseOut" .attr
    val onMouseOver = "onMouseOver".attr
    val onMouseUp   = "onMouseUp"  .attr
    val onSelect    = "onSelect"   .attr
    val onScroll    = "onScroll"   .attr
    val onSubmit    = "onSubmit"   .attr
    val onReset     = "onReset"    .attr

    val ref = "ref".attr
  }

  implicit class ReactAttrExt(val a: Attr) extends AnyVal {
    def runs(thunk: => Unit) = a := ((() => thunk): js.Function)
    def ==>[E <: dom.Node, R](eventHandler: SyntheticEvent[E] => R) = a := (eventHandler: js.Function)
  }
}
