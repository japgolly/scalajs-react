package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object AjaxExample1 {

  def content = SingleSide.Content(source, Main())

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START

  def body = {
    val main = AsyncCallback
      .pure(<.div(^.color := "#070", "Async load complete."))
      .delayMs(2000)

    React.Suspense(
      fallback = <.div(^.color.red, "Loading..."),
      asyncBody = main
    )
  }

  val Main =
    ScalaComponent
      .builder[Unit]("Main")
      .stateless
      .render_(<.div(^.fontSize := "175%", body))
      .build

  // EXAMPLE:END
}
