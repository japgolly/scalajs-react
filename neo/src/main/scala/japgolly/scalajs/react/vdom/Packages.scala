package japgolly.scalajs.react.vdom

//object all extends Base with Tags with Attrs {
//  object svg extends SvgTags with SvgAttrs
//  @inline def keyAttr = key
//  @inline def refAttr = ref
//}

//object svg {
//  object all extends Base with SvgTags with SvgAttrs
//}

abstract class PackageBase extends Exports with Implicits
object PackageBase extends PackageBase

object html_<^ extends PackageBase {
//  @inline def < = Tags
  val ^ = HtmlAttrs
}

object svg_<^ extends PackageBase {
// @inline def < = SvgTags
// @inline def ^ = SvgAttrs
}
