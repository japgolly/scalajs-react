package japgolly.scalajs.react

import utest._
import scalajs.js
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.test._

/** Test that all of the module values in facade @JSImports are valid (meaning, that they link) */
object ImportsTest extends TestSuite {

  val Component = ScalaComponent.builder[Int]("Square")
    .render_P(i => <.p(s"$i² = ", <.em(i * i)))
    .build

  override def tests = Tests {

    // + react
    "react-dom/server" - {
      val s = ReactDOMServer.renderToStaticMarkup(Component(3))
      assert(s == "<p>3² = <em>9</em></p>")
    }

    // + react & react-dom
    "react-dom/test-utils" -
      ReactTestUtils.withRenderedIntoDocument(Component(4)) { m =>
        val s = m.outerHtmlScrubbed()
        assert(s == "<p>4² = <em>16</em></p>")
      }

    "react-addons-perf" - {
      val p = ReactAddons.Perf.asInstanceOf[js.UndefOr[js.Object]]
      assert(p.filter(_ ne null).isDefined)
    }

    "react-addons-css-transition-group" - {
      val p = ReactAddons.CSSTransitionGroup.raw.asInstanceOf[js.UndefOr[js.Object]]
      assert(p.filter(_ ne null).isDefined)
    }
  }
}
