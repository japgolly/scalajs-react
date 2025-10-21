package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.test.{InferenceHelpers, ReactTestUtils, Simulate}
import japgolly.scalajs.react.vdom.ImplicitsFromRaw._
import utest._

object ScalaComponentPTest extends TestSuite {
  japgolly.scalajs.react.test.InitTestEnv()

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
    }

    "types" - {
      import InferenceHelpers._
      import ScalaComponent._
      "cu" - assertType[Component[P, S, B, CtorType.Nullary]].map(_.ctor()).is[Unmounted[P, S, B]]
    }

    "basic" - {
      val unmounted = BasicComponent(BasicProps("Bob"))
      assertEq(unmounted.props.name, "Bob")
      assertEq(unmounted.propsChildren.count, 0)
      assertEq(unmounted.propsChildren.isEmpty, true)
      assertEq(unmounted.key, None)
      assertEq(unmounted.ref, None)
      ReactTestUtils.withRenderedSync(unmounted) { t =>
        t.outerHTML.assert("<div>Hello Bob</div>")
      }
    }

    "withKey" - {
      val u = BasicComponent.withKey("k")(BasicProps("Bob"))
      assertEq(u.key, Option[Key]("k"))
      ReactTestUtils.withRenderedSync(u) { t =>
        t.outerHTML.assert("<div>Hello Bob</div>")
      }
    }

    "ctorReuse" -
      assert(BasicComponent(BasicProps("a")) ne BasicComponent(BasicProps("b")))

    "ctorMap" - {
      val c2 = BasicComponent.mapCtorType(_ withProps BasicProps("hello!"))
      val unmounted = c2()
      assertEq(unmounted.props.name, "hello!")
      ReactTestUtils.withRenderedSync(unmounted) { t =>
        t.outerHTML.assert("<div>Hello hello!</div>")
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

      def assertMountCount(expect: Int): Unit = {
        assertEq("mountCountA", mountCountA, expect)
        assertEq("mountCountB", mountCountB, expect)
      }

      var didUpdates = Vector.empty[Props]
      def assertUpdates(ps: Props*): Unit = {
        val e = ps.toVector
        assertEq("didUpdates", didUpdates, e)
      }

      var willUnmountCount = 0

      class Backend {
        def incMountCount = Callback(mountCountB += 1)
        def didUpdate(prev: Props, cur: Props) = Callback(didUpdates :+= cur - prev)
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
        .componentDidUpdate(x => x.backend.didUpdate(x.prevProps, x.currentProps))
        .componentWillUnmount(_.backend.incUnmountCount)
        .build

      val Comp = ScalaComponent.builder[Props]("")
          .initialState[Option[String]](None) // error message
          .render_PS((p, s) => s match {
            case None    => Inner(p).vdomElement
            case Some(e) => facade.React.createElement("div", null, "Error: " + e)
          })
        .componentDidCatch($ => $.setState(Some($.error.message.replaceFirst("'.+' *", ""))))
        .build

      ReactTestUtils.withReactRootSync { root =>
        assertMountCount(0)

        root.renderSync(Comp(Props(1, 2, 3)))
        assertMountCount(1)
        root.innerHTML.assert("<div>1 2 3</div>")
        assertUpdates()

        root.renderSync(Comp(Props(1, 2, 8)))
        root.innerHTML.assert("<div>1 2 3</div>")
        assertUpdates()

        root.renderSync(Comp(Props(1, 5, 8)))
        root.innerHTML.assert("<div>1 5 8</div>")
        assertUpdates(Props(0, 3, 0))

        assertEq("willUnmountCount", willUnmountCount, 0)
        root.renderSync(Comp(null))
        // Error message varies between development and production modes
        root.innerHTML.assertContainsCI("null")
        assertEq("willUnmountCount", willUnmountCount, 1)
      }

      assertMountCount(1)
      assertEq("willUnmountCount", willUnmountCount, 1)
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

      ReactTestUtils.withReactRootSync { root =>
        root.renderSync(Comp(10))
        root.innerHTML.assert("<div>p=10 s=110</div>")
        assertEq(snapshots, Vector())

        root.renderSync(Comp(20))
        root.innerHTML.assert("<div>p=20 s=120</div>")
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

        ReactTestUtils.withReactRootSync { root =>
          root.renderSync(Comp(108))
          root.innerHTML.assert("<div>p=108 s=4</div>")

          root.renderSync(Comp(103))
          root.innerHTML.assert("<div>p=103 s=3</div>")

          root.renderSync(Comp(204))
          root.innerHTML.assert("<div>p=204 s=52</div>")

          root.renderSync(Comp(6))
          root.innerHTML.assert("<div>p=6 s=26</div>")
        }
      }

      "early" - {
        val Comp = ScalaComponent.builder[Int]("")
          .getDerivedStateFromPropsAndState((p, _: Option[Int]) => -p)
          .noBackend
          .render_PS((p, s) => facade.React.createElement("div", null, s"p=$p s=$s"))
          .getDerivedStateFromPropsOption((_, s) => if (s > 100) Some(s - 100) else None)
          .getDerivedStateFromPropsOption((_, s) => if ((s & 1) == 0) Some(s >> 1) else None)
          .build

        ReactTestUtils.withReactRootSync { root =>
          root.renderSync(Comp(-108))
          root.innerHTML.assert("<div>p=-108 s=4</div>")

          root.renderSync(Comp(-103))
          root.innerHTML.assert("<div>p=-103 s=3</div>")

          root.renderSync(Comp(-204))
          root.innerHTML.assert("<div>p=-204 s=52</div>")

          root.renderSync(Comp(-6))
          root.innerHTML.assert("<div>p=-6 s=3</div>")
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

        ReactTestUtils.withReactRootSync { root =>
          root.renderSync(Comp(-108))
          root.innerHTML.assert("<div>p=-108 s=4</div>")

          root.renderSync(Comp(-103))
          root.innerHTML.assert("<div>p=-103 s=3</div>")

          root.renderSync(Comp(-204))
          root.innerHTML.assert("<div>p=-204 s=52</div>")

          root.renderSync(Comp(-6))
          root.innerHTML.assert("<div>p=-6 s=3</div>")
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
        .backend(new Backend(_))
        .renderS(_.backend.render(_))
        .build

      ReactTestUtils.withRenderedSync(Component()) { t =>
        assertEq(results, Vector())
        Simulate.click(t.asHtml())
        assertEq(results, Vector(1))
      }
    }
  }
}
