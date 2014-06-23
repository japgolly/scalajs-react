package golly.react.scalatags

import scala.annotation.unchecked.uncheckedVariance
import scala.scalajs.js
import scalatags._
import scalatags.generic._

object ReactDom extends Bundle[ReactBuilder, ReactOutput, ReactFragT] {

  object all extends StringTags with Attrs with Styles with ReactTags with DataConverters
  object short extends StringTags with Util with DataConverters with generic.AbstractShort[ReactBuilder, ReactOutput, ReactFragT]{
    object * extends StringTags with Attrs with Styles
  }

  object attrs extends StringTags with Attrs
  object tags extends StringTags with ReactTags
  object tags2 extends StringTags with ReactTags2
  object styles extends StringTags with Styles
  object styles2 extends StringTags with Styles2
  object svgTags extends StringTags with ReactSvgTags
  object svgStyles extends StringTags with SvgStyles


  trait StringTags extends Util{ self =>
    type ConcreteHtmlTag[T <: ReactOutput] = TypedTag[T]

    protected[this] implicit def stringAttr = new GenericAttr[String]
    protected[this] implicit def stringStyle = new GenericStyle[String]

    def makeAbstractTypedTag[T <: ReactOutput](tag: String, void: Boolean): TypedTag[T] = {
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
  case class StringFrag(v: String) extends Frag {
    def render: ReactFragT = v
  }

  def raw(s: String) = new RawFrag(s)

  object RawFrag extends Companion[RawFrag]
  case class RawFrag(v: String) extends Modifier {
    def render: ReactFragT = v
    def applyTo(t: ReactBuilder): Unit = t.appendChild(this.render)
  }

  class GenericAttr[T] extends AttrValue[T]{
    def apply(t: ReactBuilder, a: Attr, v: T): Unit = {
      t.addAttr(a.name, v.toString)
    }
  }
  implicit val stringAttr = new GenericAttr[String]
  implicit val booleanAttr= new GenericAttr[Boolean]
  implicit def numericAttr[T: Numeric] = new GenericAttr[T]

  class GenericStyle[T] extends StyleValue[T]{
    def apply(b: ReactBuilder, s: Style, v: T): Unit = {
      b.addStyle(s.cssName, v.toString)
    }
  }
  implicit val stringStyle = new GenericStyle[String]
  implicit val booleanStyle = new GenericStyle[Boolean]
  implicit def numericStyle[T: Numeric] = new GenericStyle[T]

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

    def apply(xs: Modifier*): TypedTag[Output] = {
      this.copy(tag=tag, void = void, modifiers = xs :: modifiers)
    }

    /**
     * Converts an ScalaTag fragment into an html string
     */
    override def toString = (render:js.Any).toString

  }
  type Tag = TypedTag[ReactOutput]
  val Tag = TypedTag

  type Frag = ReactDomFrag
  trait ReactDomFrag extends generic.Frag[ReactBuilder, ReactOutput, ReactFragT]{
    def render: ReactFragT
    def applyTo(b: ReactBuilder): Unit = b.appendChild(this.render)
  }
}
