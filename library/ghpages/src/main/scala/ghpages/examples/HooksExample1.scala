package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SideBySide

object HooksExample1 {

  val jsSource =
    """
      |import React, { useState, useEffect } from 'react';
      |
      |function Example() {
      |  const [count, setCount] = useState(0);
      |
      |  // Similar to componentDidMount and componentDidUpdate:
      |  useEffect(() => {
      |    // Update the document title using the browser API
      |    document.title = `You clicked ${count} times`;
      |  });
      |
      |  return (
      |    <div>
      |      <p>You clicked {count} times</p>
      |      <button onClick={() => setCount(count + 1)}>
      |        Click me
      |      </button>
      |    </div>
      |  );
      |}
      |""".stripMargin

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START

  import japgolly.scalajs.react._
  import japgolly.scalajs.react.vdom.html_<^._
  import org.scalajs.dom.document

  object Example {
    val Component = ScalaFnComponent.withHooks[Unit]

      .useState(0)

      // Similar to componentDidMount and componentDidUpdate:
      .useEffectBy((props, count) => Callback {
        // Update the document title using the browser API
        document.title = s"You clicked ${count.value} times"
      })

      .render((props, count) =>
        <.div(
          <.p(s"You clicked ${count.value} times"),
          <.button(
            ^.onClick --> count.modState(_ + 1),
            "Click me"
          )
        )
      )
  }

  // EXAMPLE:END

  def content = SideBySide.Content(jsSource, source, main())

  lazy val main = addIntro(Example.Component.withKey(_)(), _(scalaPortOfPage("docs/hooks-overview.html#effect-hook")))

}
