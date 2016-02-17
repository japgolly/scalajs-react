package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide
import japgolly.scalajs.react._, vdom.prefix_<^._

object WebSocketsExample {

  def content = SingleSide.Content(source, main())

  lazy val main = addIntro(WebSocketsApp, _(
    s"Echo messages with WebSockets using ReactJS"))

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START

  import org.scalajs.dom.{WebSocket, MessageEvent, Event, CloseEvent, ErrorEvent}
  import scala.util.{Success, Failure}

  val url = "wss://echo.websocket.org"

  case class State(ws: Option[WebSocket], logLines: Vector[String], message: String) {

    // Create a new state with a line added to the log
    def log(line: String): State =
      copy(logLines = logLines :+ line)
  }

  class Backend($: BackendScope[Unit, State]) {
    def render(s: State) = {

      // Can only send if WebSocket is connected and user has entered text
      val send: Option[Callback] =
        for (ws <- s.ws if s.message.nonEmpty)
          yield sendMessage(ws, s.message)

      <.div(
        <.h3("Type a message and get an echo:"),
        <.div(
          <.input(
            ^.onChange ==> onChange,
            ^.value := s.message),
          <.button(
            ^.disabled := send.isEmpty, // Disable button if unable to send
            ^.onClick -->? send,        // --> suffixed by ? because it's for Option[Callback]
            "Send")),
        <.h4("Connection log"),
        <.pre(
          ^.width := 360,
          ^.height := 200,
          ^.border := "1px solid",
          s.logLines.map(<.p(_)))       // Display log
      )
    }

    def onChange(e: ReactEventI): Callback = {
      val newMessage = e.target.value
      $.modState(_.copy(message = newMessage))
    }

    def sendMessage(ws: WebSocket, msg: String): Callback = {
      // Send a message to the WebSocket
      def send = Callback(ws.send(msg))

      // Update the log, clear the text box
      def updateState = $.modState(s => s.log(s"Sent: ${s.message}").copy(message = ""))

      send >> updateState
    }

    def start: Callback = {
      def connect = Callback {
        // Get direct access so WebSockets API can modify state directly
        // (for access outside of a normal DOM/React callback).
        val direct = $.accessDirect

        // These are message-receiving events from the WebSocket "thread".

        def onopen(e: Event): Unit = {
          // Indicate the connection is open
          direct.modState(_.log("Connected."))
        }

        def onmessage(e: MessageEvent): Unit = {
          // Echo message received
          direct.modState(_.log(s"Echo: ${e.data.toString}"))
        }

        def onerror(e: ErrorEvent): Unit = {
          // Display error message
          direct.modState(_.log(s"Error: ${e.message}"))
        }

        def onclose(e: CloseEvent): Unit = {
          // Close the connection
          direct.modState(_.copy(ws = None).log(s"Closed: ${e.reason}"))
        }

        // Create WebSocket and setup listeners
        val ws = new WebSocket(url)
        ws.onopen = onopen _
        ws.onclose = onclose _
        ws.onmessage = onmessage _
        ws.onerror = onerror _
        direct.setState(State(Some(ws), Vector("Connecting..."), ""))
      }

      // Here we attempt to connect and handle a possible exception
      connect.attemptTry.flatMap {
        case Success(())    => Callback.empty
        case Failure(error) => $.modState(_.log(error.toString))
      }
    }

    def end: Callback = {
      def closeWebSocket = $.state.map(_.ws.foreach(_.close()))
      def clearWebSocket = $.modState(_.copy(ws = None))
      closeWebSocket >> clearWebSocket
    }
  }

  val WebSocketsApp = ReactComponentB[Unit]("WebSocketsApp")
    .initialState(State(None, Vector.empty, ""))
    .renderBackend[Backend]
    .componentDidMount(_.backend.start)
    .componentWillUnmount(_.backend.end)
    .buildU

  // EXAMPLE:END
}
