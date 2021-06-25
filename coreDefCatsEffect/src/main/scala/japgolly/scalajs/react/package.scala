package japgolly.scalajs

import japgolly.scalajs.react.util.DefaultEffects._

package object react
  extends japgolly.scalajs.react.internal.CoreGeneralF[Sync]
     with japgolly.scalajs.react.ReactCats
