package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.internal.Box
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.test.ReactTestUtils
import scala.annotation.nowarn
import scala.scalajs.js
import scala.scalajs.js.|
import utest._

object RawComponentEs6STest extends TestSuite {
  japgolly.scalajs.react.test.InitTestEnv()

  case class State1(num1: Int, s2: State2)
  case class State2(num2: Int, num3: Int)

  implicit val equalState1: UnivEq[State1] = UnivEq.force
  implicit val equalState2: UnivEq[State2] = UnivEq.force

  @nowarn("cat=unused")
  class RawComp(ctorProps: Box[Unit]) extends facade.React.Component[Box[Unit], Box[State1]] {
    this.state = Box(State1(123, State2(400, 7)))
    override def render() = {
      val s = this.state.unbox
      facade.React.createElement("div", null, "State = ", s.num1, " + ", s.s2.num2, " + ", s.s2.num3)
    }
    def inc(): Unit =
      modState((s: State, _: Props) => Box(s.unbox.copy(s.unbox.num1 + 1)): State | Null)
  }
  val RawCompCtor = js.constructorOf[RawComp]
  RawCompCtor.displayName = "State, no Props"

  val Component =
    JsComponent[Box[Unit], Children.None, Box[State1]](RawCompCtor)
        .xmapProps(_.unbox)(Box(_))
        .xmapState(_.unbox)(Box(_))
        .withRawType[RawComp]

  override def tests = Tests {

    "main" - {
      val unmounted = Component()
      assert(unmounted.propsChildren.isEmpty)
      assertEq(unmounted.key, None)
      assertEq(unmounted.ref, None)
      ReactTestUtils.withRenderedSync(unmounted) { t =>
        t.outerHTML.assert("<div>State = 123 + 400 + 7</div>")
      }
    }

    "ctorReuse" -
      assert(Component() eq Component())

  }
}
