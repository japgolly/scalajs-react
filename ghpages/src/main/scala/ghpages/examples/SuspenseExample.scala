package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object SuspenseExample {

  def content = SingleSide.Content(source, Main)

  val source = GhPagesMacros.exampleSource

  val Main = <.div(^.fontSize := "175%", body)

  def body = {
    // EXAMPLE:START

    val main = AsyncCallback
      .pure(<.div(^.color := "#070", "Async load complete."))
      .delayMs(2000)

    React.Suspense(
      fallback  = <.div(^.color.red, "Loading..."),
      asyncBody = main)

    // EXAMPLE:END
  }

}
