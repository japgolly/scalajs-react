package japgolly.scalajs.react.vdom

import org.scalajs.dom
import scala.annotation.implicitNotFound
import scala.scalajs.js
import japgolly.scalajs.react._
import Scalatags._
import Implicits._

@implicitNotFound("You are passing a CallbackTo[${A}] to a DOM event handler which is most likely a mistake."
  + "\n  If the result is irrelevant, add `.void`."
  + "\n  If the result is necessary, please raise an issue and use `vdom.DomCallbackResult.force` in the meantime.")
sealed trait DomCallbackResult[A]
object DomCallbackResult {
  def force[A] = null.asInstanceOf[DomCallbackResult[A]]
  @inline implicit def unit = force[Unit]
  @inline implicit def boolean = force[Boolean]
  @inline implicit def undefOrBoolean = force[js.UndefOr[Boolean]]
}

object Extra {

  final class CompositeAttr[A](k: Attr, f: (A, List[A]) => A, e: => TagMod) {
    def apply(as: Option[A]*)(implicit ev: AttrValue[A]): TagMod =
      as.toList.filter(_.isDefined).map(_.get) match {
        case h :: t => k := f(h, t)
        case Nil => e
      }
  }

  final class AttrExt(private val attr: Attr) extends AnyVal {
    def -->[A: DomCallbackResult](callback: => CallbackTo[A]): TagMod =
      // not using callback.toJsFn or similar because we don't want to force evaluate of callback
      attr := ((() => callback.runNow()): js.Function)

    def ==>[A: DomCallbackResult, N <: dom.Node, E <: SyntheticEvent[N]](eventHandler: E => CallbackTo[A]): TagMod =
      attr := (((e: E) => eventHandler(e).runNow()): js.Function)

    def -->?[O[_]](callback: => O[Callback])(implicit o: OptionLike[O]): TagMod =
      attr --> Callback(o.foreach(callback)(_.runNow()))

    def ==>?[O[_], N <: dom.Node, E <: SyntheticEvent[N]](eventHandler: E => O[Callback])(implicit o: OptionLike[O]): TagMod =
      attr.==>[Unit, N, E](e => Callback(o.foreach(eventHandler(e))(_.runNow())))
  }

  final class BooleanExt(private val b: Boolean) extends AnyVal {
    @inline def ?=(m: => TagMod): TagMod = if (b) m else EmptyTag
  }

  final class StringExt(private val s: String) extends AnyVal {
    @inline def reactAttr : Attr     = Attr(s)
    @inline def reactStyle: Style    = Style(s, s)
    @inline def reactTag[N <: TopNode]: ReactTagOf[N] = makeAbstractReactTag(s, Scalatags.NamespaceHtml.implicitNamespace)
  }

  trait Tags {
    import NamespaceHtml._
    final val big      = "big".tag[dom.html.Element]
    final val dialog   = "dialog".tag[dom.html.Element]
    final val menuitem = "menuitem".tag[dom.html.Element]
  }

  trait Attrs {
    final val className     = ClassNameAttr
    final val cls           = className
    final val `class`       = className

    final val colSpan       = "colSpan".attr
    final val rowSpan       = "rowSpan".attr
    final val htmlFor       = "htmlFor".attr   // same as `for`
    final val ref           = RefAttr
    final val key           = "key".attr
    final val draggable     = "draggable".attr

    final val onBeforeInput       = "onBeforeInput".attr
    final val onCompositionEnd    = "onCompositionEnd".attr
    final val onCompositionStart  = "onCompositionStart".attr
    final val onCompositionUpdate = "onCompositionUpdate".attr
    final val onContextMenu       = "onContextMenu".attr
    final val onCopy              = "onCopy".attr
    final val onCut               = "onCut".attr
    final val onDrag              = "onDrag".attr
    final val onDragStart         = "onDragStart".attr
    final val onDragEnd           = "onDragEnd".attr
    final val onDragEnter         = "onDragEnter".attr
    final val onDragOver          = "onDragOver".attr
    final val onDragLeave         = "onDragLeave".attr
    final val onDragExit          = "onDragExit".attr
    final val onDrop              = "onDrop".attr
    final val onInput             = "onInput".attr
    final val onPaste             = "onPaste".attr
    final val onWheel             = "onWheel".attr

