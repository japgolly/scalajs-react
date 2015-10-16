package japgolly.scalajs.react

import scala.scalajs.js
import js.annotation.JSName

object Addons {

  object ReactCssTransitionGroup {
    /** Items in the CSSTransitionGroup need this attribute for animation to work properly. */
    @inline final def key = vdom.Attrs.key

    private val factory =
      React.createFactory(
        React.addons.CSSTransitionGroup.asInstanceOf[JsComponentType[js.Any, js.Any, TopNode]])
  }

  case class ReactCssTransitionGroup(name     : String,
                                     enter    : js.UndefOr[Boolean] = js.undefined,
                                     leave    : js.UndefOr[Boolean] = js.undefined,
                                     component: js.UndefOr[String]  = js.undefined,
                                     ref      : js.UndefOr[String]  = js.undefined) {
    def toJs: js.Object = {
      val p = js.Dynamic.literal("transitionName" -> name)
      enter    .foreach(p.updateDynamic("transitionEnter")(_))
      leave    .foreach(p.updateDynamic("transitionLeave")(_))
      component.foreach(p.updateDynamic("component"      )(_))
      ref      .foreach(p.updateDynamic("ref"            )(_))
      p
    }

    def apply(children: ReactNode*): ReactComponentU_ =
      ReactCssTransitionGroup.factory(toJs, children.toJsArray).asInstanceOf[ReactComponentU_]
  }

  object ReactCloneWithProps {

    def mapToJS(props: Map[String, js.Any]): js.Object = {
      val obj = js.Dynamic.literal()
      props.foreach { case (key, value) =>
        obj.updateDynamic(key)(value)
      }
      obj
    }

    /**
     * `cloneWithProps` is now deprecated. Use `React.cloneElement` instead (unlike `cloneWithProps`, `cloneElement`
     * does not merge `className` or `style` automatically; you can merge them manually if needed).
     */
    @deprecated("As of React 0.14, you must use React.cloneElement instead.", "0.10.0")
    def apply(child: ReactNode, newProps: Map[String, js.Any]) = {
      val f = React.addons.cloneWithProps
      f(child, mapToJS(newProps)).asInstanceOf[ReactComponentU_]
    }
  }

  /**
   * React Performance Tools
   *
   * https://facebook.github.io/react/docs/perf.html
   */
  @JSName("React.addons.Perf")
  @js.native
  object Perf extends js.Object {
    type Measurements = js.Array[Measurement]

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
    def printDOM(measurements: Measurements = js.native): Report = js.native
  }
}
