package japgolly.scalajs.react.experimental

import utest._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.test._
import TestUtil2._

object StaticPropComponentTest extends TestSuite {

  object Example1 extends StaticPropComponent.Template("Eg1") {
    override protected def configureBackend = new Backend(_, _)
    override protected def configureRender  = _.renderBackend

    case class StaticProps(s: () => Int)

    case class DynamicProps(d: Int)

    class Backend(s: StaticProps, $: BackendScope) {
      val n = s.s()
      def render(p: DynamicProps) =
        <.div(s"$n / ${p.d}")
    }
  }

  override def tests = TestSuite {

    'eg1 {
      import Example1._
      var i = 665
      def ne = Example1(StaticProps(() => { i += 1; i}))
      var e = ne
      val t = WithExternalCompStateAccess[DynamicProps](($, p) => e(p))
      ReactTestUtils.withRenderedIntoDocument(t(DynamicProps(7))) { c =>

        // First render
        assertOuterHTML(c.getDOMNode(), "<div>666 / 7</div>")
        assertEq(i, 666)

        // Dyn props change
        c.setState(DynamicProps(8))
        assertOuterHTML(c.getDOMNode(), "<div>666 / 8</div>")
        assertEq(i, 666)

        // Static props change
        e = ne
        c.setState(DynamicProps(2))
        assertOuterHTML(c.getDOMNode(), "<div>667 / 2</div>")
        assertEq(i, 667)

        // Dyn props change
        c.setState(DynamicProps(5))
        assertOuterHTML(c.getDOMNode(), "<div>667 / 5</div>")
        assertEq(i, 667)
      }
    }

  }
}