    final val acceptCharset     = "acceptCharset".attr
    final val accessKey         = "accessKey".attr
    final val allowFullScreen   = "allowFullScreen".attr
    final val allowTransparency = "allowTransparency".attr
    final val async             = "async".attr
    final val autoCapitalize    = "autoCapitalize".attr
    final val autoCorrect       = "autoCorrect".attr
    final val autoPlay          = "autoPlay".attr
    final val cellPadding       = "cellPadding".attr
    final val cellSpacing       = "cellSpacing".attr
    final val classID           = "classID".attr
    final val contentEditable   = "contentEditable".attr
    final val contextMenu       = "contextMenu".attr
    final val controls          = "controls".attr
    final val coords            = "coords".attr
    final val crossOrigin       = "crossOrigin".attr
    final val dateTime          = "dateTime".attr
    final val defer             = "defer".attr
    final val defaultValue      = "defaultValue".attr
    final val dir               = "dir".attr
    final val download          = "download".attr
    final val encType           = "encType".attr
    final val formAction        = "formAction".attr
    final val formEncType       = "formEncType".attr
    final val formMethod        = "formMethod".attr
    final val formNoValidate    = "formNoValidate".attr
    final val formTarget        = "formTarget".attr
    final val frameBorder       = "frameBorder".attr
    final val headers           = "headers".attr
    final val hrefLang          = "hrefLang".attr
    final val icon              = "icon".attr
    final val itemProp          = "itemProp".attr
    final val itemScope         = "itemScope".attr
    final val itemType          = "itemType".attr
    final val list              = "list".attr
    final val loop              = "loop".attr
    final val manifest          = "manifest".attr
    final val marginHeight      = "marginHeight".attr
    final val marginWidth       = "marginWidth".attr
    final val maxLength         = "maxLength".attr
    final val mediaGroup        = "mediaGroup".attr
    final val multiple          = "multiple".attr
    final val muted             = "muted".attr
    final val noValidate        = "noValidate".attr
    final val open              = "open".attr
    final val poster            = "poster".attr
    final val preload           = "preload".attr
    final val radioGroup        = "radioGroup".attr
    final val sandbox           = "sandbox".attr
    final val scope             = "scope".attr
    final val scrolling         = "scrolling".attr
    final val seamless          = "seamless".attr
    final val selected          = "selected".attr
    final val shape             = "shape".attr
    final val sizes             = "sizes".attr
    final val srcDoc            = "srcDoc".attr
    final val srcSet            = "srcSet".attr
    final val step              = "step".attr
    final val useMap            = "useMap".attr
    final val wmode             = "wmode".attr

    final val dangerouslySetInnerHtmlAttr = "dangerouslySetInnerHTML".attr
    final def dangerouslySetInnerHtml(html: String): TagMod = {
      val o: js.Object = js.Dynamic.literal("__html" -> html)
      dangerouslySetInnerHtmlAttr := o
    }

    final def compositeAttr[A](k: Attr, f: (A, List[A]) => A, e: => TagMod = EmptyTag) =
      new CompositeAttr(k, f, e)

    final def classSet(ps: (String, Boolean)*): TagMod =
      classSetImpl(EmptyTag, ps)

    final def classSet1(a: String, ps: (String, Boolean)*): TagMod =
      classSetImpl(cls_=(a), ps)

    final def classSetM(ps: Map[String, Boolean]): TagMod =
      classSetImpl(EmptyTag, ps.toSeq)

    final def classSet1M(a: String, ps: Map[String, Boolean]): TagMod =
      classSetImpl(cls_=(a), ps.toSeq)
  }

  @inline private[vdom] def cls_=(v:  String): TagMod =
    ClassNameAttr.:=(v)(stringAttrX)

  private[vdom] def classSetImpl(z: TagMod, ps: Seq[(String, Boolean)]): TagMod =
    ps.foldLeft(z)((q, p) => if (p._2) q + cls_=(p._1) else q)
}