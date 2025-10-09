package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.TestUtil._
import sourcecode.Line
import utest._

// TODO: Re-enable ReusabilityTest2 for Scala 3 after
// https://github.com/lampepfl/dotty-feature-requests/issues/161
// https://github.com/lampepfl/dotty/pull/11686
object ReusabilityTest2 extends TestSuite {

  sealed trait X
  object X {
    case object X1 extends X
    final case class X2() extends X
    sealed abstract class X3 extends X
    final case class X3a(i: Int) extends X3
    case object X3b extends X3
  }

  sealed abstract class Y
  object Y {
    case object Y1 extends Y
    final case class Y2() extends Y
    sealed trait Y3 extends Y
    final case class Y3a(i: Int) extends Y3
    case object Y3b extends Y3
  }

  override def tests = Tests {
    "macros" - {
      def test[A](a: A, b: A, expect: Boolean)(implicit r: Reusability[A], l: Line) =
        assertEq(r.test(a, b), expect)

      "sealedTrait" - {
        import X._
        def testAll()(implicit r: Reusability[X]): Unit = {
          test[X](X1    , X1    , true)
          test[X](X2()  , X1    , false)
          test[X](X1    , X2()  , false)
          test[X](X2()  , X2()  , true)
          test[X](X3a(1), X3a(1), true)
          test[X](X3a(1), X3a(2), false)
          test[X](X3a(2), X3a(1), false)
          test[X](X3a(2), X3b   , false)
          test[X](X3b   , X3b   , true)
        }
        "all" - {
          implicit val r: Reusability[X] = Reusability.derive[X]
          testAll()
        }
        "allDebug" - {
          implicit val r: Reusability[X] = Reusability.deriveDebug[X](true, false)
          testAll()
        }
        "reuseMid" - {
          implicit val r: Reusability[X] = {
            implicit val x3: Reusability[X3] = Reusability.always[X3]
            Reusability.derive[X]
          }
          test[X](X1    , X1    , true)
          test[X](X2()  , X1    , false)
          test[X](X1    , X2()  , false)
          test[X](X2()  , X2()  , true)
          test[X](X3a(1), X3a(1), true)
          test[X](X3a(1), X3a(2), true) // magic
          test[X](X3a(2), X3a(1), true) // magic
          test[X](X3a(2), X3b   , true) // magic
          test[X](X3b   , X3b   , true)
        }
      }

      "sealedClass" - {
        import Y._
        "all" - {
          implicit val r: Reusability[Y] = Reusability.derive[Y]
          test[Y](Y1    , Y1    , true)
          test[Y](Y2()  , Y1    , false)
          test[Y](Y1    , Y2()  , false)
          test[Y](Y2()  , Y2()  , true)
          test[Y](Y3a(1), Y3a(1), true)
          test[Y](Y3a(1), Y3a(2), false)
          test[Y](Y3a(2), Y3a(1), false)
          test[Y](Y3a(2), Y3b   , false)
          test[Y](Y3b   , Y3b   , true)
        }
        "reuseMid" - {
          implicit val r: Reusability[Y] = {
            implicit val y3: Reusability[Y3] = Reusability.always[Y3]
            Reusability.derive[Y]
          }
          test[Y](Y1    , Y1    , true)
          test[Y](Y2()  , Y1    , false)
          test[Y](Y1    , Y2()  , false)
          test[Y](Y2()  , Y2()  , true)
          test[Y](Y3a(1), Y3a(1), true)
          test[Y](Y3a(1), Y3a(2), true) // magic
          test[Y](Y3a(2), Y3a(1), true) // magic
          test[Y](Y3a(2), Y3b   , true) // magic
          test[Y](Y3b   , Y3b   , true)
        }
      }

    }
  }
}
