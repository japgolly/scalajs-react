package japgolly.scalajs.react.vdom

import scala.scalajs.js

trait ImplicitReactAttrValueTypes {
  import Attr.ValueType
  import ValueType._

  implicit val reactAttrVtString: Simple[String] = string

  implicit val reactAttrVtInt: Simple[Int] = byImplicit

  implicit val reactAttrVtJsObject: Simple[js.Object] = direct

  implicit def reactAttrVtJsDictionary[A]: ValueType[js.Dictionary[A], js.Object] = byImplicit

  // For attributes that aren't typed yet
  implicit def reactAttrVtJsAny[A](implicit f: A => js.Any): ValueType[A, Any] = byImplicit
}

trait Implicits
  extends ImplicitReactAttrValueTypes

object Implicits extends Implicits
