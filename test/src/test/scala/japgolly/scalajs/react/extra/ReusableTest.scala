package japgolly.scalajs.react.extra

import utest._

object ReusableTest extends TestSuite {

  def assertReusable[A](a: A, b: A)(implicit r: Reusability[A]): Unit = assert(a ~=~ b)
  def assertNotReusable[A](a: A, b: A)(implicit r: Reusability[A]): Unit = assert(a ~/~ b)

  def i(n: Int) = Reusable(n)((x, y) => Math.abs(x - y) < 10)
  val one = i(1)

  override def tests = TestSuite {

    'refl - assertReusable(one, one)

    'simple {
      assertReusable(one, i(4))
      assertNotReusable(one, i(400))
    }

    'symmetric {
      assertNotReusable(one, Reusable.implicitly(2))
      assertNotReusable(Reusable.implicitly(2), one)
    }

    'differentTypes - assertNotReusable(one, Reusable.implicitly("hi").map(_.length))

    'map {
      "doesn't affect reusability" - {
        assertReusable(one.map(_ + 99999), i(1))
        assertNotReusable(one, i(99999).map(_ => 1))
      }
      'lazy {
        var set = 0
        val x = one.map(_ => {set += 1; set})
        assert(set == 0)
        var a = x.value
        assert(set == 1, a == 1)
        a = x.value
        assert(set == 1)
      }
    }

  }
}
