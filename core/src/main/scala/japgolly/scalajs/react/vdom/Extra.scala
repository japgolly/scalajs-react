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

  final class CompositeAttr[A](k: ReactAttr, f: (A, List[A]) => A, e: => TagMod) {
    def apply(as: Option[A]*)(implicit ev: ReactAttr.ValueType[A]): TagMod =
      as.toList.filter(_.isDefined).map(_.get) match {
        case h :: t => k := f(h, t)
        case Nil    => e
      }
  }

  final class AttrExt(private val attr: ReactAttr) extends AnyVal {
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
    @inline def ?=(m: => ReactNode): ReactNode = if (b) m else null
  }

  final class StringExt(private val s: String) extends AnyVal {
    @inline def reactAttr: ReactAttr =
      new ReactAttr.Generic(s)

    @inline def reactStyle: ReactStyle =
      new ReactStyle.Generic(s)

    @inline def reactTag[N <: TopNode]: ReactTagOf[N] =
      makeAbstractReactTag(s, NamespaceHtml.implicitNamespace)
  }

  trait Tags {
    import NamespaceHtml._
    final lazy val big      = "big".tag[dom.html.Element]
    final lazy val dialog   = "dialog".tag[dom.html.Element]
    final lazy val menuitem = "menuitem".tag[dom.html.Element]
  }

  trait Attrs {
    final lazy val className     = ReactAttr.ClassName
    final lazy val cls           = className
    final lazy val `class`       = className

    final lazy val colSpan       = "colSpan".attr
    final lazy val rowSpan       = "rowSpan".attr
    final lazy val htmlFor       = "htmlFor".attr   // same as `for`
    final      val ref           = ReactAttr.Ref
    final      val key           = "key".attr
    final lazy val draggable     = "draggable".attr

    final lazy val onBeforeInput       = "onBeforeInput".attr
    final lazy val onCompositionEnd    = "onCompositionEnd".attr
    final lazy val onCompositionStart  = "onCompositionStart".attr
    final lazy val onCompositionUpdate = "onCompositionUpdate".attr
    final lazy val onContextMenu       = "onContextMenu".attr
    final lazy val onCopy              = "onCopy".attr
    final lazy val onCut               = "onCut".attr
    final lazy val onDrag              = "onDrag".attr
    final lazy val onDragStart         = "onDragStart".attr
    final lazy val onDragEnd           = "onDragEnd".attr
    final lazy val onDragEnter         = "onDragEnter".attr
    final lazy val onDragOver          = "onDragOver".attr
    final lazy val onDragLeave         = "onDragLeave".attr
    final lazy val onDragExit          = "onDragExit".attr
    final lazy val onDrop              = "onDrop".attr
    final lazy val onInput             = "onInput".attr
    final lazy val onPaste             = "onPaste".attr
    final lazy val onWheel             = "onWheel".attr

    final lazy val acceptCharset     = "acceptCharset".attr
    final lazy val accessKey         = "accessKey".attr
    final lazy val allowFullScreen   = "allowFullScreen".attr
    final lazy val allowTransparency = "allowTransparency".attr
    final lazy val async             = "async".attr
    final lazy val autoCapitalize    = "autoCapitalize".attr
    final lazy val autoCorrect       = "autoCorrect".attr
    final lazy val autoPlay          = "autoPlay".attr
    final lazy val cellPadding       = "cellPadding".attr
    final lazy val cellSpacing       = "cellSpacing".attr
    final lazy val classID           = "classID".attr
    final lazy val contentEditable   = "contentEditable".attr
    final lazy val contextMenu       = "contextMenu".attr
    final lazy val controls          = "controls".attr
    final lazy val coords            = "coords".attr
    final lazy val crossOrigin       = "crossOrigin".attr
    final lazy val dateTime          = "dateTime".attr
    final lazy val defer             = "defer".attr
    final lazy val defaultValue      = "defaultValue".attr
    final lazy val dir               = "dir".attr
    final lazy val download          = "download".attr
    final lazy val encType           = "encType".attr
    final lazy val formAction        = "formAction".attr
    final lazy val formEncType       = "formEncType".attr
    final lazy val formMethod        = "formMethod".attr
    final lazy val formNoValidate    = "formNoValidate".attr
    final lazy val formTarget        = "formTarget".attr
    final lazy val frameBorder       = "frameBorder".attr
    final lazy val headers           = "headers".attr
    final lazy val hrefLang          = "hrefLang".attr
    final lazy val icon              = "icon".attr
    final lazy val itemProp          = "itemProp".attr
    final lazy val itemScope         = "itemScope".attr
    final lazy val itemType          = "itemType".attr
    final lazy val list              = "list".attr
    final lazy val loop              = "loop".attr
    final lazy val manifest          = "manifest".attr
    final lazy val marginHeight      = "marginHeight".attr
    final lazy val marginWidth       = "marginWidth".attr
    final lazy val maxLength         = "maxLength".attr
    final lazy val mediaGroup        = "mediaGroup".attr
    final lazy val multiple          = "multiple".attr
    final lazy val muted             = "muted".attr
    final lazy val noValidate        = "noValidate".attr
    final lazy val open              = "open".attr
    final lazy val poster            = "poster".attr
    final lazy val preload           = "preload".attr
    final lazy val radioGroup        = "radioGroup".attr
    final lazy val sandbox           = "sandbox".attr
    final lazy val scope             = "scope".attr
    final lazy val scrolling         = "scrolling".attr
    final lazy val seamless          = "seamless".attr
    final lazy val selected          = "selected".attr
    final lazy val shape             = "shape".attr
    final lazy val sizes             = "sizes".attr
    final lazy val srcDoc            = "srcDoc".attr
    final lazy val srcSet            = "srcSet".attr
    final lazy val step              = "step".attr
    final lazy val useMap            = "useMap".attr
    final lazy val wmode             = "wmode".attr

    final lazy val dangerouslySetInnerHtmlAttr = "dangerouslySetInnerHTML".attr
    final def dangerouslySetInnerHtml(html: String): TagMod = {
      lazy val o: js.Object = js.Dynamic.literal("__html" -> html)
      dangerouslySetInnerHtmlAttr := o
    }

    final def compositeAttr[A](k: ReactAttr, f: (A, List[A]) => A, e: => TagMod = EmptyTag) =
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
    ReactAttr.ClassName.:=(v)(ReactAttr.ValueType.string)

  private[vdom] def classSetImpl(z: TagMod, ps: Seq[(String, Boolean)]): TagMod =
    ps.foldLeft(z)((q, p) => if (p._2) q + cls_=(p._1) else q)
}
