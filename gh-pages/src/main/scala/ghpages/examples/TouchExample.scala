package ghpages.examples

import ghpages.examples.util.SingleSide
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom

/**
 * Example of using Touch events.
 *
 * TouchList is JavaScript collection, so it is converted to Scala IndexedSeq.
 * Showing only top 10 events, so mobile phone will not crash.
 * Preventing default events, so move and zoom events could also be tested
 */
object TouchExample {

  def content = SingleSide.Content(source, TouchExampleApp())

  val source =
    """
      |// Recommended to test with real Touch screens or with Chrome "Emulate touch screen"
      |
      |/** Keeping history of events **/
      |case class State(log: List[String] = List()) {
      |  def withEntry(name: String) = copy(log = name :: log)
      |
      |  def limit(max: Int) = if (log.size > max) copy(log = log.init) else this
      |}
      |
      |/** Saving touch event details to state */
      |class Backend(val $: BackendScope[Unit, State]) {
      |  def debugEvent(e: ReactTouchEvent): Unit = preventDefault(e) { state =>
      |    state withEntry s"${e.nativeEvent.`type`}: ${formatTouches(e.changedTouches)}" limit 10
      |  }
      |
      |  private def preventDefault(e: ReactTouchEvent)(transformer: State => State): Unit = {
      |    e.preventDefault()
      |    $.modState(transformer)
      |  }
      |
      |  private def formatTouches(touches: dom.TouchList) =
      |    toSeq(touches).map(formatCoordinates).mkString(" | ")
      |
      |  private def toSeq[A](list: dom.DOMList[A]) =
      |    for(i <- 0 to list.length - 1) yield list.item(i)
      |
      |  private def formatCoordinates(touch: dom.Touch) =
      |    s"${touch.screenX}x${touch.screenY}: ${touch.radiusX}x${touch.radiusY}"
      |}
      |
      |/** Rendering touch area and history of events */
      |val TouchExampleApp = ReactComponentB[Unit]("TouchExample")
      |  .initialState(new State)
      |  .backend(new Backend(_))
      |  .render { (P, S, B) =>
      |
      |  <.div(
      |    <.div(
      |      "Area to test touch events",
      |      ^.width := 200,                   // Basic style
      |      ^.height := 200,
      |      ^.border := "1px solid blue",
      |      ^.onTouchStart ==> B.debugEvent,  // Forwarding touch events
      |      ^.onTouchMove ==> B.debugEvent,
      |      ^.onTouchEnd ==> B.debugEvent,
      |      ^.onTouchCancel ==> B.debugEvent
      |    ),
      |    <.ul (                              // Rendering history of events
      |      S.log.map(
      |        <.li(_)
      |      )
      |    )
      |  )
      |
      |}.buildU
      |""".stripMargin

  /** Keeping history of events **/
  case class State(log: List[String] = List()) {
    def withEntry(name: String) = copy(log = name :: log)

    def limit(max: Int) = if (log.size > max) copy(log = log.init) else this
  }

  /** Saving touch event details to state */
  class Backend(val $: BackendScope[Unit, State]) {
    def debugEvent(e: ReactTouchEvent): Unit = preventDefault(e) { state =>
      state withEntry s"${e.nativeEvent.`type`}: ${formatTouches(e.changedTouches)}" limit 10
    }

    private def preventDefault(e: ReactTouchEvent)(transformer: State => State): Unit = {
      e.preventDefault()
      $.modState(transformer)
    }

    private def formatTouches(touches: dom.TouchList) =
      toSeq(touches).map(formatCoordinates).mkString(" | ")

    private def toSeq[A](list: dom.DOMList[A]) =
      for(i <- 0 to list.length - 1) yield list.item(i)

    private def formatCoordinates(touch: dom.Touch) =
      s"${touch.screenX}x${touch.screenY}: ${touch.radiusX}x${touch.radiusY}"
  }

  /** Rendering touch area and history of events */
  val TouchExampleApp = ReactComponentB[Unit]("TouchExample")
    .initialState(new State)
    .backend(new Backend(_))
    .render { (P, S, B) =>

    <.div(
      <.div(
        "Area to test touch events",
        ^.width := 200,                   // Basic style
        ^.height := 200,
        ^.border := "1px solid blue",
        ^.onTouchStart ==> B.debugEvent,  // Forwarding touch events
        ^.onTouchMove ==> B.debugEvent,
        ^.onTouchEnd ==> B.debugEvent,
        ^.onTouchCancel ==> B.debugEvent
      ),
      <.ul (                              // Rendering history of events
        S.log.map(
          <.li(_)
        )
      )
    )

  }.buildU
}
