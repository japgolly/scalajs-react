package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.test.{InferenceHelpers, ReactTestUtils, Simulate}
import japgolly.scalajs.react.vdom.ImplicitsFromRaw._
import scala.annotation.nowarn
import utest._

@nowarn("cat=deprecation")
object ScalaComponentPTest extends TestSuite {

  private case class BasicProps(name: String)

  private val BasicComponent =
    ScalaComponent.builder[BasicProps]("HelloMessage")
      .stateless
      .noBackend
      .render_P(p => facade.React.createElement("div", null, "Hello ", p.name))
      .build

  override def tests = Tests {

    "displayName" - {
      assertEq(BasicComponent.displayName, "HelloMessage")
//      ReactTestUtils.withRenderedIntoDocument(BasicComponent(BasicProps("X"))) { m =>
//        println(inspectObject(m.raw))
//        assertEq(m.raw.displayName, "HelloMessage")
//      }
    }

    "types" - {
      import InferenceHelpers._
      import ScalaComponent._
      "cu" - assertType[Component[P, S, B, CtorType.Nullary]].map(_.ctor()).is[Unmounted[P, S, B]]
      "um" - assertType[Unmounted[P, S, B]].map(_.renderIntoDOM(null)).is[MountedImpure[P, S, B]]
    }

    "basic" - {
      val unmounted = BasicComponent(BasicProps("Bob"))
      assertEq(unmounted.props.name, "Bob")
      assertEq(unmounted.propsChildren.count, 0)
      assertEq(unmounted.propsChildren.isEmpty, true)
      assertEq(unmounted.key, None)
      assertEq(unmounted.ref, None)
      ReactTestUtils.withNewBodyElement { mountNode =>
        val mounted = unmounted.renderIntoDOM(mountNode)
        val n = mounted.getDOMNode.asMounted().asElement()
        assertOuterHTML(n, "<div>Hello Bob</div>")
        assertEq(mounted.props.name, "Bob")
        assertEq(mounted.propsChildren.count, 0)
        assertEq(mounted.propsChildren.isEmpty, true)
        assertEq(mounted.state, ())
        assertEq(mounted.backend, ())
      }
    }

    "withKey" - {
      ReactTestUtils.withNewBodyElement { mountNode =>
        val u = BasicComponent.withKey("k")(BasicProps("Bob"))
        assertEq(u.key, Option[Key]("k"))
        val m = u.renderIntoDOM(mountNode)
        assertOuterHTML(m.getDOMNode.asMounted().asElement(), "<div>Hello Bob</div>")
      }
    }

    "ctorReuse" -
      assert(BasicComponent(BasicProps("a")) ne BasicComponent(BasicProps("b")))

    "ctorMap" - {
      val c2 = BasicComponent.mapCtorType(_ withProps BasicProps("hello!"))
      val unmounted = c2()
      assertEq(unmounted.props.name, "hello!")
      ReactTestUtils.withNewBodyElement { mountNode =>
        val mounted = unmounted.renderIntoDOM(mountNode)
        val n = mounted.getDOMNode.asMounted().asElement()
        assertOuterHTML(n, "<div>Hello hello!</div>")
      }
    }

    "lifecycle1" - {
      case class Props(a: Int, b: Int, c: Int) {
        def -(x: Props) = Props(
          this.a - x.a,
          this.b - x.b,
          this.c - x.c)
      }
      implicit def equalProps: UnivEq[Props] = UnivEq.force

      var mountCountA = 0
      var mountCountB = 0
      var mountCountBeforeMountA = 0
      var mountCountBeforeMountB = 0
      var willMountCountA = 0
      var willMountCountB = 0

      def assertMountCount(expect: Int): Unit = {
        assertEq("mountCountA", mountCountA, expect)
        assertEq("mountCountB", mountCountB, expect)
        assertEq("willMountCountA", willMountCountA, expect)
        assertEq("willMountCountB", willMountCountB, expect)
        assertEq("mountCountBeforeMountA", mountCountBeforeMountA, 0)
        assertEq("mountCountBeforeMountB", mountCountBeforeMountB, 0)
      }

      var didUpdates = Vector.empty[Props]
      var willUpdates = Vector.empty[Props]
      def assertUpdates(ps: Props*): Unit = {
        val e = ps.toVector
        assertEq("willUpdates", willUpdates, e)
        assertEq("didUpdates", didUpdates, e)
      }

      var recievedPropDeltas = Vector.empty[Props]

      var willUnmountCount = 0

      class Backend {
        def willMount = Callback { mountCountBeforeMountB += mountCountB; willMountCountB += 1 }
        def incMountCount = Callback(mountCountB += 1)
        def willUpdate(cur: Props, next: Props) = Callback(willUpdates :+= next - cur)
        def didUpdate(prev: Props, cur: Props) = Callback(didUpdates :+= cur - prev)
        def receive(cur: Props, next: Props) = Callback(recievedPropDeltas :+= next - cur)
        def incUnmountCount = Callback(willUnmountCount += 1)
      }

      val Inner = ScalaComponent.builder[Props]("")
        .stateless
        .backend(_ => new Backend)
        .render_P(p => facade.React.createElement("div", null, s"${p.a} ${p.b} ${p.c}"))
        .shouldComponentUpdatePure(_.cmpProps(_.a != _.a)) // update if .a differs
        .shouldComponentUpdatePure(_.cmpProps(_.b != _.b)) // update if .b differs
        .componentDidMount(_ => Callback(mountCountA += 1))
        .componentDidMount(_.backend.incMountCount)
        .componentWillMount(_ => AsyncCallback.delay { mountCountBeforeMountA += mountCountA; willMountCountA += 1 })
        .componentWillMount(_.backend.willMount)
        .componentWillUpdate(x => x.backend.willUpdate(x.currentProps, x.nextProps))
        .componentDidUpdate(x => x.backend.didUpdate(x.prevProps, x.currentProps))
        .componentWillUnmount(_.backend.incUnmountCount)
        .componentWillReceiveProps(x => x.backend.receive(x.currentProps, x.nextProps))
        .build

      val Comp = ScalaComponent.builder[Props]("")
          .initialState[Option[String]](None) // error message
          .render_PS((p, s) => s match {
            case None    => Inner(p).vdomElement
            case Some(e) => facade.React.createElement("div", null, "Error: " + e)
          })
        .componentDidCatch($ => $.setState(Some($.error.message.replaceFirst("'.+' *", ""))))
        .build

      val staleDomNodeCallback = ReactTestUtils.withNewBodyElement { mountNode =>
        assertMountCount(0)

        var mounted = Comp(Props(1, 2, 3)).renderIntoDOM(mountNode)
        def el() = mounted.getDOMNode.asMounted().asElement()

        assertMountCount(1)
        assertOuterHTML(el(), "<div>1 2 3</div>")
        assertUpdates()

        mounted = Comp(Props(1, 2, 8)).renderIntoDOM(mountNode)
        assertOuterHTML(el(), "<div>1 2 3</div>")
        assertUpdates()

        mounted = Comp(Props(1, 5, 8)).renderIntoDOM(mountNode)
        assertOuterHTML(el(), "<div>1 5 8</div>")
        assertUpdates(Props(0, 3, 0))

        assertEq("willUnmountCount", willUnmountCount, 0)
        mounted = Comp(null).renderIntoDOM(mountNode)
        assertOuterHTMLMatches(el(), "<div>Error: Cannot read propert(y|ies) of null.*</div>")
        assertEq("willUnmountCount", willUnmountCount, 1)
        mounted.withEffectsPure.getDOMNode
      }

      assertMountCount(1)
      assertEq("willUnmountCount", willUnmountCount, 1)
      assertEq("receivedPropDeltas", recievedPropDeltas, Vector(Props(0, 0, 5), Props(0, 3, 0)))

      assert(staleDomNodeCallback.runNow().mounted.isEmpty)
    }

    "lifecycle2" - {
      type Props = Int
      var snapshots = Vector.empty[String]

      val Comp = ScalaComponent.builder[Props]("")
        .initialState(0)
        .noBackend
        .render_PS((p, s) => facade.React.createElement("div", null, s"p=$p s=$s"))
        .getDerivedStateFromProps(_ + 100)
        .getSnapshotBeforeUpdatePure($ => s"${$.prevProps} -> ${$.currentProps}")
        .componentDidUpdate($ => Callback(snapshots :+= $.snapshot))
        .build

      ReactTestUtils.withNewBodyElement { mountNode =>
        var mounted = Comp(10).renderIntoDOM(mountNode)
        assertOuterHTML(mounted.getDOMNode.asMounted().asElement(), "<div>p=10 s=110</div>")
        assertEq(snapshots, Vector())

        mounted = Comp(20).renderIntoDOM(mountNode)
        assertOuterHTML(mounted.getDOMNode.asMounted().asElement(), "<div>p=20 s=120</div>")
        assertEq(snapshots, Vector("10 -> 20"))
      }
    }

    "getDerivedStateFromProps" - {

      "multiple" - {
        val Comp = ScalaComponent.builder[Int]("")
          .initialState(0)
          .noBackend
          .render_PS((p, s) => facade.React.createElement("div", null, s"p=$p s=$s"))
          .getDerivedStateFromPropsOption(p => if (p > 100) Some(p - 100) else None)
          .getDerivedStateFromPropsOption((_, s) => if ((s & 1) == 0) Some(s >> 1) else None)
          .build

        ReactTestUtils.withNewBodyElement { mountNode =>
          var mounted = Comp(108).renderIntoDOM(mountNode)
          assertOuterHTML(mounted.getDOMNode.asMounted().asElement(), "<div>p=108 s=4</div>")

          mounted = Comp(103).renderIntoDOM(mountNode)
          assertOuterHTML(mounted.getDOMNode.asMounted().asElement(), "<div>p=103 s=3</div>")

          mounted = Comp(204).renderIntoDOM(mountNode)
          assertOuterHTML(mounted.getDOMNode.asMounted().asElement(), "<div>p=204 s=52</div>")

          mounted = Comp(6).renderIntoDOM(mountNode)
          assertOuterHTML(mounted.getDOMNode.asMounted().asElement(), "<div>p=6 s=26</div>")
        }
      }

      "early" - {
        val Comp = ScalaComponent.builder[Int]("")
          .getDerivedStateFromProps(-_)
          .noBackend
          .render_PS((p, s) => facade.React.createElement("div", null, s"p=$p s=$s"))
          .getDerivedStateFromPropsOption((_, s) => if (s > 100) Some(s - 100) else None)
          .getDerivedStateFromPropsOption((_, s) => if ((s & 1) == 0) Some(s >> 1) else None)
          .build

        ReactTestUtils.withNewBodyElement { mountNode =>
          var mounted = Comp(-108).renderIntoDOM(mountNode)
          assertOuterHTML(mounted.getDOMNode.asMounted().asElement(), "<div>p=-108 s=4</div>")

          mounted = Comp(-103).renderIntoDOM(mountNode)
          assertOuterHTML(mounted.getDOMNode.asMounted().asElement(), "<div>p=-103 s=3</div>")

          mounted = Comp(-204).renderIntoDOM(mountNode)
          assertOuterHTML(mounted.getDOMNode.asMounted().asElement(), "<div>p=-204 s=52</div>")

          mounted = Comp(-6).renderIntoDOM(mountNode)
          assertOuterHTML(mounted.getDOMNode.asMounted().asElement(), "<div>p=-6 s=3</div>")
        }
      }

      "early2" - {
        val Comp = ScalaComponent.builder[Int]("")
          .getDerivedStateFromPropsAndState[Int]((p, os) => os.fold(0)(_ => -p))
          .noBackend
          .render_PS((p, s) => facade.React.createElement("div", null, s"p=$p s=$s"))
          .getDerivedStateFromPropsOption((_, s) => if (s > 100) Some(s - 100) else None)
          .getDerivedStateFromPropsOption((_, s) => if ((s & 1) == 0) Some(s >> 1) else None)
          .build

        ReactTestUtils.withNewBodyElement { mountNode =>
          var mounted = Comp(-108).renderIntoDOM(mountNode)
          assertOuterHTML(mounted.getDOMNode.asMounted().asElement(), "<div>p=-108 s=4</div>")

          mounted = Comp(-103).renderIntoDOM(mountNode)
          assertOuterHTML(mounted.getDOMNode.asMounted().asElement(), "<div>p=-103 s=3</div>")

          mounted = Comp(-204).renderIntoDOM(mountNode)
          assertOuterHTML(mounted.getDOMNode.asMounted().asElement(), "<div>p=-204 s=52</div>")

          mounted = Comp(-6).renderIntoDOM(mountNode)
          assertOuterHTML(mounted.getDOMNode.asMounted().asElement(), "<div>p=-6 s=3</div>")
        }
      }
    }

    "asyncSetState" - {
      import japgolly.scalajs.react.vdom.html_<^._

      var results = Vector.empty[Int]

      final class Backend($: BackendScope[Unit, Int]) {

        val onClick: AsyncCallback[Unit] =
          for {
            _ <- $.modStateAsync(_ + 1)
            s <- $.state.asAsyncCallback
          } yield results :+= s

        def render(s: Int): VdomNode =
          <.div(s, ^.onClick --> onClick)
      }

      val Component = ScalaComponent.builder[Unit]("")
        .initialState(0)
        .renderBackend[Backend]
        .build

      ReactTestUtils.withNewBodyElement { mountNode =>
        val mounted = Component().renderIntoDOM(mountNode)
        assertEq(results, Vector())

        Simulate.click(mounted.getDOMNode.toHtml.get)
        assertEq(results, Vector(1))
      }
    }
  }
}


