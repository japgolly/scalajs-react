package japgolly.scalajs.react.extra

import utest._
import japgolly.scalajs.react._
import StateSnapshot.{ModFn, SetFn}

object StateSnapshotTest extends TestSuite {

  def assertReusable[A](a: A, b: A)(implicit r: Reusability[A]): Unit = assert(a ~=~ b)
  def assertNotReusable[A](a: A, b: A)(implicit r: Reusability[A]): Unit = assert(a ~/~ b)

  override def tests = Tests {

    "noReuse" - {
      def make = StateSnapshot(1)((os, cb) => cb <<? os.map(Callback.log(_)))
      "same" - {val a = make; assertReusable(a, a)}
      "diff" - assertNotReusable(make, make)

      "inference" - {
        import japgolly.scalajs.react.test.InferenceUtil._
        "of" - {
                           test[Render        ](StateSnapshot.of(_)).expect[StateSnapshot[S]]
          compileError(""" test[StateAccessP  ](StateSnapshot.of(_)) """) // lack safe read
          compileError(""" test[Backend       ](StateSnapshot.of(_)) """) // lack safe read
          compileError(""" test[ScalaMountedCB](StateSnapshot.of(_)) """) // lack safe read
          compileError(""" test[JsMounted     ](StateSnapshot.of(_)) """) // use (x.state).setStateVia(x.pure)
          compileError(""" test[ScalaMountedId](StateSnapshot.of(_)) """) // use (x.state).setStateVia(x.pure)
          compileError(""" test[StateAccessI  ](StateSnapshot.of(_)) """) // use (x.state).setStateVia(x.pure)
        }
        "apply_apply" - test[SetFn[S]](StateSnapshot(S)(_)).expect[StateSnapshot[S]]
        "apply_setStateVia" - {
                           test[Render        ](StateSnapshot(S).setStateVia(_)).expect[StateSnapshot[S]]
                           test[Backend       ](StateSnapshot(S).setStateVia(_)).expect[StateSnapshot[S]]
                           test[ScalaMountedCB](StateSnapshot(S).setStateVia(_)).expect[StateSnapshot[S]]
                           test[StateAccessP  ](StateSnapshot(S).setStateVia(_)).expect[StateSnapshot[S]]
          compileError(""" test[JsMounted     ](StateSnapshot(S).setStateVia(_)) """) // use setStateVia(x.pure)
          compileError(""" test[ScalaMountedId](StateSnapshot(S).setStateVia(_)) """) // use setStateVia(x.pure)
          compileError(""" test[StateAccessI  ](StateSnapshot(S).setStateVia(_)) """) // use setStateVia(x.pure)
        }
        "zoom" - {
          def z = StateSnapshot.zoom[S, T](???)(???)
          "of" - test[Render](z.of(_)).expect[StateSnapshot[T]]
          "apply_apply" - test[ModFn[S]](z(S)(_)).expect[StateSnapshot[T]]
          "apply_setStateVia" - {
            test[Render ](z(S).setStateVia(_)).expect[StateSnapshot[T]]
            test[Backend](z(S).setStateVia(_)).expect[StateSnapshot[T]]
          }
        }
      }
    }

    "withReuse" - {
      val log = Reusable.byRef[SetFn[Int]]((os, cb) => cb <<? os.map(Callback.log(_)))
      val warn = Reusable.byRef[SetFn[Int]]((os, cb) => cb <<? os.map(Callback.warn(_)))
      def make = StateSnapshot.withReuse(1)(log)
      "equal" - assertReusable(make, make)
      "diffGet" - assertNotReusable(make, StateSnapshot.withReuse(2)(log))
      "diffSet" - assertNotReusable(make, StateSnapshot.withReuse(1)(warn))

      "inference" - {
        import japgolly.scalajs.react.test.InferenceUtil._
        def SS = StateSnapshot.withReuse
        implicit def rs: Reusability[S] = ???
        "apply_apply" - test[Reusable[SetFn[S]]](SS(S)(_)).expect[StateSnapshot[S]]
        "zoom" - {
          def rs = ??? // shadow
          implicit def rt: Reusability[T] = ???
          def z = SS.zoom[S, T](???)(???)
          "prepareVia" - {
            def p = z.prepareVia(null.asInstanceOf[Render])
            "apply" - test[S](p(_)).expect[StateSnapshot[T]]
          }
        }
      }
    }
  }
}
