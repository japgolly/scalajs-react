package japgolly.scalajs.react.vdom

import scala.annotation.implicitNotFound
import scala.scalajs.js
import japgolly.scalajs.react._
import Scalatags._

class ReactTagOf[+N <: TopNode] private[vdom](
  val tag:       String,
  val modifiers: List[Seq[TagMod]],
  val namespace: Namespace) extends DomFrag {

  def copy(tag: String                  = this.tag,
           modifiers: List[Seq[TagMod]] = this.modifiers,
           namespace: Namespace         = this.namespace): ReactTagOf[N] =
    new ReactTagOf(tag, modifiers, namespace)

  def render: ReactElement = {
    val b = new Builder()
    build(b)
    b.render(tag)
  }

  /**
   * Walks the [[modifiers]] to apply them to a particular [[Builder]].
   * Super sketchy/procedural for max performance.
   */
  private[this] def build(b: Builder): Unit = {
    var current = modifiers
    val arr = new Array[Seq[TagMod]](modifiers.length)

    var i = 0
    while(current != Nil){
      arr(i) = current.head
      current =  current.tail
      i += 1
    }

    var j = arr.length
    while (j > 0) {
      j -= 1
      val frag = arr(j)
      var i = 0
      while(i < frag.length){
        frag(i).applyTo(b)
        i += 1
      }
    }
  }

  def apply(xs: TagMod*): ReactTagOf[N] =
    this.copy(modifiers = xs :: modifiers)

  override def toString = render.toString
}

/**
 * Wraps up a HTML attribute in a value which isn't a string.
 */
final case class Attr(name: String) {
  Escaping.assertValidAttrName(name)

  def :=[T](v: T)(implicit ev: AttrValue[T]): TagMod = AttrPair(this, v, ev)
}

case class ClassNameAttr[T](t: T, av: AttrValue[T]) extends TagMod {
  override def applyTo(b: Builder): Unit =
    av.apply(t, b.addClassName)
}
object ClassNameAttr {
  def :=[T](t: T)(implicit av: AttrValue[T]): ClassNameAttr[T] = ClassNameAttr(t, av)
}

object RefAttr {
  private val ref = "ref".attr

  def :=[T](v: T)(implicit ev: AttrValue[T]): TagMod =
    AttrPair(ref, v, ev)

  import Implicits._react_attrJsFn
  def apply[N <: TopNode](f: N => Unit): TagMod =
    :=((f: js.Function1[N, Unit]): js.Function)
}

/**
 * Wraps up a CSS style in a value.
 */
case class Style(jsName: String, cssName: String) {
  // val s2 = camelCase(s); Style(s2, s2)
  def :=[T](v: T)(implicit ev: StyleValue[T]): TagMod = StylePair(this, v, ev)
}

/**
 * Used to specify how to handle a particular type [[T]] when it is used as
 * the value of a [[Attr]]. Only types with a specified [[AttrValue]] may
 * be used.
 */
@implicitNotFound(
  "No AttrValue defined for type ${T}; scalatags does not know how to use ${T} as an attribute"
)
trait AttrValue[T]{
  def apply(v: T, b: js.Any => Unit): Unit
}

/**
 * Used to specify how to handle a particular type [[T]] when it is used as
 * the value of a [[Style]]. Only types with a specified [[StyleValue]] may
 * be used.
 */
@implicitNotFound(
  "No StyleValue defined for type ${T}; scalatags does not know how to use ${T} as an style"
)
trait StyleValue[T]{
  def apply(t: Builder, s: Style, v: T): Unit
}