object ScalaComponentSTest extends TestSuite {

  case class State(num1: Int, s2: State2)
  case class State2(num2: Int, num3: Int)

  implicit val equalState: UnivEq[State] = UnivEq.force
  implicit val equalState2: UnivEq[State2] = UnivEq.force

  class Backend($: BackendScope[Int, State]) {
    val inc: Callback =
      $.modState(s => s.copy(s.num1 + 1))
  }

  val Component =
    ScalaComponent.builder[Int]("")
      .initialState(State(123, State2(400, 7)))
      .backend(new Backend(_))
      .render_PS((p, s) => facade.React.createElement("div", null, "Props = ", p, ". State = ", s.num1, " + ", s.s2.num2, " + ", s.s2.num3))
      .build

  override def tests = Tests {

    "main" - {
      var callCount = 0
      val incCallCount = Callback(callCount += 1)
      val p = 9000
      val unmounted = Component(p)
      assert(unmounted.propsChildren.isEmpty)
      assertEq(unmounted.key, None)
      assertEq(unmounted.ref, None)
      ReactTestUtils.withNewBodyElement { mountNode =>
        val mounted = unmounted.renderIntoDOM(mountNode)
        val n = mounted.getDOMNode.asMounted().asElement()
        val b = mounted.backend
        var s = State(123, State2(400, 7))
        var cc = 0

        def test(children: Int = 0, incCallCount: Boolean = false): Unit = {
          if (incCallCount) cc += 1
          assertOuterHTML(n, s"<div>Props = $p. State = ${s.num1} + ${s.s2.num2} + ${s.s2.num3}</div>")
          assertEq(mounted.state, s)
          assertEq("propsChildren.count", mounted.propsChildren.count, children)
          assertEq("propsChildren.isEmpty", mounted.propsChildren.isEmpty, children == 0)
          assertEq("callCount", callCount, cc)
          assert(mounted.backend eq b)
        }

        test()

        s = State(66, State2(50, 77))
        mounted.setState(s, incCallCount)
        test(incCallCount = true)

        s = State(100, State2(300, 11))
        mounted.setStateOption(Some(s), incCallCount)
        test(incCallCount = true)

        mounted.setStateOption(None, incCallCount)
        test(incCallCount = true) // If this ever fails (i.e. React stops calling cb on setState(null, cb)),
                                  // then change the logic in StateAccess.apply & ReactTestVar

        s = State(88, s.s2)
        mounted.modState(_.copy(88), incCallCount)
        test(incCallCount = true)

        s = State(9088, s.s2)
        mounted.modState((s, p) => s.copy(s.num1 + p), incCallCount)
        test(incCallCount = true)

        s = State(828, s.s2)
        mounted.modStateOption(x => Some(x.copy(828)), incCallCount)
        test(incCallCount = true)

        s = State(9828, s.s2)
        mounted.modStateOption((s, p) => Some(s.copy(p + 828)), incCallCount)
        test(incCallCount = true)

        mounted.modStateOption(_ => None, incCallCount)
        test(incCallCount = true)

        s = State(666, State2(500, 7))
        mounted.setState(s)
        test()

        mounted.backend.inc.runNow()
        s = State(667, State2(500, 7))
        test()

        val zoomed = mounted
          .zoomState(_.s2)(n => _.copy(s2 = n))
          .zoomState(_.num2)(n => _.copy(num2 = n))
        assertEq(zoomed.state, 500)
        zoomed.modState(_ + 1)
        s = State(667, State2(501, 7))
        test()
      }
    }

    "ctorReuse" - {
      val Component =
        ScalaComponent.builder[Unit]("")
          .initialState(123)
          .render_S(s => facade.React.createElement("div", null, s))
          .build

      assert(Component() eq Component())
    }

  }
}