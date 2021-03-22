package japgolly.scalajs.react.vdom

abstract class PackageBase extends Exports with Implicits
object PackageBase extends PackageBase

trait HtmlAttrAndStyles extends HtmlAttrs with HtmlStyles
object HtmlAttrAndStyles extends HtmlAttrAndStyles

trait SvgAttrAndStyles extends SvgAttrs with SvgStyles
object SvgAttrAndStyles extends SvgAttrAndStyles

object all extends PackageBase with HtmlTags with HtmlAttrAndStyles {
  object svg extends SvgTags with SvgAttrAndStyles
  @inline def keyAttr = key
}

object html_<^ extends PackageBase {
  val < = HtmlTags
  val ^ = HtmlAttrAndStyles
}

object svg_<^ extends PackageBase {
  val < = SvgTags
  val ^ = SvgAttrAndStyles
}
