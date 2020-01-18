package japgolly.scalajs.react.core

import scalajs.js
import utest._
import scalaz.Equal
import japgolly.scalajs.react._
import japgolly.scalajs.react.internal.Box
import japgolly.scalajs.react.internal.JsUtil.inspectObject
import japgolly.scalajs.react.test.{InferenceUtil, ReactTestUtils}
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.ImplicitsFromRaw._
import scala.scalajs.js.|

object RawComponentEs6PTest extends TestSuite {

  case class BasicProps(name: String)

  class RawComp(ctorProps: Box[BasicProps]) extends raw.React.Component[Box[BasicProps], Box[Unit]] {
    override def render() =
      raw.React.createElement("div", null, "Hello ", this.props.unbox.name)
  }
  val RawCompCtor = js.constructorOf[RawComp]
  RawCompCtor.displayName = "HelloRaw6"

  val BasicComponent =
    JsComponent[Box[BasicProps], Children.None, Box[Unit]](RawCompCtor)
        .xmapProps(_.unbox)(Box(_))
        .xmapState(_.unbox)(Box(_))

  override def tests = Tests {

    "displayName" - {
      assertEq(BasicComponent.displayName, "HelloRaw6")
//      ReactTestUtils.withRenderedIntoDocument(BasicComponent(BasicProps("X"))) { m =>
//        println(inspectObject(m.raw))
//        assertEq(m.raw.displayName, "HelloRaw6")
//      }
    }

    "types" - {
      import InferenceUtil._
      import ScalaComponent._
      "cu" - test[Component[P, S, B, CtorType.Nullary]](_.ctor()).expect[Unmounted[P, S, B]]
      "um" - test[Unmounted[P, S, B]](_.renderIntoDOM(null)).expect[MountedImpure[P, S, B]]
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
        // assertEq(mounted.isMounted, yesItsMounted)
        assertEq(mounted.props.name, "Bob")
        assertEq(mounted.propsChildren.count, 0)
        assertEq(mounted.propsChildren.isEmpty, true)
        assertEq(mounted.state, ())
        // assertEq(mounted.backend, ())
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

    "lifecycle" - {
      case class Props(a: Int, b: Int, c: Int) {
        def -(x: Props) = Props(
          this.a - x.a,
          this.b - x.b,
          this.c - x.c)
      }
      implicit def equalProps = Equal.equalA[Props]

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

      class Backend($: BackendScope[Props, Unit]) {
        def willMount = Callback { mountCountBeforeMountB += mountCountB; willMountCountB += 1 }
        def incMountCount = Callback(mountCountB += 1)
        def willUpdate(cur: Props, next: Props) = Callback(willUpdates :+= next - cur)
        def didUpdate(prev: Props, cur: Props) = Callback(didUpdates :+= cur - prev)
        def receive(cur: Props, next: Props) = Callback(recievedPropDeltas :+= next - cur)
        def incUnmountCount = Callback(willUnmountCount += 1)
      }

      val Comp = ScalaComponent.builder[Props]("")
        .stateless
        .backend(new Backend(_))
        .render_P(p => raw.React.createElement("div", null, s"${p.a} ${p.b} ${p.c}"))
        .shouldComponentUpdatePure(_.cmpProps(_.a != _.a)) // update if .a differs
        .shouldComponentUpdatePure(_.cmpProps(_.b != _.b)) // update if .b differs
        .componentDidMount(_ => Callback(mountCountA += 1))
        .componentDidMount(_.backend.incMountCount)
        .componentWillMount(_ => Callback { mountCountBeforeMountA += mountCountA; willMountCountA += 1 })
        .componentWillMount(_.backend.willMount)
        .componentWillUpdate(x => x.backend.willUpdate(x.currentProps, x.nextProps))
        .componentDidUpdate(x => x.backend.didUpdate(x.prevProps, x.currentProps))
        .componentWillUnmount(_.backend.incUnmountCount)
        .componentWillReceiveProps(x => x.backend.receive(x.currentProps, x.nextProps))
        .build

      ReactTestUtils.withNewBodyElement { mountNode =>
        assertMountCount(0)

        var mounted = Comp(Props(1, 2, 3)).renderIntoDOM(mountNode)
        assertMountCount(1)
        assertOuterHTML(mounted.getDOMNode.asMounted().asElement(), "<div>1 2 3</div>")
        assertUpdates()

        mounted = Comp(Props(1, 2, 8)).renderIntoDOM(mountNode)
        assertOuterHTML(mounted.getDOMNode.asMounted().asElement(), "<div>1 2 3</div>")
        assertUpdates()

        mounted = Comp(Props(1, 5, 8)).renderIntoDOM(mountNode)
        assertOuterHTML(mounted.getDOMNode.asMounted().asElement(), "<div>1 5 8</div>")
        assertUpdates(Props(0, 3, 0))

        assertEq("willUnmountCount", willUnmountCount, 0)
      }

      assertMountCount(1)
      assertEq("willUnmountCount", willUnmountCount, 1)
      assertEq("recievedPropDeltas", recievedPropDeltas, Vector(Props(0, 0, 5), Props(0, 3, 0)))
    }
  }
}


object RawComponentEs6STest extends TestSuite {

