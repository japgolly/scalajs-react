package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide
import japgolly.scalajs.react._, vdom.prefix_<^._

import org.scalajs.dom.{WebSocket, MessageEvent, Event, CloseEvent, ErrorEvent}

object WebSocketsExample {

  def content = SingleSide.Content(source, main())

  lazy val main = addIntro(WebSocketsApp, _(
    s"Echo messages with WebSockets using ReactJS"))

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START
  val url = "ws://echo.websocket.org"

  case class State(ws: Option[WebSocket], log: List[String], message: String)

  class Backend($: BackendScope[Unit, State]) {
    def render(p: Unit, s: State) = {
      <.div(
        <.h3(s"Type a message and get an echo"),
        <.form(
          ^.onSubmit ==> send,
          <.input(
            ^.onChange ==> onChange, 
            ^.value := s.message),
          <.button(
            ^.disabled := s.message.isEmpty && s.ws.isDefined, "Send")), // Enable if the text exist and the WebSocket is connected
        <.h4("Connection log"),
        <.pre(                              // Log content
          ^.width := 200,                   // Basic style
          ^.height := 200,
          ^.border := "1px solid",
          s.log.map(<.p(_)))
      )
    }

    def onChange(e: ReactEventI): Callback =
      $.modState(_.copy(message = e.target.value))

    def send(e: ReactEventI): Callback = {
      // Send a message to the WebSocket
      val send          = $.state.map(s => s.ws.foreach(_.send(s.message)))
      val preventSubmit = e.preventDefaultCB
      val updateLog     = $.modState(s => s.copy(log = s.log :+ s"Sent: ${s.message}", message = ""))
      send >> preventSubmit >> updateLog
    }

    // These are message receiving events from the WebSocket "thread",
    // to change the state, you need to call `runNow()` on them
    def onopen(e: Event) = {
      // Indicate the connection is open
      $.modState(s => s.copy(log = s.log :+ "Connected")).runNow()
    }

    def onmessage(e: MessageEvent) = {
      // Echo message received
      $.modState(s => s.copy(log = s.log :+ s"Echo: ${e.data.toString}")).runNow()
    }

    def onerror(e: ErrorEvent) = {
      // Display error message
      $.modState(s => s.copy(log = s.log :+ s"Error: ${e.message}")).runNow()
    }

    def onclose(e: CloseEvent) = {
      // Close the connection
      $.modState(s => s.copy(ws = None, log = s.log :+ s"Closed: ${e.reason}")).runNow()
    }

    def start: Callback = {
      // Create WebSocket and setup listeners
      val ws = new WebSocket(url)
      ws.onopen = onopen _
      ws.onclose = onclose _
      ws.onmessage = onmessage _
      ws.onerror = onerror _
      $.setState(State(Some(ws), List("Connecting"), ""))
    }

    def end: Callback = {
      $.state.map(s => s.ws.foreach(_.close())) >> $.modState(_.copy(ws = None))
    }
  }

  val WebSocketsApp = ReactComponentB[Unit]("WebSocketsApp")
    .initialState(State(None, Nil, ""))
    .renderBackend[Backend]
    .componentDidMount(_.backend.start)
    .componentWillUnmount(_.backend.end)
    .buildU

  // EXAMPLE:END
}
