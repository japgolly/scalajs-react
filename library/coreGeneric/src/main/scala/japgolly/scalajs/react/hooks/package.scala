package japgolly.scalajs.react

import japgolly.scalajs.react.component.Delayed

package object hooks {
  // Offers nicer API for hooks
  type HookResult[A] = Delayed[A]
  val HookResult = Delayed
}
