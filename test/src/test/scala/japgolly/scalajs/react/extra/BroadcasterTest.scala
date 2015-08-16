package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.vdom.prefix_<^._
import utest._

object BroadcasterTest extends TestSuite {

  class B extends Broadcaster[Int] {
    override def broadcast(a: Int): Unit =
      super.broadcast(a)
  }

  val C = ReactComponentB[B]("")
    .initialState(Vector.empty[Int])
    .backend(_ => new OnUnmount.Backend)
    .render((_, state, _) => <.div("Got: " + state.mkString("{",",","}")))
    .configure(Listenable.install(b => b, $ => (i: Int) => $.modState(_ :+ i)))
    .build

  override def tests = TestSuite {
    val b = new B

    'component {
      val c = ReactTestUtils.renderIntoDocument(C(b))
      assert(c.state == Vector())
      b.broadcast(2)
      assert(c.state == Vector(2))
      b.broadcast(7)
      assert(c.state == Vector(2, 7))
    }

    'unregister {
      var i1 = 0
      var i2 = 0
      val u1 = b.register(i1 += _)
      val u2 = b.register(i2 += _)

      b.broadcast(3)
      assert(i1 == 3, i2 == 3)

      u1()
      b.broadcast(4)
      assert(i1 == 3, i2 == 7)

      u1() // Subsequent calls do nada
      b.broadcast(2)
      assert(i1 == 3, i2 == 9)

      u2()
      b.broadcast(10)
      assert(i1 == 3, i2 == 9)
    }
  }
}
