package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.StateSnapshot.{ModFn, SetFn}
import japgolly.scalajs.react.test.TestUtil._
import scala.annotation.nowarn
import utest._

object StateSnapshotTest extends TestSuite {

  def assertReusable[A](a: A, b: A)(implicit r: Reusability[A]): Unit = assert(a ~=~ b)
  def assertNotReusable[A](a: A, b: A)(implicit r: Reusability[A]): Unit = assert(a ~/~ b)

  override def tests = Tests {

    "noReuse" - {
      def make = StateSnapshot(1)((os, cb) => cb <<? os.map(Callback.log(_)))
      "same" - {val a = make; assertReusable(a, a)}
      "diff" - assertNotReusable(make, make)

      "inference" - {
        import japgolly.scalajs.react.test.InferenceHelpers._
        "of" - {
                           assertType[Render        ].map(StateSnapshot.of(_)).is[StateSnapshot[S]]
          compileError(""" assertType[StateAccessP  ].map(StateSnapshot.of(_)) """) // lack safe read
          compileError(""" assertType[Backend       ].map(StateSnapshot.of(_)) """) // lack safe read
          compileError(""" assertType[ScalaMountedCB].map(StateSnapshot.of(_)) """) // lack safe read
          compileError(""" assertType[JsMounted     ].map(StateSnapshot.of(_)) """) // use (x.state).setStateVia(x.pure)
          compileError(""" assertType[ScalaMountedId].map(StateSnapshot.of(_)) """) // use (x.state).setStateVia(x.pure)
          compileError(""" assertType[StateAccessI  ].map(StateSnapshot.of(_)) """) // use (x.state).setStateVia(x.pure)
        }
        "apply_apply" - assertType[SetFn[S]].map(StateSnapshot(S)(_)).is[StateSnapshot[S]]
        "apply_setStateVia" - {
                           assertType[Render        ].map(StateSnapshot(S).setStateVia(_)).is[StateSnapshot[S]]
                           assertType[Backend       ].map(StateSnapshot(S).setStateVia(_)).is[StateSnapshot[S]]
                           assertType[ScalaMountedCB].map(StateSnapshot(S).setStateVia(_)).is[StateSnapshot[S]]
                           assertType[StateAccessP  ].map(StateSnapshot(S).setStateVia(_)).is[StateSnapshot[S]]
          compileError(""" assertType[JsMounted     ].map(StateSnapshot(S).setStateVia(_)) """) // use setStateVia(x.pure)
          compileError(""" assertType[ScalaMountedId].map(StateSnapshot(S).setStateVia(_)) """) // use setStateVia(x.pure)
          compileError(""" assertType[StateAccessI  ].map(StateSnapshot(S).setStateVia(_)) """) // use setStateVia(x.pure)
        }
        "zoom" - {
          def z = StateSnapshot.zoom[S, T](???)(???)
          "of" - assertType[Render].map(z.of(_)).is[StateSnapshot[T]]
          "apply_apply" - assertType[ModFn[S]].map(z(S)(_)).is[StateSnapshot[T]]
          "apply_setStateVia" - {
            assertType[Render ].map(z(S).setStateVia(_)).is[StateSnapshot[T]]
            assertType[Backend].map(z(S).setStateVia(_)).is[StateSnapshot[T]]
          }
        }
      }
    }

    "withReuse" - {
      "SS_SetFn" - {
        val log = Reusable.byRef[SetFn[Int]]((os, cb) => cb <<? os.map(Callback.log(_)))
        val warn = Reusable.byRef[SetFn[Int]]((os, cb) => cb <<? os.map(Callback.warn(_)))
        def make = StateSnapshot.withReuse(1)(log)
        "equal" - assertReusable(make, make)
        "diffGet" - assertNotReusable(make, StateSnapshot.withReuse(2)(log))
        "diffSet" - assertNotReusable(make, StateSnapshot.withReuse(1)(warn))
      }

      "SetStateFn" - {
        val log = Reusable.byRef[SetStateFnPure[Int]](SetStateFn((os, cb) => cb <<? os.map(Callback.log(_))))
        val warn = Reusable.byRef[SetStateFnPure[Int]](SetStateFn((os, cb) => cb <<? os.map(Callback.warn(_))))
        def make = StateSnapshot.withReuse(1)(log)
        "equal" - assertReusable(make, make)
        "diffGet" - assertNotReusable(make, StateSnapshot.withReuse(2)(log))
        "diffSet" - assertNotReusable(make, StateSnapshot.withReuse(1)(warn))
      }

      "ModStateFn" - {
        val _ = (f: ModStateFnPure[Int]) => f: StateSnapshot.ModFn[Int]
        ()
      }

      "inference" - {
        import japgolly.scalajs.react.test.InferenceHelpers._
        def SS = StateSnapshot.withReuse
        implicit def rs: Reusability[S] = ???
        "apply_apply" - assertType[Reusable[SetFn[S]]].map(SS(S)(_)).is[StateSnapshot[S]]
        "zoom" - {
          @nowarn("cat=unused") def rs = ??? // shadow
          implicit def rt: Reusability[T] = ???
          def z = SS.zoom[S, T](???)(???)
          "prepareVia" - {
            def p = z.prepareVia(null.asInstanceOf[Render])
            "apply" - assertType[S].map(p(_)).is[StateSnapshot[T]]
          }
        }
      }
    }
  }

}
