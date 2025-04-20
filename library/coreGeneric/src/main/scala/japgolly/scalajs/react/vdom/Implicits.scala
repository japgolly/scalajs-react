package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.feature.ReactFragment
import japgolly.scalajs.react.util._
import japgolly.scalajs.react.{PropsChildren, facade}
import scala.scalajs.js

// =====================================================================================================================

trait ImplicitsForVdomAttr1 extends CssUnitsOps {
  import Attr.ValueType

  implicit lazy val vdomAttrVtInnerHtml: ValueType[String, InnerHtmlAttr] =
    ValueType[String, InnerHtmlAttr]((b, html) => b(js.Dynamic.literal("__html" -> html)))

  implicit def vdomAttrVtKey[A](implicit k: A => facade.React.Key): ValueType[A, Attr.Key] =
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

  @inline implicit def vdomAttrVtJsDictionary[A]: ValueType[js.Dictionary[A], js.Object] = byImplicit

  // For attributes that aren't typed yet
  @inline implicit def vdomAttrVtJsAny[A](implicit f: A => js.Any): ValueType[A, Any] = byImplicit
}

// =====================================================================================================================

object ImplicitsForTagMod {
  final class OptionExt[O[_], A](o: O[A])(implicit O: OptionLike[O]) {
    def whenDefined(f: A => TagMod): TagMod =
      O.fold(o, TagMod.empty)(f)
  }
}

trait ImplicitsForTagMod {
  @inline implicit def vdomOptionExt[O[_], A](o: O[A])(implicit O: OptionLike[O]): ImplicitsForTagMod.OptionExt[O, A] =
    new ImplicitsForTagMod.OptionExt(o)
}

// =====================================================================================================================

object ImplicitsForVdomNode {

  final class TraversableOnceExt[A](private val as: IterableOnce[A]) extends AnyVal {

    /** Like `.mkString(String)` in Scala stdlib. */
    def mkReactFragment(sep: VdomNode)(f: A => VdomNode): VdomElement =
      mkReactFragment(VdomNode.empty, sep, VdomNode.empty)(f)

    /** Like `.mkString(String, String, String)` in Scala stdlib. */
    def mkReactFragment(start: VdomNode, sep: VdomNode, end: VdomNode)(f: A => VdomNode): VdomElement = {
      val b = List.newBuilder[VdomNode]
      if (start ne VdomNode.empty) b += start
      Util.intercalateInto(b, as.iterator.map(f), sep)
      if (end ne VdomNode.empty) b += end
      ReactFragment(b.result(): _*)
    }

    /** Like `.mkString(String)` in Scala stdlib. */
    def mkTagMod(sep: TagMod)(f: A => TagMod): TagMod =
      mkTagMod(VdomNode.empty, sep, VdomNode.empty)(f)

    /** Like `.mkString(String, String, String)` in Scala stdlib. */
    def mkTagMod(start: TagMod, sep: TagMod, end: TagMod)(f: A => TagMod): TagMod = {
      val b = Vector.newBuilder[TagMod]
      if (start ne VdomNode.empty) b += start
      Util.intercalateInto(b, as.iterator.map(f), sep)
      if (end ne VdomNode.empty) b += end
      TagMod.fromTraversableOnce(b.result())
    }

    def toReactFragment(f: A => VdomNode): VdomElement =
      ReactFragment(as.iterator.map(f).toList: _*)

    def toTagMod(f: A => TagMod): TagMod =
      TagMod.fromTraversableOnce(as.iterator.map(f))

    def toVdomArray(implicit f: A => VdomNode): VdomArray =
      VdomArray.empty() ++= as
  }
}

trait ImplicitsForVdomNode extends VdomNodeScalaSpecificImplicits {
  import ImplicitsForVdomNode._

  @inline implicit def vdomNodeFromByte         (v: Byte)         : VdomNode = VdomNode.cast(v)
  @inline implicit def vdomNodeFromShort        (v: Short)        : VdomNode = VdomNode.cast(v)
  @inline implicit def vdomNodeFromInt          (v: Int)          : VdomNode = VdomNode.cast(v)
          implicit def vdomNodeFromLong         (v: Long)         : VdomNode = VdomNode.cast(v.toString)
  @inline implicit def vdomNodeFromFloat        (v: Float)        : VdomNode = VdomNode.cast(v)
  @inline implicit def vdomNodeFromDouble       (v: Double)       : VdomNode = VdomNode.cast(v)
  @inline implicit def vdomNodeFromString       (v: String)       : VdomNode = VdomNode.cast(v)
  @inline implicit def vdomNodeFromPropsChildren(v: PropsChildren): VdomNode = VdomNode.cast(v.raw)

  implicit def vdomNodeFromOption[O[_], A](o: O[A])(implicit O: OptionLike[O], f: A => VdomNode): VdomNode =
    O.fold(o, VdomNode.empty)(f)

  @inline implicit def vdomSeqExtForTO[A](as: IterableOnce[A]): TraversableOnceExt[A] = new TraversableOnceExt[A](as)
  @inline implicit def vdomSeqExtForSA[A](as: Array       [A]): TraversableOnceExt[A] = new TraversableOnceExt[A](as)
  @inline implicit def vdomSeqExtForJA[A](as: js.Array    [A]): TraversableOnceExt[A] = new TraversableOnceExt[A](as)
}

// =====================================================================================================================

trait Implicits
  extends ImplicitsForTagMod
     with ImplicitsForVdomAttr
     with ImplicitsForVdomNode

object Implicits extends Implicits

// =====================================================================================================================

object ImplicitsFromRaw {
  @inline implicit def vdomElementFromRawReactElement(e: facade.React.Element): VdomElement =
    VdomElement(e)

  @inline implicit def vdomNodeFromRawReactNode(e: facade.React.Node): VdomNode =
    VdomNode(e)
}
