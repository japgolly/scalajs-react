package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import utest._

object ReusableTest extends TestSuite {

  def assertReusable[A](a: A, b: A)(implicit r: Reusability[A]): Unit =
    assert(a ~=~ b)

  def assertNotReusable[A](a: A, b1: A, bn: A*)(implicit r: Reusability[A]): Unit =
    (b1 :: bn.toList).foreach(b => assert(a ~/~ b))

  def n(i: Int) = Reusable.implicitly(i)
  def nIsh(n: Int) = Reusable(n)((x, y) => Math.abs(x - y) < 10)
  val oneIsh = nIsh(1)

  override def tests = Tests {

    "refl" - assertReusable(oneIsh, oneIsh)

    "simple" - {
      assertReusable(oneIsh, nIsh(4))
      assertNotReusable(oneIsh, nIsh(400))
    }

    "symmetric" - {
      assertNotReusable(oneIsh, n(2))
      assertNotReusable(n(2), oneIsh)
    }

    "differentTypes" - assertNotReusable(oneIsh, Reusable.implicitly("hi").map(_.length))

    "map" - {
      "doesn't affect reusability" - {
        assertReusable(oneIsh.map(_ + 99999), nIsh(1))
        assertNotReusable(oneIsh, nIsh(99999).map(_ => 1))
      }
      "lazy" - {
        var set = 0
        val x = oneIsh.map(_ => {set += 1; set})
        assert(set == 0)
        var a = x.value
        assert(set == 1, a == 1)
        a = x.value
        assert(set == 1)
      }
    }

    "ap" - {
      val f = Reusable.fn((_: Int) => 5)
      val g = Reusable.fn((_: Int) => 6)
      def f1 = n(1) ap f
      def f2 = n(2) ap f
      def g1 = n(1) ap g
      def g2 = n(2) ap g
      assertReusable(f1, f1)
      assertReusable(f2, f2)
      assertReusable(g1, g1)
      assertReusable(g2, g2)
      assertNotReusable(f1, f2, g1, g2)
      assertNotReusable(f2, g1, g2, f1)
      assertNotReusable(g1, g2, f1, f2)
      assertNotReusable(g2, f1, f2, g1)
    }

    'callback {
      val a = Callback(0)
      val b = Callback(0)
      val fa = Reusable.callbackByRef(a)
      val fb = Reusable.callbackByRef(b)
      assertReusable(fa, fa)
      assertReusable(fb, fb)
      assertNotReusable(fa, fb)
      assertNotReusable(fb, fa)
    }

  }
}
