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

      'inference {
        import japgolly.scalajs.react.test.InferenceUtil._
        'of {
                           test[Render        ](StateSnapshot.of(_)).expect[StateSnapshot[S]]
          compileError(""" test[StateAccessP  ](StateSnapshot.of(_)) """) // lack safe read
          compileError(""" test[Backend       ](StateSnapshot.of(_)) """) // lack safe read
          compileError(""" test[ScalaMountedCB](StateSnapshot.of(_)) """) // lack safe read
          compileError(""" test[JsMounted     ](StateSnapshot.of(_)) """) // use (x.state).writeVia(x.pure)
          compileError(""" test[ScalaMountedId](StateSnapshot.of(_)) """) // use (x.state).writeVia(x.pure)
          compileError(""" test[StateAccessI  ](StateSnapshot.of(_)) """) // use (x.state).writeVia(x.pure)
        }
        'writeVia {
                           test[Render        ](StateSnapshot(S).writeVia(_)).expect[StateSnapshot[S]]
                           test[Backend       ](StateSnapshot(S).writeVia(_)).expect[StateSnapshot[S]]
                           test[ScalaMountedCB](StateSnapshot(S).writeVia(_)).expect[StateSnapshot[S]]
                           test[StateAccessP  ](StateSnapshot(S).writeVia(_)).expect[StateSnapshot[S]]
          compileError(""" test[JsMounted     ](StateSnapshot(S).writeVia(_)) """) // use writeVia(x.pure)
          compileError(""" test[ScalaMountedId](StateSnapshot(S).writeVia(_)) """) // use writeVia(x.pure)
          compileError(""" test[StateAccessI  ](StateSnapshot(S).writeVia(_)) """) // use writeVia(x.pure)
        }
      }
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
