package japgolly.scalajs.react

package object extra
  extends japgolly.scalajs.react.extra.internal.StateSnapshot.HooksApiExt {

  type StateSnapshot[S] = japgolly.scalajs.react.extra.StateSnapshotF.StateSnapshot[S]
  lazy val StateSnapshot = japgolly.scalajs.react.extra.internal.StateSnapshot

}
