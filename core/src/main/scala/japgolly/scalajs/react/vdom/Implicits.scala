package japgolly.scalajs.react.vdom

import scala.scalajs.js
import japgolly.scalajs.react._
import Scalatags._

abstract class LowPri {
  @inline implicit final def _react_fragSeq   [A <% Frag](xs: Seq[A])   : Frag = SeqFrag(xs)
  @inline implicit final def _react_fragOption[A <% Frag](xs: Option[A]): Frag = SeqFrag(xs.toSeq)
  @inline implicit final def _react_fragArray [A <% Frag](xs: Array[A]) : Frag = SeqFrag[A](xs.toSeq)
}

// If you're wondering why abstract class instead of trait, https://issues.scala-lang.org/browse/SI-4767
abstract class Implicits extends LowPri {

  // Attributes
  @inline implicit final def _react_attrString   : AttrValue[String]          = stringAttrX
          implicit final val _react_attrBoolean  : AttrValue[Boolean]         = GenericAttr[Boolean]
          implicit final val _react_attrByte     : AttrValue[Byte]            = GenericAttr[Byte]
          implicit final val _react_attrShort    : AttrValue[Short]           = GenericAttr[Short]
          implicit final val _react_attrInt      : AttrValue[Int]             = GenericAttr[Int]
          implicit final val _react_attrLong     : AttrValue[Long]            = GenericAttr[Long]
          implicit final val _react_attrFloat    : AttrValue[Float]           = GenericAttr[Float]
          implicit final val _react_attrDouble   : AttrValue[Double]          = GenericAttr[Double]
          implicit final val _react_attrJsThisFn : AttrValue[js.ThisFunction] = GenericAttr[js.ThisFunction]
          implicit final val _react_attrJsFn     : AttrValue[js.Function]     = GenericAttr[js.Function]
          implicit final val _react_attrJsObj    : AttrValue[js.Object]       = GenericAttr[js.Object]

  @inline implicit final def _react_attrRef[T <: Ref[_]]: AttrValue[T] =
    new GenericAttr[T](_.name)

  @inline implicit final def _react_attrOption[A](implicit tc: AttrValue[A]): AttrValue[Option[A]] =
    new OptionalAttrValue[Option, A](tc, _ foreach _)

  @inline implicit final def _react_attrJsUndef[A](implicit tc: AttrValue[A]): AttrValue[js.UndefOr[A]] =
    new OptionalAttrValue[js.UndefOr, A](tc, _ foreach _)

  // Styles
  @inline implicit final def _react_styleString : StyleValue[String]  = stringStyleX
          implicit final val _react_styleBoolean: StyleValue[Boolean] = GenericStyle.stringValue
          implicit final val _react_styleByte   : StyleValue[Byte]    = GenericStyle.stringValue
          implicit final val _react_styleShort  : StyleValue[Short]   = GenericStyle.stringValue
          implicit final val _react_styleInt    : StyleValue[Int]     = GenericStyle.stringValue
          implicit final val _react_styleLong   : StyleValue[Long]    = GenericStyle.stringValue
          implicit final val _react_styleFloat  : StyleValue[Float]   = GenericStyle.stringValue
          implicit final val _react_styleDouble : StyleValue[Double]  = GenericStyle.stringValue

  @inline implicit final def _react_styleOption[A](implicit tc: StyleValue[A]): StyleValue[Option[A]] =
    new OptionalStyleValue[Option, A](tc, _ foreach _)

  @inline implicit final def _react_styleJsUndef[A](implicit tc: StyleValue[A]): StyleValue[js.UndefOr[A]] =
    new OptionalStyleValue[js.UndefOr, A](tc, _ foreach _)

  // Frag
  @inline implicit final def _react_fragReactNode[T <% ReactNode](v: T): Frag = new ReactNodeFrag(v)

  // TagMod
  @inline implicit final def _react_nodeSeq    [A <% TagMod](xs: Seq[A])       : TagMod = new SeqNode(xs)
  @inline implicit final def _react_nodeArray  [A <% TagMod](xs: Array[A])     : TagMod = new SeqNode[A](xs.toSeq)
  @inline implicit final def _react_nodeOption [A <% TagMod](xs: Option[A])    : TagMod = new SeqNode(xs.toSeq)
  @inline implicit final def _react_nodeJsUndef[A <% TagMod](xs: js.UndefOr[A]): TagMod = new SeqNode(xs.toList)

  // Scalatags misc
  @inline implicit final def _react_styleOrdering                  : Ordering[Style] = Scalatags.styleOrdering
  @inline implicit final def _react_attrOrdering                   : Ordering[Attr]  = Scalatags.attrOrdering
  @inline implicit final def _react_cssNumber    [T: Numeric](t: T): CssNumber       = new CssNumber(t)

  // Rendering
  @inline implicit final def _react_autoRender (t: ReactTag)     : ReactElement      = t.render
  @inline implicit final def _react_autoRenderS(t: Seq[ReactTag]): Seq[ReactElement] = t.map(_.render)

  // Extensions
  @inline implicit final def _react_ext_attr(a: Attr)    = new Extra.AttrExt(a)
  @inline implicit final def _react_ext_bool(a: Boolean) = new Extra.BooleanExt(a)
}

object Implicits extends Implicits