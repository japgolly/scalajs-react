package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.feature.ReactFragment
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.{PropsChildren, raw}
import scala.collection.compat._
import scala.language.`3.0`
import scala.scalajs.js

// =====================================================================================================================

trait ImplicitsForVdomAttr1 extends CssUnitsOps {
  import Attr.ValueType

  implicit lazy val vdomAttrVtInnerHtml: ValueType[String, InnerHtmlAttr] =
    ValueType[String, InnerHtmlAttr]((b, html) => b(js.Dynamic.literal("__html" -> html)))

  implicit def vdomAttrVtKey[A](implicit k: A => raw.React.Key): ValueType[A, Attr.Key] =
    ValueType((b, a) => b(k(a).asInstanceOf[js.Any]))

  implicit val vdomAttrVtKeyL: ValueType[Long, Attr.Key] =
    ValueType((b, a) => b(a.toString))

  // 90% case so reuse
  implicit val vdomAttrVtKeyS: ValueType[String, Attr.Key] = vdomAttrVtKey[String]
}

trait ImplicitsForVdomAttr extends ImplicitsForVdomAttr1 {
  import Attr.ValueType
  import ValueType._

  implicit      val vdomAttrVtBoolean : Simple[Boolean  ] = byImplicit
  implicit      val vdomAttrVtString  : Simple[String   ] = string
  implicit      val vdomAttrVtInt     : Simple[Int      ] = byImplicit
  implicit lazy val vdomAttrVtLong    : Simple[Long     ] = byImplicit(_.toDouble)
  implicit lazy val vdomAttrVtFloat   : Simple[Float    ] = byImplicit
  implicit lazy val vdomAttrVtDouble  : Simple[Double   ] = byImplicit
  implicit lazy val vdomAttrVtShort   : Simple[Short    ] = byImplicit
  implicit lazy val vdomAttrVtByte    : Simple[Byte     ] = byImplicit
  implicit      val vdomAttrVtJsObject: Simple[js.Object] = direct

  inline given vdomAttrVtJsDictionary[A]: ValueType[js.Dictionary[A], js.Object] = byImplicit

  // For attributes that aren't typed yet
  inline implicit def vdomAttrVtJsAny[A](implicit f: A => js.Any): ValueType[A, Any] = byImplicit
}

// =====================================================================================================================

object ImplicitsForTagMod {
  final class OptionExt[O[_], A](o: O[A])(implicit O: OptionLike[O]) {
    def whenDefined(implicit f: A => TagMod): TagMod =
      O.fold(o, TagMod.empty)(f)
  }
}

trait ImplicitsForTagMod {
  implicit def vdomOptionExt[O[_], A](o: O[A])(implicit O: OptionLike[O]): ImplicitsForTagMod.OptionExt[O, A] =
    new ImplicitsForTagMod.OptionExt(o)
}

// =====================================================================================================================

object ImplicitsForVdomNode {

  final class TraversableOnceExt[A](private val as: IterableOnce[A]) extends AnyVal {

    /** Like `.mkString(String)` in Scala stdlib. */
    def mkReactFragment(sep: VdomNode)(implicit f: A => VdomNode): VdomElement =
      mkReactFragment(VdomNode.empty, sep, VdomNode.empty)

    /** Like `.mkString(String, String, String)` in Scala stdlib. */
    def mkReactFragment(start: VdomNode, sep: VdomNode, end: VdomNode)(implicit f: A => VdomNode): VdomElement = {
      val b = List.newBuilder[VdomNode]
      if (start ne VdomNode.empty) b += start
      Util.intercalateInto(b, as.iterator.map(f), sep)
      if (end ne VdomNode.empty) b += end
      ReactFragment(b.result(): _*)
    }

    /** Like `.mkString(String)` in Scala stdlib. */
    def mkTagMod(sep: TagMod)(implicit f: A => TagMod): TagMod =
      mkTagMod(VdomNode.empty, sep, VdomNode.empty)

    /** Like `.mkString(String, String, String)` in Scala stdlib. */
    def mkTagMod(start: TagMod, sep: TagMod, end: TagMod)(implicit f: A => TagMod): TagMod = {
      val b = Vector.newBuilder[TagMod]
      if (start ne VdomNode.empty) b += start
      Util.intercalateInto(b, as.iterator.map(f), sep)
      if (end ne VdomNode.empty) b += end
      TagMod.fromTraversableOnce(b.result())
    }

    def toReactFragment(implicit f: A => VdomNode): VdomElement =
      ReactFragment(as.iterator.map(f).toList: _*)

    def toTagMod(implicit f: A => TagMod): TagMod =
      TagMod.fromTraversableOnce(as.iterator.map(f))

    def toVdomArray(implicit f: A => VdomNode): VdomArray =
      VdomArray.empty() ++= as
  }
}

trait ImplicitsForVdomNode {
  import ImplicitsForVdomNode._

  inline given vdomNodeFromByte         : Conversion[Byte          , VdomNode] = v => VdomNode.cast(v)
  inline given vdomNodeFromShort        : Conversion[Short         , VdomNode] = v => VdomNode.cast(v)
  inline given vdomNodeFromInt          : Conversion[Int           , VdomNode] = v => VdomNode.cast(v)
  inline given vdomNodeFromLong         : Conversion[Long          , VdomNode] = v => VdomNode.cast(v.toString)
  inline given vdomNodeFromFloat        : Conversion[Float         , VdomNode] = v => VdomNode.cast(v)
  inline given vdomNodeFromDouble       : Conversion[Double        , VdomNode] = v => VdomNode.cast(v)
  inline given vdomNodeFromString       : Conversion[String        , VdomNode] = v => VdomNode.cast(v)
  inline given vdomNodeFromPropsChildren: Conversion[PropsChildren , VdomNode] = v => VdomNode.cast(v.raw)
  inline given vdomNodeFromRawReactNode : Conversion[raw.React.Node, VdomNode] = v => VdomNode(v)

  implicit def vdomNodeFromOption[O[_], A](o: O[A])(implicit O: OptionLike[O], f: A => VdomNode): VdomNode =
    O.fold(o, VdomNode.empty)(f)

  inline given vdomSeqExtForTO[A]: Conversion[IterableOnce[A], TraversableOnceExt[A]] = new TraversableOnceExt[A](_)
  inline given vdomSeqExtForSA[A]: Conversion[Array       [A], TraversableOnceExt[A]] = new TraversableOnceExt[A](_)
  inline given vdomSeqExtForJA[A]: Conversion[js.Array    [A], TraversableOnceExt[A]] = new TraversableOnceExt[A](_)
}

// =====================================================================================================================

trait Implicits
  extends ImplicitsForTagMod
     with ImplicitsForVdomAttr
     with ImplicitsForVdomNode

object Implicits extends Implicits

// =====================================================================================================================

object ImplicitsFromRaw {
  inline given vdomElementFromRawReactElement: Conversion[raw.React.Element, VdomElement] = VdomElement.apply
  inline given vdomNodeFromRawReactNode      : Conversion[raw.React.Node   , VdomNode   ] = VdomNode.apply
}