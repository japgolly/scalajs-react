package japgolly.scalajs.react

import scala.scalajs.js
import scala.scalajs.js.|

/** See https://flow.org/en/docs/react/types */
package object raw {

  type Void = Unit

  type JsNumber = Byte | Short | Int | Float | Double

  type Empty = Void | Null | Boolean

  val recursiveTypeAliases: RecursiveTypeAliases = RecursiveTypeAliases
  type ChildrenArray[A] = A | recursiveTypeAliases.ChildrenArray[A]

  type PropsChildren = React.Node

  @js.native
  trait PropsWithChildren extends js.Object {
    val children: PropsChildren
  }

  @js.native
  trait HasDisplayName extends js.Object {
    val displayName: js.UndefOr[String] = js.native
  }

}
