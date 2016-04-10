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

  /**
    * [[ReactCssTransitionGroup]] is based on `ReactTransitionGroup` and is an easy way to perform CSS transitions and
    * animations when a React component enters or leaves the DOM.
    *
    * @param name The prefix for all class-names that will be applied to elements to trigger animations.
    * @param appear Enable/disable animating appear animations.
    * @param enter Enable/disable animating enter animations.
    * @param leave Enable/disable animating leave animations.
    * @param appearTimeout Timeout in milliseconds.
    * @param enterTimeout Timeout in milliseconds.
    * @param leaveTimeout Timeout in milliseconds.
    * @param component The container type of each child. By default this renders as a span.
    *
    * @see https://facebook.github.io/react/docs/animation.html
    */
  case class ReactCssTransitionGroup(name         : String,
                                     appear       : js.UndefOr[Boolean] = js.undefined,
                                     enter        : js.UndefOr[Boolean] = js.undefined,
                                     leave        : js.UndefOr[Boolean] = js.undefined,
                                     appearTimeout: js.UndefOr[Int]     = js.undefined,
                                     enterTimeout : js.UndefOr[Int]     = js.undefined,
                                     leaveTimeout : js.UndefOr[Int]     = js.undefined,
                                     component    : js.UndefOr[String]  = js.undefined,
                                     ref          : js.UndefOr[String]  = js.undefined) {
    def toJs: js.Object = {
      val o = js.Dictionary.empty[js.Any]
      o("transitionName") = name
      appear        foreach (o("transitionAppear"       ) = _)
      enter         foreach (o("transitionEnter"        ) = _)
      leave         foreach (o("transitionLeave"        ) = _)
      appearTimeout foreach (o("transitionAppearTimeout") = _)
      enterTimeout  foreach (o("transitionEnterTimeout" ) = _)
      leaveTimeout  foreach (o("transitionLeaveTimeout" ) = _)
      component     foreach (o("component"              ) = _)
      ref           foreach (o("ref"                    ) = _)
      o.asInstanceOf[js.Object]
    }

    /**
      * You must provide the key attribute for all children of [[ReactCssTransitionGroup]], even when only rendering a
      * single item. This is how React will determine which children have entered, left, or stayed.
      */
    def apply(children: ReactNode*): ReactComponentU_ =
      ReactCssTransitionGroup.factory(toJs, children.toJsArray).asInstanceOf[ReactComponentU_]
  }

  // ===================================================================================================================

  /**
   * React Performance Tools
   *
   * @see https://facebook.github.io/react/docs/perf.html
   */
  @js.native
  @JSName("React.addons.Perf")
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
