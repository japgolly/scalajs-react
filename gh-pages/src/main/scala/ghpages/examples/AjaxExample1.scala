package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object AjaxExample1 {

  def content = SingleSide.Content(source, Main())

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START
  import japgolly.scalajs.react.extra.Ajax

  /* There are two main points to this example:
   *
   * 1. The Backend submit method demonstrates how to make ajax calls in a way
   *    that's integrated with scalajs-react and Callback.
   *
   * 2. The `async: Option[AsyncState]` part of State demonstrates good practice
   *    by keeping track of async requests and affecting the UX accordingly.
   *
   * Nearly everything else is just to make the demo function.
   */

  case class Request(method: String, url: String, body: String)
  case class Response(status: Int, response: String)

  sealed trait AsyncState
  case class InFlight(req: Request) extends AsyncState
  case class Failed(req: Request, reason: String) extends AsyncState

  case class State(lastSuccess: Option[(Request, Response)],
                   async      : Option[AsyncState]) {

    // Is a request currently in-flight?
    val inFlight: Boolean =
      async.exists {
        case _: InFlight => true
        case _: Failed   => false
      }
  }

  final class Backend($: BackendScope[Unit, State]) {

    // =================================================================================================================
    // This is the heart of this demo.
    // Notice that it's all pure functions. There are no side-effects.
    private def submit(req: Request): Callback = {

      // Here is the AJAX request and response logic
      val ajax = Ajax(req.method, req.url)
        .setRequestContentTypeJsonUtf8
        .send(req.body)
        .onComplete { xhr =>
          def resp = Response(xhr.status, xhr.responseText)
          xhr.status match {
            case 200 => $.setState(State(Some((req, resp)), None))
            case _   => $.modState(_.copy(async = Some(Failed(req, Ajax.deriveErrorMessage(xhr)))))
          }
        }

      // Here we return a single Callback for event handlers
      $.modState(
        _.copy(async = Some(InFlight(req))), // First, the state management
        ajax.asCallback)                     // Then after the state change has been applied, execute the AJAX
    }
    // =================================================================================================================

    def render(s: State): VdomElement = {

      def button(label: String, req: Request) =
        <.button(
          ^.marginLeft := "1em",
          ^.onClick --> submit(req),
          ^.disabled := s.inFlight, // Don't allow more requests while one is already in progress
          label)

      val buttons =
        <.div(
          "Submit new request:",
          button("GET", GetOK),
          button("POST", PostOK),
          button("Failure", PostKO))

      val asyncStatus = s.async.whenDefined {
        case InFlight(req) => <.div(^.color.blue, s"IN-FLIGHT: $req ...")
        case f: Failed     => <.div(^.color.red, s"${f.req} FAILED.", <.br, f.reason)
      }

      val lastSuccess =
        s.lastSuccess.whenDefined {
          case (req, resp) =>
            def keyValues(title: String, kvs: (String, Any)*) =
              <.td(
                ^.background := "#f2f2f2",
                ^.padding := "1em",
                ^.border := "solid 1px #666",
                ^.whiteSpace.`pre-wrap`,
                ^.verticalAlign.`text-top`,
                <.div(title, ^.fontWeight.bold, ^.textDecoration.underline, ^.marginBottom := "1.2em", ^.color := "#222"),
                kvs.toTagMod { case (k, v) =>
                  <.div(^.paddingBottom := "1.2em",
                    <.div(^.fontWeight.bold, k + ":"),
                    <.div(v.toString.replace("\",", "\", ")))
                })

            <.table(^.marginBottom := "2em",
              <.tbody(<.tr(
                keyValues("Request", "Method" -> req.method, "URL" -> req.url, "Body" -> req.body),
                keyValues("Response", "Status" -> resp.status, "Body" -> resp.response))))
        }

      <.div(
        lastSuccess,
        buttons,
        asyncStatus)
    }

    // Sample Requests
    val GetOK  = Request("GET", "https://reqres.in/api/users/2", "")
    val PostOK = Request("POST", "https://reqres.in/api/login","""{ "email": "peter@klaven", "password": "cityslicka" }""")
    val PostKO = Request("POST", "https://reqres.in/api/login","""{ "email": "peter@klaven" }""")
  }

  val Main = ScalaComponent.builder[Unit]("AjaxExample")
    .initialState(State(None, None))
    .renderBackend[Backend]
    .build

  // EXAMPLE:END
}
