package japgolly.scalajs.react.extra

import utest._
import japgolly.scalajs.react.Callback

object StateSnapshotTest extends TestSuite {

  def assertReusable[A](a: A, b: A)(implicit r: Reusability[A]): Unit = assert(a ~=~ b)
  def assertNotReusable[A](a: A, b: A)(implicit r: Reusability[A]): Unit = assert(a ~/~ b)

  override def tests = TestSuite {
    'noReuse {
      def make = StateSnapshot(1)(Callback.log(_))
      'same - {val a = make; assertReusable(a, a)}
      'diff - assertNotReusable(make, make)
    }
    'withReuse {
      val log: Int ~=> Callback = ReusableFn(Callback.log(_))
      def make = StateSnapshot.withReuse(1)(log)
      'equal - assertReusable(make, make)
      'diffGet - assertNotReusable(make, StateSnapshot.withReuse(2)(log))
      'diffSet - assertNotReusable(make, StateSnapshot.withReuse(1)(ReusableFn(Callback.warn(_))))
    }
  }
}
