package japgolly.scalajs.react.raw

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

/**
 * React Performance Tools
 *
 * @see https://facebook.github.io/react/docs/perf.html
 */
@js.native
@JSName("React.addons.Perf")
object Perf extends js.Object {

  // Opaque pending:
  // https://github.com/facebook/react/pull/6286
  // https://github.com/facebook/react/pull/6046
  @js.native
  sealed trait Measurements extends js.Object

  @js.native
  sealed trait Measurement extends js.Object {
    val totalTime: Double = js.native
  }

  @js.native
  sealed trait Report extends js.Object

  def start(): Unit = js.native

  def stop(): Unit = js.native

  def getLastMeasurements(): Measurements = js.native

  /**
   * Prints the overall time taken.
   */
  def printInclusive(measurements: Measurements = js.native): Report = js.native

  /**
   * "Exclusive" times don't include the times taken to mount the components:
   * processing props, getInitialState, call componentWillMount and componentDidMount, etc.
   */
  def printExclusive(measurements: Measurements = js.native): Report = js.native

  /**
   * <strong>The most useful part of the profiler.</strong>
   *
   * "Wasted" time is spent on components that didn't actually render anything,
   * e.g. the render stayed the same, so the DOM wasn't touched.
   */
  def printWasted(measurements: Measurements = js.native): Report = js.native

  /**
   * Prints the underlying DOM manipulations, e.g. "set innerHTML" and "remove".
   */
  def printOperations(measurements: Measurements = js.native): Report = js.native

  @deprecated("Renamed to printOperations() in React v15.", "0.11.0")
  def printDOM(measurements: Measurements = js.native): Report = js.native
}
