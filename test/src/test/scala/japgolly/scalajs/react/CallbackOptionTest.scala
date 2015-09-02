package japgolly.scalajs.react

import utest._

object CallbackOptionTest extends TestSuite {

  import CallbackOption.{require, unless}

  override def tests = TestSuite {
    'conditions {
      var cp1 = false
      var cp2 = false
      val co = for {
        _ <- require(1 == 1)
        _ <- unless(1 == 0)
        _ <- Callback(cp1 = true).toCBO
        _ <- require(1 == 0) //  <--- short-circuit here
        _ <- Callback(cp2 = true).toCBO
        _ <- require(1 == 1)
        _ <- Callback(cp2 = true).toCBO
      } yield ()
      co.runNow()
      assert(cp1, !cp2)
    }

    'misc {
      var state = 3
      val get = CallbackTo(state).toCBO
      def set(i: Int) = Callback(state = i).toCBO

      val co = for {
        a <- get
        _ <- set(a + 1)
        b <- get
        _ <- require(b == 4)
        _ <- set(10)
      } yield b

      val cb = co.get
      assert(state == 3)
      val r1 = cb.runNow()
      assert(r1 == Some(4), state == 10)
      val r2 = cb.runNow()
      assert(r2 == None, state == 11)
    }
  }

}