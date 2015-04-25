package japgolly.scalajs.react

import scala.scalajs.js
import js.annotation.JSName

object Addons {

  object ReactCssTransitionGroup {
    /** Items in the CSSTransitionGroup need this attribute for animation to work properly. */
    @inline final def key = vdom.Attrs.key
  }

  case class ReactCssTransitionGroup(name: String,
                                     enter: js.UndefOr[Boolean] = js.undefined,
                                     leave: js.UndefOr[Boolean] = js.undefined,
                                     component: js.UndefOr[String] = js.undefined) {
    def toJs: js.Object = {
      val p = js.Dynamic.literal("transitionName" -> name)
      enter    .foreach(v => p.updateDynamic("transitionEnter")(v))
      leave    .foreach(v => p.updateDynamic("transitionLeave")(v))
      component.foreach(v => p.updateDynamic("component")(v))
      p
    }

    def apply(children: ReactNode*): ReactComponentU_ = {
      val f = React.addons.CSSTransitionGroup
      f(toJs, children.toJsArray).asInstanceOf[ReactComponentU_]
    }
  }

  object ReactCloneWithProps {

    def mapToJS(props: Map[String, js.Any]): js.Object = {
      val obj = js.Dynamic.literal()
      props.foreach { case (key, value) =>
        obj.updateDynamic(key)(value)
      }
      obj
    }

    def apply(child: ReactNode, newProps: Map[String, js.Any]) = {
      val f = React.addons.cloneWithProps
      f(child, mapToJS(newProps)).asInstanceOf[ReactComponentU_]
    }
  }

  /**
   * React Performance Tools
   *
   * http://facebook.github.io/react/docs/perf.html
   */
  @JSName("React.addons.Perf")
  object Perf extends js.Object {
    type Measurements = js.Array[Measurement]

    sealed trait Measurement extends js.Object {
      val totalTime: Double = js.native
    }

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
