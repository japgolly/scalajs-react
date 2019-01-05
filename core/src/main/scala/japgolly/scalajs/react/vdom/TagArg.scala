package japgolly.scalajs.react.vdom

trait TagArg {

  /** Applies this modifier to the specified [[Builder]], such that when
    * rendering is complete the effect of adding this modifier can be seen.
    */
  def applyTo(b: Builder): Unit

}