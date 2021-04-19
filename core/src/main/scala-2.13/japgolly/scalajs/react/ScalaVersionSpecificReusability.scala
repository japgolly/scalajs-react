package japgolly.scalajs.react

import scala.collection.immutable.ArraySeq

/** Reusability specific to Scala 2.13 */
trait ScalaVersionSpecificReusability extends ReusabilityForScala2 {
  import Reusability._

  final implicit def arraySeq[A: Reusability]: Reusability[ArraySeq[A]] =
    byRef[ArraySeq[A]] || indexedSeq[ArraySeq, A]

}