private[vdom] object Scalatags {

  /**
   * Marker sub-type of [[TagMod]] which signifies that that type can be
   * rendered as a standalone fragment of [[ReactNode]]. This excludes things
   * like [[AttrPair]]s or [[StylePair]]s which only make sense as part of
   * a parent fragment
   */
  trait Frag extends TagMod {
    def render: ReactNode
  }

  trait DomFrag extends Frag {
    def applyTo(b: Builder): Unit = b.appendChild(this.render)
  }

  /**
   * An [[Attr]], it's associated value, and an [[AttrValue]] of the correct type
   */
  case class AttrPair[T](a: Attr, t: T, av: AttrValue[T]) extends TagMod {
    override def applyTo(b: Builder): Unit =
      av.apply(t, b.addAttr(a.name, _))
  }

  /**
   * A [[Style]], it's associated value, and a [[StyleValue]] of the correct type
   */
  case class StylePair[T](s: Style, v: T, ev: StyleValue[T]) extends TagMod {
    override def applyTo(t: Builder): Unit = {
      ev.apply(t, s, v)
    }
  }

  /**
   * Represents a single XML namespace. This is currently ignored in `scalatags.Text`,
   * but used to create elements with the correct namespace in `scalatags.JsDom`. A
   * [[Namespace]] can be provided implicitly (or explicitly) when creating tags via
   * `"".tag`, with a default of "http://www.w3.org/1999/xhtml" if none is found.
   */
  trait Namespace {
    def uri: String
  }
  object NamespaceHtml {
    implicit val implicitNamespace = new Namespace {
      def uri = "http://www.w3.org/1999/xhtml"
    }
  }
  object NamespaceSvg {
    implicit val implicitNamespace = new Namespace {
      def uri = "http://www.w3.org/2000/svg"
    }
  }

  def camelCase(dashedString: String) = {
    val first :: rest = dashedString.split("-").toList
    (first :: rest.map(s => s(0).toUpper.toString + s.drop(1))).mkString
  }

  implicit object styleOrdering extends Ordering[Style] {
    override def compare(x: Style, y: Style): Int = x.cssName compareTo y.cssName
  }

  implicit object attrOrdering extends Ordering[Attr]{
    override def compare(x: Attr, y: Attr): Int = x.name compareTo y.name
  }

  final class OptionalAttrValue[T[_], A](ot: OptionLike[T], v: AttrValue[A]) extends AttrValue[T[A]] {
    override def apply(ta: T[A], b: js.Any => Unit): Unit = ot.foreach(ta)(v(_, b))
  }

  final class OptionalStyleValue[T[_], A](ot: OptionLike[T], v: StyleValue[A]) extends StyleValue[T[A]] {
    override def apply(b: Builder, s: Style, t: T[A]) = ot.foreach(t)(v(b, s, _))
  }

  @inline def makeAbstractReactTag[N <: TopNode](tag: String, namespaceConfig: Namespace): ReactTagOf[N] = {
    Escaping.assertValidTag(tag)
    new ReactTagOf[N](tag, Nil, namespaceConfig)
  }

  implicit final class SeqFrag[A <% Frag](xs: Seq[A]) extends Frag {
    def applyTo(t: Builder): Unit = xs.foreach(_.applyTo(t))
    override def render: ReactElement = {
      val b = new Builder()
      applyTo(b)
      b.render("")
    }
  }

  implicit val stringAttrX: AttrValue[String] = GenericAttr[String]
  implicit val stringStyleX: StyleValue[String] = GenericStyle.stringValue[String]

  final case class ReactNodeFrag(v: ReactNode) extends DomFrag {
    def render: ReactNode = v
  }

  final class GenericAttr[T](f: T => js.Any) extends AttrValue[T] {
    def apply(v: T, b: js.Any => Unit): Unit = b(f(v))
  }
  object GenericAttr {
    @inline def apply[T <% js.Any] = new GenericAttr[T](a => a)
  }

  final class ArrayAttr[T <% js.Any] extends AttrValue[js.Array[T]] {
    def apply(v: js.Array[T], b: js.Any => Unit): Unit = b(v.map(a => a: js.Any))
  }

  final class GenericStyle[T](f: T => String) extends StyleValue[T] {
    def apply(b: Builder, s: Style, v: T): Unit = {
      b.addStyle(s.cssName, f(v))
    }
  }
  object GenericStyle {
    @inline def stringValue[T]: GenericStyle[T] = new GenericStyle[T](_.toString)
  }

  implicit class STStringExt(private val s: String) extends AnyVal {
    /**
     * Converts the string to a [[ReactTagOf]]
     */
    def tag[N <: TopNode](implicit namespaceConfig: Namespace): ReactTagOf[N] =
      makeAbstractReactTag(s, namespaceConfig)

    /**
     * Converts the string to a void [[ReactTagOf]]; that means that they cannot
     * contain any content, and can be rendered as self-closing tags.
     */
    def voidTag[N <: TopNode](implicit namespaceConfig: Namespace): ReactTagOf[N] =
      makeAbstractReactTag(s, namespaceConfig)

    /**
     * Converts the string to a [[Attr]]
     */
    def attr = Attr(s)

    /**
     * Converts the string to a [[Style]]. The string is used as the cssName of the
     * style, and the jsName of the style is generated by converted the dashes
     * to camelcase.
     */
    def style = Style(s, s)
  }

  /**
   * Allows you to modify a [[ReactTagOf]] by adding a Seq containing other nest-able
   * objects to its list of children.
   */
  implicit class SeqNode[A <% TagMod](xs: Seq[A]) extends TagMod {
    def applyTo(t: Builder) = xs.foreach(_.applyTo(t))
  }
}

