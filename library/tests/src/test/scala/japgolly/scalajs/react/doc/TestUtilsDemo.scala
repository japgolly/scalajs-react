package japgolly.scalajs.react.doc

import japgolly.scalajs.react._
import japgolly.scalajs.react.test._
import japgolly.scalajs.react.vdom.html_<^._
import utest._

// =====================================================================================================================
//
// Keep /doc/TESTING.md up-to-date with any changes made to this
//
// =====================================================================================================================

object TestUtilsDemo extends TestSuite {

  // This is a sample component that we will test
  val Component = ScalaFnComponent[String](props =>
    for {
      count <- useState(0)
    } yield
      <.div(
        <.p(s"Hi $props. You clicked ${count.value} times"),
        <.button("Click me", ^.onClick --> count.modState(_ + 1)),
      )
  )

  override def tests = Tests {

    // First we render the component
    ReactTestUtils2.withRenderedSync(Component("Axe")) { t =>

      // We have a variety of ways to test the HTML
      t.outerHTML.assert("<div><p>Hi Axe. You clicked 0 times</p><button>Click me</button></div>")
      t.root.outerHTML.assert("<div><div><p>Hi Axe. You clicked 0 times</p><button>Click me</button></div></div>")
      t.innerHTML.assertContains("You clicked 0 times")

      // Let's click the button
      Simulate.click(t.querySelector("button"))
      t.innerHTML.assertContains("You clicked 1 times")

      // Let's change the props
      t.root.renderSync(Component("Bob"))
      t.innerHTML.assertContains("Hi Bob. You clicked 1 times")
    }

  }
}
