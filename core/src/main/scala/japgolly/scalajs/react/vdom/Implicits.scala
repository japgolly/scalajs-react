package japgolly.scalajs.react.vdom

import scala.scalajs.js
import japgolly.scalajs.react._
import Scalatags._

abstract class LowPri {
  @inline implicit final def _react_fragSeq   [A <% Frag](xs: Seq[A])   : Frag = SeqFrag(xs)
  @inline implicit final def _react_fragArray [A <% Frag](xs: Array[A]) : Frag = SeqFrag[A](xs.toSeq)

//  @inline implicit final def _react_fragOption[A <% Frag](xs: Option[A]): Frag = SeqFrag(xs.toSeq)
//  @inline implicit final def _react_fragOptionLike[T[_], A](t: T[A])(implicit o: OptionLike[T], f: A => Frag): Frag =
//    o.fold(t, f, js.native)
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

  implicit final def _react_attrJsDictionary[A]: AttrValue[js.Dictionary[A]] =
    new GenericAttr[js.Dictionary[A]](d => d.asInstanceOf[js.Object])

  @inline implicit final def _react_attrRef[R <: Ref]: AttrValue[R] =
    new GenericAttr[R](_.name)

  @inline implicit final def _react_attrOptionLike[T[_], A](implicit t: OptionLike[T], a: AttrValue[A]): AttrValue[T[A]] =
    new OptionalAttrValue[T, A](t, a)

  @inline implicit final def _react_attrArray[A <% js.Any]: AttrValue[js.Array[A]] =
    new ArrayAttr[A]

  // Styles
  @inline implicit final def _react_styleString : StyleValue[String]  = stringStyleX
          implicit final val _react_styleBoolean: StyleValue[Boolean] = GenericStyle.stringValue
          implicit final val _react_styleByte   : StyleValue[Byte]    = GenericStyle.stringValue
          implicit final val _react_styleShort  : StyleValue[Short]   = GenericStyle.stringValue
          implicit final val _react_styleInt    : StyleValue[Int]     = GenericStyle.stringValue
          implicit final val _react_styleLong   : StyleValue[Long]    = GenericStyle.stringValue
          implicit final val _react_styleFloat  : StyleValue[Float]   = GenericStyle.stringValue
          implicit final val _react_styleDouble : StyleValue[Double]  = GenericStyle.stringValue

  @inline implicit final def _react_styleOptionLike[O[_], A](implicit O: OptionLike[O], a: StyleValue[A]): StyleValue[O[A]] =
    new OptionalStyleValue[O, A](O, a)

  // Frag
  @inline implicit final def _react_fragReactNode[T <% ReactNode](v: T): Frag = new ReactNodeFrag(v)

  // TagMod
  @inline implicit final def _react_nodeSeq  [A <% TagMod](xs: Seq[A])      : TagMod = new SeqNode(xs)
  @inline implicit final def _react_nodeArray[A <% TagMod](xs: Array[A])    : TagMod = new SeqNode[A](xs.toSeq)
  @inline implicit final def _react_nodeOptionLike[O[_], A](o: O[A])(implicit O: OptionLike[O], f: A => TagMod): TagMod =
    O.fold(o, EmptyTag)(f)

  // Scalatags misc
  @inline implicit final def _react_styleOrdering                  : Ordering[Style] = Scalatags.styleOrdering
  @inline implicit final def _react_attrOrdering                   : Ordering[Attr]  = Scalatags.attrOrdering
  @inline implicit final def _react_cssNumber    [T: Numeric](t: T): CssNumber       = new CssNumber(t)

  // Rendering
  @inline implicit final def _react_autoRender [T <: TopNode](t: ReactTagOf[T])     : ReactElement      = t.render
  @inline implicit final def _react_autoRenderS[T <: TopNode](t: Seq[ReactTagOf[T]]): Seq[ReactElement] = t.map(_.render)

  // Extensions
  @inline implicit final def _react_ext_attr(a: Attr)    = new Extra.AttrExt(a)
  @inline implicit final def _react_ext_bool(a: Boolean) = new Extra.BooleanExt(a)
  @inline implicit final def _react_ext_str (a: String)  = new Extra.StringExt(a)
}

object Implicits extends Implicits
