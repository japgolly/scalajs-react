package japgolly.scalajs.react.vdom

import scala.scalajs.js
import japgolly.scalajs.react._
import Scalatags._

import scala.scalajs.js.Any

abstract class LowPri {
  @inline implicit final def _react_fragSeq   [A <% Frag](xs: Seq[A])   : Frag = new SeqFrag(xs)
  @inline implicit final def _react_fragArray [A <% Frag](xs: Array[A]) : Frag = new SeqFrag[A](xs.toSeq)

//  @inline implicit final def _react_fragOption[A <% Frag](xs: Option[A]): Frag = SeqFrag(xs.toSeq)
//  @inline implicit final def _react_fragOptionLike[T[_], A](t: T[A])(implicit o: OptionLike[T], f: A => Frag): Frag =
//    o.fold(t, f, js.native)
}

// If you're wondering why abstract class instead of trait, https://issues.scala-lang.org/browse/SI-4767
abstract class Implicits extends LowPri {

  // Attributes
  @inline implicit final def _react_attrString   : ReactAttr.ValueType[String         ] = ReactAttr.ValueType.string
          implicit final val _react_attrBoolean  : ReactAttr.ValueType[Boolean        ] = ReactAttr.ValueType.map
          implicit final def _react_attrByte     : ReactAttr.ValueType[Byte           ] = ReactAttr.ValueType.map
          implicit final def _react_attrShort    : ReactAttr.ValueType[Short          ] = ReactAttr.ValueType.map
          implicit final val _react_attrInt      : ReactAttr.ValueType[Int            ] = ReactAttr.ValueType.map
          implicit final val _react_attrLong     : ReactAttr.ValueType[Long           ] = ReactAttr.ValueType.map
          implicit final def _react_attrFloat    : ReactAttr.ValueType[Float          ] = ReactAttr.ValueType.map
          implicit final val _react_attrDouble   : ReactAttr.ValueType[Double         ] = ReactAttr.ValueType.map
          implicit final val _react_attrJsThisFn : ReactAttr.ValueType[js.ThisFunction] = ReactAttr.ValueType.map
          implicit final val _react_attrJsFn     : ReactAttr.ValueType[js.Function    ] = ReactAttr.ValueType.map
          implicit final val _react_attrJsObj    : ReactAttr.ValueType[js.Object      ] = ReactAttr.ValueType.map

  implicit final def _react_attrJsDictionary[A]: ReactAttr.ValueType[js.Dictionary[A]] =
    ReactAttr.ValueType.map(d => d.asInstanceOf[js.Object])

  @inline implicit final def _react_attrRef[R <: Ref]: ReactAttr.ValueType[R] =
    ReactAttr.ValueType.map(_.name)

  @inline implicit final def _react_attrOptionLike[T[_], A](implicit o: OptionLike[T], t: ReactAttr.ValueType[A]): ReactAttr.ValueType[T[A]] =
    ReactAttr.ValueType.optional(o, t)

  @inline implicit final def _react_attrArray[A](implicit f: A => Any): ReactAttr.ValueType[js.Array[A]] =
    ReactAttr.ValueType.array(f)

  // Styles
  @inline implicit final def _react_styleString   : ReactStyle.ValueType[String         ] = ReactStyle.ValueType.string
          implicit final val _react_styleBoolean  : ReactStyle.ValueType[Boolean        ] = ReactStyle.ValueType.stringValue
          implicit final def _react_styleByte     : ReactStyle.ValueType[Byte           ] = ReactStyle.ValueType.stringValue
          implicit final def _react_styleShort    : ReactStyle.ValueType[Short          ] = ReactStyle.ValueType.stringValue
          implicit final val _react_styleInt      : ReactStyle.ValueType[Int            ] = ReactStyle.ValueType.stringValue
          implicit final val _react_styleLong     : ReactStyle.ValueType[Long           ] = ReactStyle.ValueType.stringValue
          implicit final def _react_styleFloat    : ReactStyle.ValueType[Float          ] = ReactStyle.ValueType.stringValue
          implicit final val _react_styleDouble   : ReactStyle.ValueType[Double         ] = ReactStyle.ValueType.stringValue

  @inline implicit final def _react_styleOptionLike[O[_], A](implicit o: OptionLike[O], t: ReactStyle.ValueType[A]): ReactStyle.ValueType[O[A]] =
    ReactStyle.ValueType.optional(o, t)

  // Frag
  @inline implicit final def _react_fragReactNode[T <% ReactNode](v: T): Frag = new ReactNodeFrag(v)

  // Scalatags misc
  @inline implicit final def _react_styleOrdering                  : Ordering[ReactStyle] = ReactStyle.ordering
  @inline implicit final def _react_attrOrdering                   : Ordering[ReactAttr]  = ReactAttr.ordering
  @inline implicit final def _react_cssNumber    [T: Numeric](t: T): CssNumber       = new CssNumber(t)

  // Rendering
  @inline implicit final def _react_autoRender [T <: TopNode](t: ReactTagOf[T])     : ReactElement      = t.render
  @inline implicit final def _react_autoRenderS[T <: TopNode](t: Seq[ReactTagOf[T]]): Seq[ReactElement] = t.map(_.render)

  // Extensions
  @inline implicit final def _react_ext_attr(a: ReactAttr)    = new Extra.AttrExt(a)
  @inline implicit final def _react_ext_bool(a: Boolean) = new Extra.BooleanExt(a)
  @inline implicit final def _react_ext_str (a: String)  = new Extra.StringExt(a)

  // TagMod

  @inline implicit final def _react_nodeSeq[A <% TagMod](xs: Seq[A]): TagMod =
    new SeqNode(xs)

  @inline implicit final def _react_nodeArray[A <% TagMod](xs: Array[A]): TagMod =
    new SeqNode[A](xs.toSeq)

  @inline implicit final def _react_nodeOptionLike[O[_], A](o: O[A])(implicit O: OptionLike[O], f: A => TagMod): TagMod =
    O.fold(o, EmptyTag)(f)
}

object Implicits extends Implicits
