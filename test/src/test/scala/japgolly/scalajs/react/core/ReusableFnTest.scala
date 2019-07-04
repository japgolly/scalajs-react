package japgolly.scalajs.react.core

import utest._
import japgolly.scalajs.react._
import japgolly.scalajs.react.test._

object ReusableFnTest extends TestSuite {

  object Fs {
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
  }

  object AIs {
    sealed trait A { def i: Int }
    case class I(i: Int) extends A
    case object O extends A  { override def i = 0 }
    implicit def reuseA = Reusability.by_==[A]

    type F1 = I ~=> A // ← from A ~=> I
    type F2 = I ~=> F1

    def test1(f: => F1, g: => F1): Unit = {
      assert(f ~=~ f)
      assert(f ~/~ g)
    }

    def test2(f: => F2, g: => F2): Unit = {
      test1(f(I(1)), f(I(2)))
      test1(f(I(1)), g(I(1)))
    }
  }

  override def tests = Tests {

    "fn1" - {
      import Fs._
      val f = Reusable.fn((i: Int) => i + 1)
      val g = Reusable.fn((i: Int) => i + 10)
      test1(f, g)
      assert(f(5) == 6)
    }

    "fn2" - {
      import Fs._
      val f = Reusable.fn((a: Int, b: Int) => a + b)
      val g = Reusable.fn((a: Int, b: Int) => a * b)
      test2(f, g)
      assert(f(1)(2) == 3)
    }

    "fn3" - {
      import Fs._
      val f = Reusable.fn((a: Int, b: Int, c: Int) => a + b + c)
      val g = Reusable.fn((a: Int, b: Int, c: Int) => a * b * c)
      test3(f, g)
      assert(f(1)(2)(3) == 6)
    }

    "state" - {
      import InferenceUtil._
      test[BackendScope[P, S]]($ => Reusable.fn.state($).set).expect[S ~=> Callback]
      test[BackendScope[P, S]]($ => Reusable.fn.state($).mod).expect[(S => S) ~=> Callback]
    }

    "variance" - {
      import InferenceUtil._

      "fn1" - {
        "i" - {
          compileError("test[Medium  => Int].usableAs[Big  => Int]")
          compileError("test[Medium ~=> Int].usableAs[Big ~=> Int]")
                        test[Medium  => Int].usableAs[Small  => Int]
                        test[Medium ~=> Int].usableAs[Small ~=> Int]
        }
        "o" - {
          compileError("test[Int  => Medium].usableAs[Int  => Small]")
          compileError("test[Int ~=> Medium].usableAs[Int ~=> Small]")
                        test[Int  => Medium].usableAs[Int  => Big]
                        test[Int ~=> Medium].usableAs[Int ~=> Big]
        }
        "run" - {
          import AIs._

          def fai(add: Int): A ~=> I =
            Reusable.fn[A, I] {
              case I(i) => I(i + add)
              case O    => I(0)
            }

          val fai3 = fai(3)
          val fai7 = fai(7)
          def fia3: I ~=> A = fai3
          test1(fia3, fai7)
          assert(fia3(I(2)) == I(5))
        }
      }

      "fn2" - {
        "i1" - {
          compileError("test[Medium  => (Int  => Int)].usableAs[Big    => (Int  => Int)]")
          compileError("test[Medium ~=> (Int ~=> Int)].usableAs[Big   ~=> (Int ~=> Int)]")
                        test[Medium  => (Int  => Int)].usableAs[Small  => (Int  => Int)]
                        test[Medium ~=> (Int ~=> Int)].usableAs[Small ~=> (Int ~=> Int)]
        }
        "i2" - {
          compileError("test[Int  => (Medium  => Int)].usableAs[Int  => (Big    => Int)]")
          compileError("test[Int ~=> (Medium ~=> Int)].usableAs[Int ~=> (Big   ~=> Int)]")
                        test[Int  => (Medium  => Int)].usableAs[Int  => (Small  => Int)]
                        test[Int ~=> (Medium ~=> Int)].usableAs[Int ~=> (Small ~=> Int)]
        }
        "o" - {
          compileError("test[Int  => (Int  => Medium)].usableAs[Int  => (Int  => Small)]")
          compileError("test[Int ~=> (Int ~=> Medium)].usableAs[Int ~=> (Int ~=> Small)]")
                        test[Int  => (Int  => Medium)].usableAs[Int  => (Int  => Big  )]
                        test[Int ~=> (Int ~=> Medium)].usableAs[Int ~=> (Int ~=> Big  )]
        }
        "run" - {
          import AIs._

          def faai(add: Int): A ~=> (A ~=> I) =
            Reusable.fn[A, A, I]((a, b) => b match {
              case I(i) => I(a.i + i + add)
              case O    => I(a.i + 0)
            })

          val faai3 = faai(3)
          val faai7 = faai(7)
          def fiia3: I ~=> (I ~=> A) = faai3
          test2(fiia3, faai7)
          assert(fiia3(I(2))(I(19)) == I(2 + 19 + 3))
        }
      }

    }

  }
}
