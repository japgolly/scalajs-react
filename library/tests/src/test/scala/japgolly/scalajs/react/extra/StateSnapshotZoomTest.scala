package japgolly.scalajs.react.extra

import japgolly.scalajs.react.ReactMonocle._
import japgolly.scalajs.react._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.test._
import japgolly.scalajs.react.vdom.html_<^._
import monocle._
import utest._

object StateSnapshotZoomTest extends TestSuite {

  private def testReZoomWithReuse(): Unit = {

    var intRenders = 0
    val IntComp = ScalaComponent.builder[StateSnapshot[Int]]("")
      .render_P { p =>
        intRenders += 1
        <.span(p.value, ^.onClick --> p.modState(_ + 1))
      }
      .configure(Reusability.shouldComponentUpdate)
      .build

    // -----------------------------------------------------------------------------------------------------------------

    var strRenders = 0
    val StrComp = ScalaComponent.builder[StateSnapshot[String]]("")
      .render_P { p =>
        strRenders += 1
        <.strong(p.value, ^.onClick --> p.modState(_ + "!"))
      }
      .configure(Reusability.shouldComponentUpdate)
      .build

    // -----------------------------------------------------------------------------------------------------------------

    final case class X(int: Int, str: String)

    object X {
      val int = Lens[X, Int   ](_.int)(x => _.copy(int = x))
      val str = Lens[X, String](_.str)(x => _.copy(str = x))
      implicit def equal: UnivEq[X] = UnivEq.force
      implicit val reusability: Reusability[X] = Reusability.derive
    }

    // -----------------------------------------------------------------------------------------------------------------

    object Middle {

      var renders = 0

      final case class Props(name: String, ss: StateSnapshot[X]) {
        @inline def render = Comp(this)
      }

      implicit def reusability: Reusability[Props] =
        Reusability.derive

      final class Backend($: BackendScope[Props, Unit]) {

        private val ssIntFn =
          StateSnapshot.withReuse.zoomL(X.int).prepareViaProps($)(_.ss)

        private val ssStrFn =
          StateSnapshot.withReuse.zoomL(X.str).prepareViaProps($)(_.ss)

        def render(p: Props): VdomElement = {
          renders += 1

          val ssI: StateSnapshot[Int] =
            ssIntFn(p.ss.value)

          val ssS: StateSnapshot[String] =
            ssStrFn(p.ss.value)

          <.div(
            <.h3(p.name),
            IntComp(ssI),
            StrComp(ssS))
        }
      }

      val Comp = ScalaComponent.builder[Props]("")
        .renderBackend[Backend]
        .configure(Reusability.shouldComponentUpdate)
        .build
    }

    // -----------------------------------------------------------------------------------------------------------------

    object Top {

      final class Backend($: BackendScope[Unit, X]) {
        private val setStateFn =
          StateSnapshot.withReuse.prepareVia($)

        def render(state: X): VdomElement = {
          val ss = setStateFn(state)
          Middle.Props("Demo", ss).render
        }
      }

      val Comp = ScalaComponent.builder[Unit]("")
        .initialState(X(0, "yo"))
        .renderBackend[Backend]
        .build
    }

    // -----------------------------------------------------------------------------------------------------------------

    def counts() = (Middle.renders, intRenders, strRenders)

    LegacyReactTestUtils.withNewBodyElement { mountNode =>
      val mounted = Top.Comp().renderIntoDOM(mountNode)
      def dom() = mounted.getDOMNode.asMounted().asElement()
      def intDom() = dom().querySelector("span")
      def strDom() = dom().querySelector("strong")
      def values() = mounted.state -> counts()

      assertOuterHTML(dom(), "<div><h3>Demo</h3><span>0</span><strong>yo</strong></div>")
      assertEq(values(), X(0, "yo") -> (1, 1, 1))

      Simulate click intDom()
      assertOuterHTML(dom(), "<div><h3>Demo</h3><span>1</span><strong>yo</strong></div>")
      assertEq(values(), X(1, "yo") -> (2, 2, 1)) // notice that StrComp didn't re-render

      Simulate click strDom()
      assertOuterHTML(dom(), "<div><h3>Demo</h3><span>1</span><strong>yo!</strong></div>")
      assertEq(values(), X(1, "yo!") -> (3, 2, 2)) // notice that IntComp didn't re-render

      Simulate click intDom()
      assertOuterHTML(dom(), "<div><h3>Demo</h3><span>2</span><strong>yo!</strong></div>")
      assertEq(values(), X(2, "yo!") -> (4, 3, 2)) // notice that StrComp didn't re-render

      Simulate click strDom()
      assertOuterHTML(dom(), "<div><h3>Demo</h3><span>2</span><strong>yo!!</strong></div>")
      assertEq(values(), X(2, "yo!!") -> (5, 3, 3)) // notice that IntComp didn't re-render
    }

  } // testReZoomWithReuse

  override def tests = Tests {
    "rezoom" - testReZoomWithReuse()
  }
}
