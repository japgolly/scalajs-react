package japgolly.scalajs.react

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.MonocleReact._
import japgolly.scalajs.react.test._
import japgolly.scalajs.react.test.TestUtil._
import monocle.macros.Lenses
import utest._

object StateSnapshotTest extends TestSuite {

  override def tests = Tests {
    "rezoom" - testReZoomWithReuse()
  }

  // ===================================================================================================================

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

    @Lenses
    final case class X(int: Int, str: String)

    object X {
      implicit val reusability: Reusability[X] = Reusability.derive

      object reusableLens {
        val int = Reusable.byRef(X.int)
        val str = Reusable.byRef(X.str)
      }
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

        // Method 2: StateSnapshot.withReuse.zoomL.prepareViaProps
        // Notice that we're using a normal lens here instead of a Reusable[lens]
        private val ssStrFn =
          StateSnapshot.withReuse.zoomL(X.str).prepareViaProps($)(_.ss)

        def render(p: Props): VdomElement = {
          renders += 1

          // Method 1: ss.withReuse.zoomStateL
          val ssI: StateSnapshot[Int] =
            p.ss.withReuse.zoomStateL(X.reusableLens.int)

          // Method 2: StateSnapshot.withReuse.zoomL.prepareViaProps
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

    ReactTestUtils.withNewBodyElement { mountNode =>
      val mounted = Top.Comp().renderIntoDOM(mountNode)
      def dom() = mounted.getDOMNode.asMounted().asElement()
      def intDom() = dom().querySelector("span")
      def strDom() = dom().querySelector("strong")

      assertOuterHTML(dom(), "<div><h3>Demo</h3><span>0</span><strong>yo</strong></div>")
      assertEq(counts(), (1, 1, 1))

      Simulate click intDom()
      assertOuterHTML(dom(), "<div><h3>Demo</h3><span>1</span><strong>yo</strong></div>")
      assertEq(counts(), (2, 2, 1)) // notice that StrComp didn't re-render

      Simulate click strDom()
      assertOuterHTML(dom(), "<div><h3>Demo</h3><span>1</span><strong>yo!</strong></div>")
      assertEq(counts(), (3, 2, 2)) // notice that IntComp didn't re-render
    }

  }
}
