package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{Component, PropsChildren, raw}
import scala.scalajs.js
import Exports.Tag

// =====================================================================================================================

trait ImplicitReactAttrValueTypes0 {
  import Attr.ValueType

  implicit lazy val reactAttrVtInnerHtml: ValueType[String, InnerHtmlAttr] =
    ValueType[String, InnerHtmlAttr]((b, html) => b(js.Dynamic.literal("__html" -> html)))
}

trait ImplicitReactAttrValueTypes extends ImplicitReactAttrValueTypes0 {
  import Attr.ValueType
  import ValueType._

  implicit val reactAttrVtBoolean: Simple[Boolean] = byImplicit

  implicit val reactAttrVtString: Simple[String] = string

  implicit val reactAttrVtInt: Simple[Int] = byImplicit

  implicit val reactAttrVtJsObject: Simple[js.Object] = direct

  implicit def reactAttrVtJsDictionary[A]: ValueType[js.Dictionary[A], js.Object] = byImplicit

  // For attributes that aren't typed yet
  implicit def reactAttrVtJsAny[A](implicit f: A => js.Any): ValueType[A, Any] = byImplicit

}

// =====================================================================================================================

trait ImplicitReactNodeTypes {
  @inline implicit def reactNodeFromL                 (v: Long)               : ReactNode = ReactNode.cast(v.toString)
  @inline implicit def reactNodeFromI                 (v: Int)                : ReactNode = ReactNode.cast(v)
  @inline implicit def reactNodeFromSh                (v: Short)              : ReactNode = ReactNode.cast(v)
  @inline implicit def reactNodeFromB                 (v: Byte)               : ReactNode = ReactNode.cast(v)
  @inline implicit def reactNodeFromD                 (v: Double)             : ReactNode = ReactNode.cast(v)
  @inline implicit def reactNodeFromF                 (v: Float)              : ReactNode = ReactNode.cast(v)
  @inline implicit def reactNodeFromS                 (v: String)             : ReactNode = ReactNode.cast(v)
          implicit def reactNodeFromPC                (pc: PropsChildren)     : ReactNode = ReactNode.cast(pc.raw)

  // TODO type ReactNode = js.Array[ReactNode | ReactEmpty]
  //  @inline implicit def reactNodeFromAn                (v: js.Array[ReactNode]): ReactNode = v.asInstanceOf[ReactNode]
  //  @inline implicit def reactNodeFromAt[T <% ReactNode](v: js.Array[T])        : ReactNode = v.toReactNodeArray
  //  @inline implicit def reactNodeFromC [T <% ReactNode](v: TraversableOnce[T]) : ReactNode = v.toReactNodeArray
}

// =====================================================================================================================

trait ImplicitReactElementTypes {
  @inline implicit def reactElementFromTag[A](a: A)(implicit f: A => Tag): ReactElement =
    f(a).render

  @inline implicit def reactElementFromCompUnmounted(u: Component.Unmounted[Any, Any]): ReactElement =
    u.reactElement
}

// =====================================================================================================================

trait Implicits
  extends ImplicitReactAttrValueTypes
     with ImplicitReactNodeTypes
     with ImplicitReactElementTypes

object Implicits extends Implicits

// =====================================================================================================================

object ImplicitsFromRaw {
  @inline implicit def rawToVdomReactElement(e: raw.ReactElement): ReactElement =
    ReactElement(e)

  @inline implicit def rawToVdomReactNode(e: raw.ReactNode): ReactNode =
    ReactNode(e)
}