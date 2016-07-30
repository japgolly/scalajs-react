package japgolly.scalajs.react.vdom

abstract class PackageBase extends Exports with Implicits
object PackageBase extends PackageBase

object all extends PackageBase with HtmlTags with HtmlAttrs {
//  object svg extends SvgTags with SvgAttrs
//  @inline def keyAttr = key
//  @inline def refAttr = ref
}

object html_<^ extends PackageBase {
  val < = HtmlTags
  val ^ = HtmlAttrs
}

object svg_<^ extends PackageBase {
// @inline def < = SvgTags
// @inline def ^ = SvgAttrs
}
