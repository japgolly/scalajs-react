package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{PropsChildren, raw}
import japgolly.scalajs.react.component.Generic
import japgolly.scalajs.react.internal.OptionLike
import scala.scalajs.js
import Exports.VdomTag

// =====================================================================================================================

trait ImplicitsForVdomAttr1 {
  import Attr.ValueType

  implicit lazy val vdomAttrVtInnerHtml: ValueType[String, InnerHtmlAttr] =
    ValueType[String, InnerHtmlAttr]((b, html) => b(js.Dynamic.literal("__html" -> html)))

  implicit def vdomAttrVtKey[A](implicit k: A => raw.Key): ValueType[A, Attr.Key] =
    ValueType((b, a) => b(k(a).asInstanceOf[js.Any]))

  implicit val vdomAttrVtKeyL: ValueType[Long, Attr.Key] =
    ValueType((b, a) => b(a.toString))

  // 90% case so reuse
  implicit val vdomAttrVtKeyS = vdomAttrVtKey[String]
}

trait ImplicitsForVdomAttr extends ImplicitsForVdomAttr1 {
  import Attr.ValueType
  import ValueType._

  implicit val vdomAttrVtBoolean: Simple[Boolean] = byImplicit

  implicit val vdomAttrVtString: Simple[String] = string

  implicit val vdomAttrVtInt: Simple[Int] = byImplicit

  implicit val vdomAttrVtJsObject: Simple[js.Object] = direct

  @inline implicit def vdomAttrVtJsDictionary[A]: ValueType[js.Dictionary[A], js.Object] = byImplicit

  // For attributes that aren't typed yet
  @inline implicit def vdomAttrVtJsAny[A](implicit f: A => js.Any): ValueType[A, Any] = byImplicit

  implicit def vdomAttrVtCssUnits[N: Numeric](n: N): CssUnits =
    new CssUnits(n)
}

// =====================================================================================================================

trait ImplicitsForTagMod {
  implicit def tagModFromOptionLike[O[_], A](o: O[A])(implicit O: OptionLike[O], f: A => TagMod): TagMod =
    O.fold(o, TagMod.Empty)(f)
}

// =====================================================================================================================

object ImplicitsForVdomNode {
  final class TravOnceExt[A](as: TraversableOnce[A])(implicit f: A => VdomNode) {
    def toVdomArray: VdomArray =
      VdomArray.empty() ++ as
  }
}

trait ImplicitsForVdomNode {
  import ImplicitsForVdomNode._

  implicit def vdomNodeFromLong         (v: Long)         : VdomNode = VdomNode.cast(v.toString)
  implicit def vdomNodeFromInt          (v: Int)          : VdomNode = VdomNode.cast(v)
  implicit def vdomNodeFromShort        (v: Short)        : VdomNode = VdomNode.cast(v)
  implicit def vdomNodeFromByte         (v: Byte)         : VdomNode = VdomNode.cast(v)
  implicit def vdomNodeFromDouble       (v: Double)       : VdomNode = VdomNode.cast(v)
  implicit def vdomNodeFromFloat        (v: Float)        : VdomNode = VdomNode.cast(v)
  implicit def vdomNodeFromString       (v: String)       : VdomNode = VdomNode.cast(v)
  implicit def vdomNodeFromPropsChildren(v: PropsChildren): VdomNode = VdomNode.cast(v.raw)

  implicit def vdomNodeExtForTO[A](as: TraversableOnce[A])(implicit f: A => VdomNode): TravOnceExt[A] = new TravOnceExt[A](as)(f)
  implicit def vdomNodeExtForSA[A](as: Array          [A])(implicit f: A => VdomNode): TravOnceExt[A] = new TravOnceExt[A](as)(f)
  implicit def vdomNodeExtForJA[A](as: js.Array       [A])(implicit f: A => VdomNode): TravOnceExt[A] = new TravOnceExt[A](as)(f)
}

// =====================================================================================================================

trait ImplicitsForVdomElement {
  implicit def vdomElementFromTag[A](a: A)(implicit f: A => VdomTag): VdomElement =
    f(a).render

  implicit def vdomElementFromComponent(u: Generic.BaseUnmounted[_, _, _, _]): VdomElement =
    u.vdomElement
}

// =====================================================================================================================

trait Implicits
  extends ImplicitsForTagMod
     with ImplicitsForVdomAttr
     with ImplicitsForVdomNode
     with ImplicitsForVdomElement

object Implicits extends Implicits

// =====================================================================================================================

object ImplicitsFromRaw {
  implicit def vdomElementFromRawReactElement(e: raw.ReactElement): VdomElement =
    VdomElement(e)

  implicit def vdomNodeFromRawReactNode(e: raw.ReactNode): VdomNode =
    VdomNode(e)
}