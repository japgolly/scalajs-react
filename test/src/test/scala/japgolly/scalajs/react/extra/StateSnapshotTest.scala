package japgolly.scalajs.react.extra

import japgolly.scalajs.react.MonocleReact._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.StateSnapshot.{ModFn, SetFn}
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.test._
import japgolly.scalajs.react.vdom.html_<^._
import monocle._
import scala.annotation.nowarn
import utest._

object StateSnapshotTest extends TestSuite {

  def assertReusable[A](a: A, b: A)(implicit r: Reusability[A]): Unit = assert(a ~=~ b)
  def assertNotReusable[A](a: A, b: A)(implicit r: Reusability[A]): Unit = assert(a ~/~ b)

  override def tests = Tests {

    "noReuse" - {
      def make = StateSnapshot(1)((os, cb) => cb <<? os.map(Callback.log(_)))
      "same" - {val a = make; assertReusable(a, a)}
      "diff" - assertNotReusable(make, make)

      "inference" - {
        import japgolly.scalajs.react.test.InferenceUtil._
        "of" - {
                           assertType[Render        ](StateSnapshot.of(_)).is[StateSnapshot[S]]
          compileError(""" assertType[StateAccessP  ](StateSnapshot.of(_)) """) // lack safe read
          compileError(""" assertType[Backend       ](StateSnapshot.of(_)) """) // lack safe read
          compileError(""" assertType[ScalaMountedCB](StateSnapshot.of(_)) """) // lack safe read
          compileError(""" assertType[JsMounted     ](StateSnapshot.of(_)) """) // use (x.state).setStateVia(x.pure)
          compileError(""" assertType[ScalaMountedId](StateSnapshot.of(_)) """) // use (x.state).setStateVia(x.pure)
          compileError(""" assertType[StateAccessI  ](StateSnapshot.of(_)) """) // use (x.state).setStateVia(x.pure)
        }
        "apply_apply" - assertType[SetFn[S]](StateSnapshot(S)(_)).is[StateSnapshot[S]]
        "apply_setStateVia" - {
                           assertType[Render        ](StateSnapshot(S).setStateVia(_)).is[StateSnapshot[S]]
                           assertType[Backend       ](StateSnapshot(S).setStateVia(_)).is[StateSnapshot[S]]
                           assertType[ScalaMountedCB](StateSnapshot(S).setStateVia(_)).is[StateSnapshot[S]]
                           assertType[StateAccessP  ](StateSnapshot(S).setStateVia(_)).is[StateSnapshot[S]]
          compileError(""" assertType[JsMounted     ](StateSnapshot(S).setStateVia(_)) """) // use setStateVia(x.pure)
          compileError(""" assertType[ScalaMountedId](StateSnapshot(S).setStateVia(_)) """) // use setStateVia(x.pure)
          compileError(""" assertType[StateAccessI  ](StateSnapshot(S).setStateVia(_)) """) // use setStateVia(x.pure)
        }
        "zoom" - {
          def z = StateSnapshot.zoom[S, T](???)(???)
          "of" - assertType[Render](z.of(_)).is[StateSnapshot[T]]
          "apply_apply" - assertType[ModFn[S]](z(S)(_)).is[StateSnapshot[T]]
          "apply_setStateVia" - {
            assertType[Render ](z(S).setStateVia(_)).is[StateSnapshot[T]]
            assertType[Backend](z(S).setStateVia(_)).is[StateSnapshot[T]]
          }
        }
      }
    }

    "withReuse" - {
      val log = Reusable.byRef[SetFn[Int]]((os, cb) => cb <<? os.map(Callback.log(_)))
      val warn = Reusable.byRef[SetFn[Int]]((os, cb) => cb <<? os.map(Callback.warn(_)))
      def make = StateSnapshot.withReuse(1)(log)
      "equal" - assertReusable(make, make)
      "diffGet" - assertNotReusable(make, StateSnapshot.withReuse(2)(log))
      "diffSet" - assertNotReusable(make, StateSnapshot.withReuse(1)(warn))

      "inference" - {
        import japgolly.scalajs.react.test.InferenceUtil._
        def SS = StateSnapshot.withReuse
        implicit def rs: Reusability[S] = ???
        "apply_apply" - assertType[Reusable[SetFn[S]]](SS(S)(_)).is[StateSnapshot[S]]
        "zoom" - {
          @nowarn("cat=unused") def rs = ??? // shadow
          implicit def rt: Reusability[T] = ???
          def z = SS.zoom[S, T](???)(???)
          "prepareVia" - {
            def p = z.prepareVia(null.asInstanceOf[Render])
            "apply" - assertType[S](p(_)).is[StateSnapshot[T]]
          }
        }
      }

      "rezoom" - testReZoomWithReuse()
    }
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

    ReactTestUtils.withNewBodyElement { mountNode =>
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
}
