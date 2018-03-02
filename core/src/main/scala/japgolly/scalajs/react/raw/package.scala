package japgolly.scalajs.react

import scalajs.js
import scalajs.js.|

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

  // Not deprecated by React after all: https://twitter.com/dan_abramov/status/958658387352477697
  type Ref = String | Null

  type RefFn[A] = js.Function1[A | Null, Unit]

  type ReactClass[P <: js.Object, S <: js.Object] = js.Function1[P, React.Component[P, S]] with HasDisplayName
  type ReactClassP[P <: js.Object] = ReactClass[P, _ <: js.Object]
  type ReactClassUntyped = ReactClass[_ <: js.Object, _ <: js.Object]

  @js.native
  trait HasDisplayName extends js.Object {
    val displayName: js.UndefOr[String] = js.native
  }

}

