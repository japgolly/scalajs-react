package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SideBySide

object HooksExample2 {

  val jsSource = HooksExample1.jsSource

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START

  import japgolly.scalajs.react._
  import japgolly.scalajs.react.vdom.html_<^._
  import org.scalajs.dom.document

  object Example {
    val Component = ScalaFnComponent[Unit] { _ =>
      for {
        count <- useState(0)

        // Similar to componentDidMount and componentDidUpdate:
        _ <- useEffect(Callback {
               // Update the document title using the browser API
               document.title = s"You clicked ${count.value} times"
             })

      } yield
        <.div(
          <.p(s"You clicked ${count.value} times"),
          <.button(
            ^.onClick --> count.modState(_ + 1),
            "Click me"
          )
        )
    }
  }

  // EXAMPLE:END

  def content = SideBySide.Content(jsSource, source, main())

  lazy val main = addIntro(Example.Component.withKey(_)(), _(scalaPortOfPage("docs/hooks-overview.html#effect-hook")))

}
