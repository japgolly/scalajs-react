package japgolly.scalajs.react.vdom

import scala.scalajs.js
import japgolly.scalajs.react._
import Scalatags._

import scala.scalajs.js.Any

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
  @inline implicit final def _react_attrString   : Attr.ValueType[String         ] = Attr.ValueType.string
          implicit final val _react_attrBoolean  : Attr.ValueType[Boolean        ] = Attr.ValueType.map
          implicit final def _react_attrByte     : Attr.ValueType[Byte           ] = Attr.ValueType.map
          implicit final def _react_attrShort    : Attr.ValueType[Short          ] = Attr.ValueType.map
          implicit final val _react_attrInt      : Attr.ValueType[Int            ] = Attr.ValueType.map
          implicit final val _react_attrLong     : Attr.ValueType[Long           ] = Attr.ValueType.map
          implicit final def _react_attrFloat    : Attr.ValueType[Float          ] = Attr.ValueType.map
          implicit final val _react_attrDouble   : Attr.ValueType[Double         ] = Attr.ValueType.map
          implicit final val _react_attrJsThisFn : Attr.ValueType[js.ThisFunction] = Attr.ValueType.map
          implicit final val _react_attrJsFn     : Attr.ValueType[js.Function    ] = Attr.ValueType.map
          implicit final val _react_attrJsObj    : Attr.ValueType[js.Object      ] = Attr.ValueType.map

  implicit final def _react_attrJsDictionary[A]: Attr.ValueType[js.Dictionary[A]] =
    Attr.ValueType.map(d => d.asInstanceOf[js.Object])

  @inline implicit final def _react_attrRef[R <: Ref]: Attr.ValueType[R] =
    Attr.ValueType.map(_.name)

  @inline implicit final def _react_attrOptionLike[T[_], A](implicit o: OptionLike[T], t: Attr.ValueType[A]): Attr.ValueType[T[A]] =
    Attr.ValueType.optional(o, t)

  @inline implicit final def _react_attrArray[A](implicit f: A => Any): Attr.ValueType[js.Array[A]] =
    Attr.ValueType.array(f)

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
  @inline implicit final def _react_attrOrdering                   : Ordering[Attr]  = Attr.ordering
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
