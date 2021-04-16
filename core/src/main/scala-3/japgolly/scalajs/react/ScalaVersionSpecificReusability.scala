package japgolly.scalajs.react

import scala.collection.immutable.ArraySeq

/** Reusability specific to Scala 3 */
trait ScalaVersionSpecificReusability {
  import Reusability.*

  final implicit def arraySeq[A: Reusability]: Reusability[ArraySeq[A]] =
    byRef[ArraySeq[A]] || indexedSeq[ArraySeq, A]

}
