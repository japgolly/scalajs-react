package japgolly.scalajs.react.facade

import japgolly.scalajs.react.facade
import scala.scalajs.js

sealed trait RecursiveTypeAliases {
  type ChildrenArray[A]
}

private[facade] object RecursiveTypeAliases extends RecursiveTypeAliases {
  override type ChildrenArray[A] = js.Array[facade.ChildrenArray[A]]
}