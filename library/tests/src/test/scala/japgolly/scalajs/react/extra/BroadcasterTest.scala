package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils2
import japgolly.scalajs.react.vdom.html_<^._
import utest._

object BroadcasterTest extends AsyncTestSuite {

  class B extends Broadcaster[Int] {
    override def broadcast(a: Int): Callback =
      super.broadcast(a)
  }

  val C = ScalaComponent.builder[B]("")
    .initialState(Vector.empty[Int])
    .backend(_ => OnUnmount())
    .renderS((_, state) => <.div("Got: " + state.mkString("{",",","}")))
    .configure(Listenable.listen(b => b, $ => (i: Int) => $.modState(_ :+ i)))
    .build

  override def tests = Tests {
    val b = new B

    "component" - {
      ReactTestUtils2.withRendered(C(b)){ d => 
        d.innerHTML.assert("Got: {}")
        for {
          _ <- d.act_(b.broadcast(2).runNow())
          _  = d.innerHTML.assert("Got: {2}")
          _ <- d.act_(b.broadcast(7).runNow())
          _  = d.innerHTML.assert("Got: {2,7}")
        } yield ()
      }
    }

    "unregister" - {
      var i1 = 0
      var i2 = 0
      val u1 = b.register(j => Callback(i1 += j)).runNow()
      val u2 = b.register(j => Callback(i2 += j)).runNow()

      b.broadcast(3).runNow()
      assert(i1 == 3, i2 == 3)

      u1.runNow()
      b.broadcast(4).runNow()
      assert(i1 == 3, i2 == 7)

      u1.runNow() // Subsequent calls do nada
      b.broadcast(2).runNow()
      assert(i1 == 3, i2 == 9)

      u2.runNow()
      b.broadcast(10).runNow()
      assert(i1 == 3, i2 == 9)
    }
  }
}