  case class State(num1: Int, s2: State2)
  case class State2(num2: Int, num3: Int)

  implicit val equalState: Equal[State] = Equal.equalA
  implicit val equalState2: Equal[State2] = Equal.equalA

  class RawComp(ctorProps: Box[Unit]) extends raw.React.Component[Box[Unit], Box[State]] {
    this.state = Box(State(123, State2(400, 7)))
    override def render() = {
      val s = this.state.unbox
      raw.React.createElement("div", null, "State = ", s.num1, " + ", s.s2.num2, " + ", s.s2.num3)
    }
    def inc(): Unit =
      modState((s: State, _: Props) => Box(s.unbox.copy(s.unbox.num1 + 1)): State | Null)
  }
  val RawCompCtor = js.constructorOf[RawComp]
  RawCompCtor.displayName = "State, no Props"

  val Component =
    JsComponent[Box[Unit], Children.None, Box[State]](RawCompCtor)
        .xmapProps(_.unbox)(Box(_))
        .xmapState(_.unbox)(Box(_))
        .withRawType[RawComp]

  override def tests = Tests {

    "main" - {
      val unmounted = Component()
      assert(unmounted.propsChildren.isEmpty)
      assertEq(unmounted.key, None)
      assertEq(unmounted.ref, None)
      ReactTestUtils.withNewBodyElement { mountNode =>
        val mounted = unmounted.renderIntoDOM(mountNode)
        val n = mounted.getDOMNode.asMounted().asElement()

        assertOuterHTML(n, "<div>State = 123 + 400 + 7</div>")
        // assertEq(mounted.isMounted, yesItsMounted)
        assertEq(mounted.propsChildren.count, 0)
        assertEq(mounted.propsChildren.isEmpty, true)
        assertEq(mounted.state, State(123, State2(400, 7)))

        mounted.setState(State(666, State2(500, 7)))
        assertOuterHTML(n, "<div>State = 666 + 500 + 7</div>")
        // assertEq(mounted.isMounted, yesItsMounted)
        assertEq(mounted.propsChildren.isEmpty, true)
        assertEq(mounted.state, State(666, State2(500, 7)))

        mounted.raw.inc()
        assertOuterHTML(n, "<div>State = 667 + 500 + 7</div>")
        // assertEq(mounted.isMounted, yesItsMounted)
        assertEq(mounted.propsChildren.isEmpty, true)
        assertEq(mounted.state, State(667, State2(500, 7)))

        val zoomed = mounted
          .zoomState(_.s2)(n => _.copy(s2 = n))
          .zoomState(_.num2)(n => _.copy(num2 = n))
        assertEq(zoomed.state, 500)
        zoomed.modState(_ + 1)
        assertOuterHTML(n, "<div>State = 667 + 501 + 7</div>")
        // assertEq(mounted.isMounted, yesItsMounted)
        assertEq(mounted.propsChildren.isEmpty, true)
        assertEq(mounted.state, State(667, State2(501, 7)))
      }
    }

    "ctorReuse" -
      assert(Component() eq Component())

  }
}