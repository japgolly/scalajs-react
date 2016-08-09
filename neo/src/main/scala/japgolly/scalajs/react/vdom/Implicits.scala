package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{Component, PropsChildren, raw}
import japgolly.scalajs.react.internal.OptionLike
import scala.scalajs.js
import Exports.ReactTag

// =====================================================================================================================

trait ImplicitsForReactAttr0 {
  import Attr.ValueType

  implicit lazy val reactAttrVtInnerHtml: ValueType[String, InnerHtmlAttr] =
    ValueType[String, InnerHtmlAttr]((b, html) => b(js.Dynamic.literal("__html" -> html)))
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

  @inline implicit def reactAttrVtCssUnits[N: Numeric](n: N): CssUnits = new CssUnits(n)

}

// =====================================================================================================================

trait ImplicitsForTagMod {
  implicit def tagModFromO[O[_], A](o: O[A])(implicit O: OptionLike[O], f: A => TagMod): TagMod =
    O.fold(o, TagMod.Empty)(f)
}

// =====================================================================================================================

object ImplicitsForReactNode {
  type ReactArray = ReactNode

  final class TravOnceExt[A](as: TraversableOnce[A], f: A => ReactNode) {
    def toReactArray: ReactArray = {
      val array = new js.Array[raw.ReactNode]
      for (a <- as)
        array.push(f(a).rawReactNode)
      ReactNode.cast(array)
    }
  }
}

trait ImplicitsForReactNode {
  import ImplicitsForReactNode._

          implicit def reactNodeFromL                 (v: Long)               : ReactNode = ReactNode.cast(v.toString)
  @inline implicit def reactNodeFromI                 (v: Int)                : ReactNode = ReactNode.cast(v)
  @inline implicit def reactNodeFromSh                (v: Short)              : ReactNode = ReactNode.cast(v)
  @inline implicit def reactNodeFromB                 (v: Byte)               : ReactNode = ReactNode.cast(v)
  @inline implicit def reactNodeFromD                 (v: Double)             : ReactNode = ReactNode.cast(v)
  @inline implicit def reactNodeFromF                 (v: Float)              : ReactNode = ReactNode.cast(v)
  @inline implicit def reactNodeFromS                 (v: String)             : ReactNode = ReactNode.cast(v)
          implicit def reactNodeFromPC                (pc: PropsChildren)     : ReactNode = ReactNode.cast(pc.raw)

  implicit def reactNodeExtForTO[A](as: TraversableOnce[A])(implicit f: A => ReactNode): TravOnceExt[A] =
    new TravOnceExt[A](as, f)

  implicit def reactNodeExtForAS[A](as: Array[A])(implicit f: A => ReactNode): TravOnceExt[A] =
    new TravOnceExt[A](as, f)

  implicit def reactNodeExtForAJ[A](as: js.Array[A])(implicit f: A => ReactNode): TravOnceExt[A] =
    new TravOnceExt[A](as, f)
}

// =====================================================================================================================

trait ImplicitsForReactElement {
  @inline implicit def reactElementFromTag[A](a: A)(implicit f: A => ReactTag): ReactElement =
    f(a).render

  @inline implicit def reactElementFromCompUnmounted(u: Component.Unmounted[Any, Any]): ReactElement =
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