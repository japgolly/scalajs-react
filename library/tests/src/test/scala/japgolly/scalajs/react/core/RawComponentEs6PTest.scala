package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.internal.Box
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.test.{InferenceHelpers, ReactTestUtils}
import japgolly.scalajs.react.vdom.ImplicitsFromRaw._
import scala.annotation.nowarn
import scala.scalajs.js
import utest._

object RawComponentEs6PTest extends TestSuite {

  case class BasicProps(name: String)

  @nowarn("cat=unused")
  class RawComp(ctorProps: Box[BasicProps]) extends facade.React.Component[Box[BasicProps], Box[Unit]] {
    override def render() =
      facade.React.createElement("div", null, "Hello ", this.props.unbox.name)
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

    "lifecycle" - {
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

      val Comp = ScalaComponent.builder[Props]("")
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
      }

      assertMountCount(1)
      assertEq("willUnmountCount", willUnmountCount, 1)
    }
  }
}
