package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{PropsChildren, raw}
import japgolly.scalajs.react.component.Generic
import japgolly.scalajs.react.internal.OptionLike
import scala.scalajs.js
import Exports.ReactTag

// =====================================================================================================================

trait ImplicitsForReactAttr0 {
  import Attr.ValueType

  implicit lazy val reactAttrVtInnerHtml: ValueType[String, InnerHtmlAttr] =
    ValueType[String, InnerHtmlAttr]((b, html) => b(js.Dynamic.literal("__html" -> html)))

  implicit def reactAttrVtKey[A](implicit k: A => raw.Key): ValueType[A, Attr.Key] =
    ValueType((b, a) => b(k(a).asInstanceOf[js.Any]))

  implicit val reactAttrVtKeyL: ValueType[Long, Attr.Key] =
    ValueType((b, a) => b(a.toString))

  // 90% case so reuse
  implicit val reactAttrVtKeyS = reactAttrVtKey[String]
}

trait ImplicitsForReactAttr extends ImplicitsForReactAttr0 {
  import Attr.ValueType
  import ValueType._

  implicit val reactAttrVtBoolean: Simple[Boolean] = byImplicit

  implicit val reactAttrVtString: Simple[String] = string

  implicit val reactAttrVtInt: Simple[Int] = byImplicit

  implicit val reactAttrVtJsObject: Simple[js.Object] = direct

  @inline implicit def reactAttrVtJsDictionary[A]: ValueType[js.Dictionary[A], js.Object] = byImplicit

  // For attributes that aren't typed yet
  @inline implicit def reactAttrVtJsAny[A](implicit f: A => js.Any): ValueType[A, Any] = byImplicit

  implicit def reactAttrVtCssUnits[N: Numeric](n: N): CssUnits = new CssUnits(n)
}

// =====================================================================================================================

trait ImplicitsForTagMod {
  implicit def tagModFromO[O[_], A](o: O[A])(implicit O: OptionLike[O], f: A => TagMod): TagMod =
    O.fold(o, TagMod.Empty)(f)
}

// =====================================================================================================================

object ImplicitsForReactNode {

  final class TravOnceExt[A](as: TraversableOnce[A])(implicit f: A => ReactNode) {
    def toReactArray: ReactArray =
      ReactArray.empty() ++ as
  }
}

trait ImplicitsForReactNode {
  import ImplicitsForReactNode._

  implicit def reactNodeFromL                 (v: Long)               : ReactNode = ReactNode.cast(v.toString)
  implicit def reactNodeFromI                 (v: Int)                : ReactNode = ReactNode.cast(v)
  implicit def reactNodeFromSh                (v: Short)              : ReactNode = ReactNode.cast(v)
  implicit def reactNodeFromB                 (v: Byte)               : ReactNode = ReactNode.cast(v)
  implicit def reactNodeFromD                 (v: Double)             : ReactNode = ReactNode.cast(v)
  implicit def reactNodeFromF                 (v: Float)              : ReactNode = ReactNode.cast(v)
  implicit def reactNodeFromS                 (v: String)             : ReactNode = ReactNode.cast(v)
  implicit def reactNodeFromPC                (pc: PropsChildren)     : ReactNode = ReactNode.cast(pc.raw)

  implicit def reactNodeExtForTO[A](as: TraversableOnce[A])(implicit f: A => ReactNode): TravOnceExt[A] =
    new TravOnceExt[A](as)(f)

  implicit def reactNodeExtForAS[A](as: Array[A])(implicit f: A => ReactNode): TravOnceExt[A] =
    new TravOnceExt[A](as)(f)

  implicit def reactNodeExtForAJ[A](as: js.Array[A])(implicit f: A => ReactNode): TravOnceExt[A] =
    new TravOnceExt[A](as)(f)
}

// =====================================================================================================================

trait ImplicitsForReactElement {
  implicit def reactElementFromTag[A](a: A)(implicit f: A => ReactTag): ReactElement =
    f(a).render

  implicit def reactElementFromCompUnmounted(u: Generic.BaseUnmounted[_, _, _, _]): ReactElement =
    u.reactElement
}

// =====================================================================================================================

trait Implicits
  extends ImplicitsForReactAttr
     with ImplicitsForTagMod
     with ImplicitsForReactNode
     with ImplicitsForReactElement

object Implicits extends Implicits

// =====================================================================================================================

object ImplicitsFromRaw {
  @inline implicit def rawToVdomReactElement(e: raw.ReactElement): ReactElement =
    ReactElement(e)

  @inline implicit def rawToVdomReactNode(e: raw.ReactNode): ReactNode =
    ReactNode(e)
}