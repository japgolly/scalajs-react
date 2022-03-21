package japgolly.scalajs.react

import japgolly.scalajs.react.extra.internal.AjaxF
import japgolly.scalajs.react.util.DefaultEffects._

package object extra
  extends japgolly.scalajs.react.extra.internal.StateSnapshot.HooksApiExt {

  object Ajax extends AjaxF[Sync, Async]

  object EventListener extends EventListenerF[Sync]

  type StateSnapshot[S] = japgolly.scalajs.react.extra.StateSnapshotF.StateSnapshot[S]
  lazy val StateSnapshot = japgolly.scalajs.react.extra.internal.StateSnapshot
}
