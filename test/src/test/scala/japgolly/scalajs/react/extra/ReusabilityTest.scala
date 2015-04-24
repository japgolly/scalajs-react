package japgolly.scalajs.react.extra

import utest._

object ReusabilityTest extends TestSuite {

  val tests = TestSuite {

    'option {
      def test(vs: Option[Boolean]*) =
        for {a <- vs; b <- vs}
          assert((a ~=~ b) == (a == b))
      test(None, Some(true), Some(false))
    }

    'fns {
      type F1[A] = Int ~=> A
      type F2[A] = Int ~=> F1[A]
      type F3[A] = Int ~=> F2[A]

      def test1[A](f: F1[A], g: F1[A]): Unit = {
        f ~=~ f
        f ~/~ g
      }

      def test2[A](f: F2[A], g: F2[A]): Unit = {
        test1(f, g)
        f(1) ~=~ f(1)
        f(1) ~/~ f(2)
        f(1) ~/~ g(1)
      }

      def test3[A](f: F3[A], g: F3[A]): Unit = {
        test2(f, g)
        f(1)(2) ~=~ f(1)(2)
        f(1)(2) ~/~ f(1)(3)
        f(1)(2) ~/~ f(2)(2)
        f(1)(2) ~/~ f(2)(1)
        f(2)(1) ~=~ f(2)(1)
        f(1)(2) ~/~ g(1)(2)
      }

      'fn1 {
        val f = ReusableFn((i: Int) => i + 1)
        val g = ReusableFn((i: Int) => i + 10)
        test1(f, g)
        assert(f(5) == 6)
      }

      'fn2 {
        val f = ReusableFn((a: Int, b: Int) => a + b)
        val g = ReusableFn((a: Int, b: Int) => a * b)
        test2(f, g)
        assert(f(1)(2) == 3)
      }

      'fn3 {
        val f = ReusableFn((a: Int, b: Int, c: Int) => a + b + c)
        val g = ReusableFn((a: Int, b: Int, c: Int) => a * b * c)
        test3(f, g)
        assert(f(1)(2)(3) == 6)
      }
    }

  }
}
