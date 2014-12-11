package japgolly.scalajs.react.vdom

import org.scalajs.dom
import scala.annotation.unchecked.uncheckedVariance
import scala.scalajs.js
import scalatags._
import scalatags.generic._
import japgolly.scalajs.react._

object ReactVDom
    extends Bundle[VDomBuilder, ReactElement, ReactFragT]
    with Aliases[VDomBuilder, ReactElement, ReactFragT] {

  object attrs extends ReactVDom.Cap with Attrs with ExtraAttrs2
  object tags extends ReactVDom.Cap with ReactTags
  object tags2 extends ReactVDom.Cap with ReactTags2
  object styles extends ReactVDom.Cap with Styles
  object styles2 extends ReactVDom.Cap with Styles2
  object svgTags extends ReactVDom.Cap with ReactSvgTags
  object svgAttrs extends ReactVDom.Cap with SvgAttrs

  object implicits extends Aggregate

  object all
      extends Cap
      with Attrs
      with Styles
      with ReactTags
      with DataConverters
      with Aggregate
      with ExtraAttrs2

  object short
      extends Cap
      with Util
      with DataConverters
      with AbstractShort
      with Aggregate
      with ExtraAttrs2 {
    object * extends Cap with Attrs with Styles
  }

  override type Tag = ReactVDom.TypedTag[ReactElement]

  trait CustomFunctions {
    implicit final val ___jsThisFnAttr              = new GenericAttr[js.ThisFunction](f => f)
    implicit final val ___jsFnAttr                  = new GenericAttr[js.Function](f => f)
    implicit final val ___jsObjAttr                 = new GenericAttr[js.Object](f => f)
    implicit final def ___reactRefAttr[T <: Ref[_]] = new GenericAttr[T](_.name)

    implicit final def ___reactNodeAsDomChild[T <% ReactNode](c: T): Modifier = new Modifier {
      override def applyTo(t: VDomBuilder): Unit = t.appendChild(c)
    }

    implicit final def ___autoRender(t: Tag)      : ReactElement      = t.render
    implicit final def ___autoRenderS(t: Seq[Tag]): Seq[ReactElement] = t.map(_.render)
  }

  sealed trait Aggregate extends generic.Aggregate[VDomBuilder, ReactElement, ReactFragT] with CustomFunctions {
    override def genericAttr[T] = new GenericAttr[T](a => a.toString)
    override def genericStyle[T] = new GenericStyle[T]

    override implicit def stringFrag(v: String) = new ReactVDom.StringFrag(v)

    override val RawFrag = ReactVDom.RawFrag
    override val StringFrag = ReactVDom.StringFrag
    override type StringFrag = ReactVDom.StringFrag
    override type RawFrag = ReactVDom.RawFrag
    override def raw(s: String) = RawFrag(s)

    override type Tag = ReactVDom.Tag

    private def genericJsAttr[T <% js.Any]: GenericAttr[T] = new GenericAttr[T](a => a)
    override implicit val booleanAttr: GenericAttr[Boolean] = genericJsAttr[Boolean]
    override implicit val byteAttr   : GenericAttr[Byte]    = genericJsAttr[Byte]
    override implicit val shortAttr  : GenericAttr[Short]   = genericJsAttr[Short]
    override implicit val intAttr    : GenericAttr[Int]     = genericJsAttr[Int]
    override implicit val longAttr   : GenericAttr[Long]    = genericJsAttr[Long]
    override implicit val floatAttr  : GenericAttr[Float]   = genericJsAttr[Float]
    override implicit val doubleAttr : GenericAttr[Double]  = genericJsAttr[Double]
  }

  trait Cap extends Util { self =>
    type ConcreteHtmlTag[T <: ReactElement] = TypedTag[T]

    protected[this] implicit val stringAttrX: AttrValue[String] = new GenericAttr[String](s => s)
    protected[this] implicit val stringStyleX: StyleValue[String] = new GenericStyle[String]

    def makeAbstractTypedTag[T <: ReactElement](tag: String, void: Boolean, namespaceConfig: Namespace): TypedTag[T] =
      TypedTag(tag, Nil, void, namespaceConfig)

    /*override*/ implicit final class SeqFrag[A <% Frag](xs: Seq[A]) extends Frag{
      def applyTo(t: VDomBuilder): Unit = xs.foreach(_.applyTo(t))
      def render: ReactElement = {
        val b = new VDomBuilder()

        applyTo(b)

        b.render("")
      }
    }
  }

  import implicits._

  object StringFrag extends Companion[StringFrag]
  final case class StringFrag(v: String) extends ReactDomFrag {
    def render: ReactFragT = v
  }

  object RawFrag extends Companion[RawFrag]
  final case class RawFrag(v: String) extends Modifier {
    def render: ReactFragT = v
    def applyTo(t: VDomBuilder): Unit = t.appendChild(this.render)
  }

  final class GenericAttr[T](f: T => js.Any) extends AttrValue[T]{
    def apply(t: VDomBuilder, a: Attr, v: T): Unit =
      t.addAttr(a.name, f(v))
  }

  final class GenericStyle[T] extends StyleValue[T]{
    def apply(b: VDomBuilder, s: Style, v: T): Unit = {
      b.addStyle(s.cssName, v.toString)
    }
  }

  final case class TypedTag[+Output <: ReactElement](tag: String = "",
                                                    modifiers: List[Seq[Modifier]],
                                                    void: Boolean = false,
                                                    namespace: Namespace)
                     extends generic.TypedTag[VDomBuilder, Output, ReactFragT]
                     with ReactDomFrag {
    // unchecked because Scala 2.10.4 seems to not like this, even though
    // 2.11.1 works just fine. I trust that 2.11.1 is more correct than 2.10.4
    // and so just force this.
    protected[this] type Self = TypedTag[Output @uncheckedVariance]

    def render: Output = {
      val b = new VDomBuilder()
      build(b)
      b.render(tag).asInstanceOf[Output]
    }

    def apply(xs: Modifier*): TypedTag[Output] =
      this.copy(tag = tag, void = void, modifiers = xs :: modifiers)

    /** Converts an ScalaTag fragment into an html string */
    override def toString = render.toString
  }

  val EmptyTag: Modifier = new Modifier {
    override def applyTo(t: VDomBuilder): Unit = ()
  }

  sealed trait ReactDomFrag extends generic.Frag[VDomBuilder, ReactFragT]{
    def render: ReactFragT
    def applyTo(b: VDomBuilder): Unit = b.appendChild(this.render)
  }

  trait ExtraAttrs extends Util {
    val colspan = "colSpan".attr
    val rowspan = "rowSpan".attr
    val className = "className".attr
    val htmlFor = "htmlFor".attr
    val ref = "ref".attr
    val key = "key".attr
    val draggable     = "draggable".attr
    val onDragStart   = "onDragStart".attr
    val onDragEnd     = "onDragEnd".attr
    val onDragEnter   = "onDragEnter".attr
    val onDragOver    = "onDragOver".attr
    val onDragLeave   = "onDragLeave".attr
    val onDrop        = "onDrop".attr
    val onBeforeInput = "onBeforeInput".attr

    val dangerouslySetInnerHtmlAttr = "dangerouslySetInnerHTML".attr
    def dangerouslySetInnerHtml(html: String): Modifier = {
      val o: js.Object = js.Dynamic.literal("__html" -> html)
      dangerouslySetInnerHtmlAttr := o
    }

    final def compositeAttr[A](k: Attr, f: (A, List[A]) => A, e: => Modifier = EmptyTag) =
      new CompositeAttr(k, f, e)

    final val classSwitch = compositeAttr[String](all.cls, (h,t) => (h::t) mkString " ")

    final def classSet(ps: (String, Boolean)*): Modifier =
      classSwitch(ps.map(p => if (p._2) Some(p._1) else None): _*)(stringAttrX)

    final def classSet1(a: String, ps: (String, Boolean)*): Modifier =
      classSet(((a, true) +: ps):_*)

    final def classSetM(ps: Map[String, Boolean]): Modifier =
      classSet(ps.toSeq: _*)

    final def classSet1M(a: String, ps: Map[String, Boolean]): Modifier =
      classSet1(a, ps.toSeq: _*)
  }

  trait ExtraAttrs2 extends ExtraAttrs {
    final def keyAttr = key
    final def refAttr = ref
  }

  final class CompositeAttr[A](k: Attr, f: (A, List[A]) => A, e: => Modifier) {
    def apply(as: Option[A]*)(implicit ev: AttrValue[A]): Modifier =
      as.toList.filter(_.isDefined).map(_.get) match {
        case h :: t => k := f(h, t)
        case Nil => e
      }
  }

  implicit final class ReactVExt_Attr(val a: Attr) extends AnyVal {
    @inline def runs(thunk: => Unit) = a := ((() => thunk): js.Function)
    @inline def -->(thunk: => Unit) = a runs thunk
    @inline def ==>[N <: dom.Node, E <: SyntheticEvent[N]](eventHandler: E => Unit) = a := (eventHandler: js.Function)
  }

  implicit final class ReactVExt_Boolean(val a: Boolean) extends AnyVal {
    @inline def &&(m: => Modifier): Modifier = if (a) m else EmptyTag
    // @inline def :=>[V](v: => V): Option[V] = if (a) Some(v) else None
  }
}
