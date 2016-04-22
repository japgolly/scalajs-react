package japgolly.scalajs.react.extra

import utest._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.test._
import ScalazReact._
import TestUtil2._

object ReusableFnTest extends TestSuite {

  type F1[A] = Int ~=> A
  type F2[A] = Int ~=> F1[A]
  type F3[A] = Int ~=> F2[A]

  def test1[A](f: F1[A], g: F1[A]): Unit = {
    assert(f ~=~ f)
    assert(f ~/~ g)
  }

  def test2[A](f: F2[A], g: F2[A]): Unit = {
    test1(f, g)
    assert(f(1) ~=~ f(1))
    assert(f(1) ~/~ f(2))
    assert(f(1) ~/~ g(1))
  }

  def test3[A](f: F3[A], g: F3[A]): Unit = {
    test2(f, g)
    assert(f(1)(2) ~=~ f(1)(2))
    assert(f(1)(2) ~/~ f(1)(3))
    assert(f(1)(2) ~/~ f(2)(2))
    assert(f(1)(2) ~/~ f(2)(1))
    assert(f(2)(1) ~=~ f(2)(1))
    assert(f(1)(2) ~/~ g(1)(2))
  }

  override def tests = TestSuite {

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

    'overComponent {
      import TestUtil.Inference._
      import CompScope._
      import CompState._
      test[DuringCallbackU[P, S, B]       ]($ => ReusableFn($).modState).expect[(S => S) ~=> Callback]
      test[DuringCallbackM[P, S, B, N]    ]($ => ReusableFn($).modState).expect[(S => S) ~=> Callback]
      test[BackendScope   [P, S]          ]($ => ReusableFn($).modState).expect[(S => S) ~=> Callback]
      test[ReadCallbackWriteCallbackOps[T]]($ => ReusableFn($).modState).expect[(T => T) ~=> Callback]
    }

    'endoOps {
      import TestUtil.Inference._
      case class Counter(count: Int) {
        def add(i: Int): Counter = copy(count = count + i)
      }
      test[BackendScope[P, S]          ]($ => ReusableFn($).modState.endoZoom(st_s)      ).expect[T ~=> Callback]
      test[BackendScope[P, Counter]    ]($ => ReusableFn($).modState.endoCall(_.add)     ).expect[Int ~=> Callback]
      test[BackendScope[P, Map[Int, S]]]($ => ReusableFn($).modState.endoCall2(_.updated)).expect[Int ~=> (S ~=> Callback)]
    }

    'byName {
      var state = 10
      val fn = ReusableFn.byName((_: Int) + state)
      assert(fn(2) == 12)
      state = 20
      assert(fn(2) == 22)
    }

    'renderComponent {
      import ReusabilityTest.SampleComponent1._
      val f = ReusableFn.renderComponent(component)
      val g: Props => ReactElement = f
      ()
    }

    'fnA {
      val f = ReusableFn((a: Int) => a + a)
      val g = ReusableFn((a: Int) => a * a)
      val f3 = f.fnA(3)
      assert(f3 ~=~ f.fnA(3))
      assert(f3 ~/~ f.fnA(2))
      assert(f3 ~/~ g.fnA(3))
      assert(f3() == 6)
    }

    'variance {
      import TestUtil.Inference._

      'fn1 {
        'o {
          test[Int  => Medium].usableAs[Int  => Big]
          test[Int ~=> Medium].usableAs[Int ~=> Big]
          compileError("test[Int  => Medium].usableAs[Int  => Small]")
          compileError("test[Int ~=> Medium].usableAs[Int ~=> Small]")
        }
        'i1 {
          test[Medium  => Int].usableAs[Small  => Int]
          test[Medium ~=> Int].usableAs[Small ~=> Int]
          compileError("test[Medium  => Int].usableAs[Big  => Int]")
          compileError("test[Medium ~=> Int].usableAs[Big ~=> Int]")
        }
      }

    }

  }
}
