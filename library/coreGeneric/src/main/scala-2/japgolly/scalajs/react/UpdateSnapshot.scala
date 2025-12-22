package japgolly.scalajs.react

import scala.annotation.implicitNotFound

sealed trait UpdateSnapshot {
  type Value
}

object UpdateSnapshot {

  sealed trait None extends UpdateSnapshot {
    override final type Value = Unit
  }

  sealed trait Some[A] extends UpdateSnapshot {
    override final type Value = A
  }

  @implicitNotFound("You can only specify getSnapshotBeforeUpdate once, and it has to be before " +
    "you specify componentDidUpdate, otherwise the snapshot type could become inconsistent.")
  sealed trait SafetyProof[U <: UpdateSnapshot]

  implicit def safetyProof[U <: UpdateSnapshot](implicit ev: U =:= UpdateSnapshot.None): SafetyProof[U] =
    null.asInstanceOf[SafetyProof[U]]
}
