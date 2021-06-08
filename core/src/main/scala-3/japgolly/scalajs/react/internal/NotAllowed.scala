package japgolly.scalajs.react.internal

/** It is impossible for you, nor are you supposed to, create an instance for this.
  * This is uninhabitable.
  *
  * This is a marker to indicate that you're not allowed to call a method, whilst leaving the method
  * in place for you with a deprecation telling you why its uncallable.
  */
// TODO: [3] Make erased class
sealed trait NotAllowed extends Any { // Any so that null can't be used
  def result: Nothing
}
