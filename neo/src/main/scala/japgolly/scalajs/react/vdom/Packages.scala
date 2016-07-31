package japgolly.scalajs.react.vdom

abstract class PackageBase extends Exports with Implicits
object PackageBase extends PackageBase

trait HtmlAttrAndStyles extends HtmlAttrs with HtmlStyles
object HtmlAttrAndStyles extends HtmlAttrAndStyles

object all extends PackageBase with HtmlAttrAndStyles {
//  object svg extends SvgTags with SvgAttrs
  @inline def keyAttr = key
//  @inline def refAttr = ref
}

object html_<^ extends PackageBase {
  val < = HtmlTags
  val ^ = HtmlAttrAndStyles
}

object svg_<^ extends PackageBase {
// @inline def < = SvgTags
// @inline def ^ = SvgAttrs
}
