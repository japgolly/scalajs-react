package japgolly.scalajs.react.raw

import japgolly.scalajs.react.raw
import scala.scalajs.js

sealed trait RecursiveTypeAliases {
  type ChildrenArray[A]
}

private[raw] object RecursiveTypeAliases extends RecursiveTypeAliases {
  override type ChildrenArray[A] = js.Array[raw.ChildrenArray[A]]
}