package japgolly.scalajs.react

import scala.annotation.implicitNotFound
import scala.compiletime.erasedValue
import scala.language.`3.0`

sealed trait UpdateSnapshot

object UpdateSnapshot {

  sealed trait None extends UpdateSnapshot
  sealed trait Some[A] extends UpdateSnapshot

  type Value[U <: UpdateSnapshot] =
    U match {
      case None    => Unit
      case Some[a] => a
    }

  @implicitNotFound("You can only specify getSnapshotBeforeUpdate once, and it has to be before " +
    "you specify componentDidUpdate, otherwise the snapshot type could become inconsistent.")
  type SafetyProof[U <: UpdateSnapshot]

  erased given safetyProof[U <: UpdateSnapshot](using erased U =:= UpdateSnapshot.None): SafetyProof[U] =
    erasedValue
}
