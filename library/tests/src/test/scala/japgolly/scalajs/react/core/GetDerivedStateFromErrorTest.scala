package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.TestUtil._
import utest._
import scala.scalajs.js

object GetDerivedStateFromErrorTest extends TestSuite {

  override def tests = Tests {

    "getDerivedStateFromError" - {
      val Child = ScalaComponent.builder[Unit]("")
        .render { _ =>
          throw new RuntimeException("Heads will roll!")
          <.div("Child")
        }
        .build

      val Parent = ScalaComponent.builder[Unit]("")
        .initialState(Option.empty[String])
        .render { $ =>
          $.state match {
            case Some(err) => <.div(s"Caught: $err")
            case None      => Child()
          }
        }
        .getDerivedStateFromError(e => Some(e.toString))
        .build

      ReactTestUtils.withReactRootSync { root =>
        // React 18+ might log the error to console even if caught,
        // and might need multiple renders or act() for error boundaries.
        // But withReactRootSync and basic render should work.
        root.renderSync(Parent())
        root.innerHTML.assert("<div>Caught: java.lang.RuntimeException: Heads will roll!</div>")
      }
    }

    "multiple" - {
      val Child = ScalaComponent.builder[Unit]("")
        .render { _ =>
          throw new RuntimeException("error")
        }
        .build

      val Parent = ScalaComponent.builder[Unit]("")
        .initialState(Option.empty[String])
        .render { $ =>
          $.state match {
            case Some(err) => <.div(s"Caught: $err")
            case None      => Child()
          }
        }
        .getDerivedStateFromError(_ => Some("first"))
        .getDerivedStateFromError(_ => Some("second"))
        .build

      ReactTestUtils.withReactRootSync { root =>
        root.renderSync(Parent())
        root.innerHTML.assert("<div>Caught: second</div>")
      }
    }
  }
}
