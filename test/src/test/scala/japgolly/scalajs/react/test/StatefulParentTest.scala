package japgolly.scalajs.react.test

import utest._
import japgolly.scalajs.react._
import japgolly.scalajs.react.experimental.StaticPropComponent
import vdom.prefix_<^._
import TestUtil2._

object StatefulParentTest extends TestSuite {
  override def tests = TestSuite {

    'normal {
      val I = ReactComponentB[(CompState.WriteAccess[Int], Int)]("I")
        .render_P { case (w, i) =>
          <.div(
            <.div("state = ", <.span(i)),
            <.button("inc", ^.onClick --> w.modState(_ + 1)) // weird here - just an example
          )
        }
        .build

      val O = StatefulParent[Int](($, i) => I(($, i)))
      ReactTestUtils.withRenderedIntoDocument(O(3)) { c =>
        def state = ReactTestUtils.findRenderedDOMComponentWithTag(c, "span").getDOMNode().innerHTML.toInt
        def button = ReactTestUtils.findRenderedDOMComponentWithTag(c, "button")
        assertEq(state, 3)
        ReactTestUtils.Simulate click button
        assertEq(state, 4)
        c.setState(7)
        assertEq(state, 7)
      }
    }


    'spc {
      object SPC extends StaticPropComponent.Template("SPC") {
        override protected def configureBackend = new Backend(_, _)
        override protected def configureRender  = _.renderBackend

        case class StaticProps(state_$ : CompState.Access[DynamicProps])

        case class DynamicProps(d: Int)

        var backendsCreated = 0
        class Backend(s: StaticProps, $: BackendScope) {
          backendsCreated += 1

          val inc = s.state_$.modState(d => DynamicProps(d.d + 1))

          def render(p: DynamicProps) =
            <.div(
              <.span(p.d),
              <.button("Inc", ^.onClick --> inc))
        }
      }

      val O = StatefulParent.spc(SPC)(SPC.StaticProps)
      ReactTestUtils.withRenderedIntoDocument(O(SPC.DynamicProps(3))) { c =>
        def state = ReactTestUtils.findRenderedDOMComponentWithTag(c, "span").getDOMNode().innerHTML.toInt
        def button = ReactTestUtils.findRenderedDOMComponentWithTag(c, "button")
        assertEq(state, 3)
        ReactTestUtils.Simulate click button
        assertEq(state, 4)
        c.setState(SPC.DynamicProps(7))
        assertEq(state, 7)
      }
      assertEq(SPC.backendsCreated, 1)
    }

  }
}
