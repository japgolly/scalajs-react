package japgolly.scalajs.react.vdom

object AltScalatags {
  import ReactVDom.{Attr, Frag, Modifier => RM, all => A}

  abstract class LowPri {
    // LowPriUtil
    @inline implicit final def ___SeqFrag   [A <% Frag](xs: Seq[A])    = A.SeqFrag(xs)
    @inline implicit final def ___OptionFrag[A <% Frag](xs: Option[A]) = A.OptionFrag(xs)
    @inline implicit final def ___ArrayFrag [A <% Frag](xs: Array[A])  = A.ArrayFrag(xs)
  }

  abstract class Base extends LowPri with ReactVDom.CustomFunctions {

    final type Tag         = ReactVDom.Tag
    final type TagModifier = ReactVDom.Modifier

    @inline final def EmptyTag = ReactVDom.EmptyTag

    // Util
    @inline implicit final def ___SeqNode   [A <% RM](xs: Seq[A])    = new A.SeqNode(xs)
    @inline implicit final def ___OptionNode[A <% RM](xs: Option[A]) = new A.SeqNode(xs.toSeq)
    @inline implicit final def ___ArrayNode [A <% RM](xs: Array[A])  = new A.SeqNode[A](xs.toSeq)
    @inline implicit final def ___UnitNode           (u: Unit)       = A.UnitNode(u)
    // Aggregate
    @inline implicit final def ___stringAttr            = A.stringAttr
    @inline implicit final def ___booleanAttr           = A.booleanAttr
    @inline implicit final def ___byteAttr              = A.byteAttr
    @inline implicit final def ___shortAttr             = A.shortAttr
    @inline implicit final def ___intAttr               = A.intAttr
    @inline implicit final def ___longAttr              = A.longAttr
    @inline implicit final def ___floatAttr             = A.floatAttr
    @inline implicit final def ___doubleAttr            = A.doubleAttr
    @inline implicit final def ___stringStyle           = A.stringStyle
    @inline implicit final def ___booleanStyle          = A.booleanStyle
    @inline implicit final def ___byteStyle             = A.byteStyle
    @inline implicit final def ___shortStyle            = A.shortStyle
    @inline implicit final def ___intStyle              = A.intStyle
    @inline implicit final def ___longStyle             = A.longStyle
    @inline implicit final def ___floatStyle            = A.floatStyle
    @inline implicit final def ___doubleStyle           = A.doubleStyle
    @inline implicit final def ___byteFrag(v: Byte)     = A.byteFrag(v)
    @inline implicit final def ___shortFrag(v: Short)   = A.shortFrag(v)
    @inline implicit final def ___intFrag(v: Int)       = A.intFrag(v)
    @inline implicit final def ___longFrag(v: Long)     = A.longFrag(v)
    @inline implicit final def ___floatFrag(v: Float)   = A.floatFrag(v)
    @inline implicit final def ___doubleFrag(v: Double) = A.doubleFrag(v)
    @inline implicit final def ___stringFrag(v: String) = A.stringFrag(v)
    // ReactVDom
    @inline implicit final def ___ReactVExt_Attr(a: Attr)    = new ReactVDom.ReactVExt_Attr(a)
    @inline implicit final def ___ReactVExt_Bool(a: Boolean) = new ReactVDom.ReactVExt_Boolean(a)
    }

  trait AllHtmlTags extends ReactVDom.Cap with ReactTags with ReactTags2
  trait AllHtmlAttr extends ReactVDom.Cap with ReactVDom.Attrs with ReactVDom.ExtraAttrs with ReactVDom.Styles
  trait AllSvgTags extends ReactVDom.Cap with ReactSvgTags
  trait AllSvgAttr extends ReactVDom.Cap with ReactVDom.SvgAttrs

  object AllHtmlTags extends AllHtmlTags
  object AllHtmlAttr extends AllHtmlAttr
  object AllSvgTags extends AllSvgTags
  object AllSvgAttr extends AllSvgAttr
}

import AltScalatags._

object prefix_<^ extends Base {
  @inline final def  < = AllHtmlTags
  @inline final def  ^ = AllHtmlAttr
  @inline final def *< = AllSvgTags
  @inline final def *^ = AllSvgAttr
}

object prefix_text extends Base {
  @inline final def tags    = AllHtmlTags
  @inline final def attr    = AllHtmlAttr
  @inline final def svgTags = AllSvgTags
  @inline final def svgAttr = AllSvgAttr
}