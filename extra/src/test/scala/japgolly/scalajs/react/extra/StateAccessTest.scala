package japgolly.scalajs.react.extra

import utest._
import japgolly.scalajs.react.{test => _, _}
import japgolly.scalajs.react.test.InferenceUtil._

object StateAccessTest extends TestSuite {

  type Render = ScalaComponent.Lifecycle.RenderScope[P, S, B]
  type Backend = BackendScope[P, S]
  type JsMounted = JsComponent.Mounted[JP, JS]
  type ScalaMountedId = ScalaComponent.Mounted[P, S, B]
  type ScalaMountedCB = ScalaComponent.MountedCB[P, S, B]

  type J = JS

  override def tests = TestSuite {

    'writeCB {
      test[Render        ]($ => ReusableFn.state($).set).expect[S ~=> Callback]
      test[Backend       ]($ => ReusableFn.state($).set).expect[S ~=> Callback]
      test[JsMounted     ]($ => ReusableFn.state($).set).expect[J ~=> Callback]
      test[ScalaMountedId]($ => ReusableFn.state($).set).expect[S ~=> Callback]
      test[ScalaMountedCB]($ => ReusableFn.state($).set).expect[S ~=> Callback]
    }

    'readIdWriteCB {
      compileError(""" test[Backend       ](StateSnapshot.of(_)) """)
      compileError(""" test[ScalaMountedCB](StateSnapshot.of(_)) """)
                       test[Render        ](StateSnapshot.of(_)).expect[StateSnapshot[S]]
                       test[JsMounted     ](StateSnapshot.of(_)).expect[StateSnapshot[J]]
                       test[ScalaMountedId](StateSnapshot.of(_)).expect[StateSnapshot[S]]
    }

    'readCBWriteCB {
      def use[I, S](i: I)(implicit sa: StateAccess.ReadCBWriteCB[I, S]): CallbackTo[S] = sa.state(i)
      compileError(""" test[Render        ](use(_)) """)
                       test[Backend       ](use(_)).expect[CallbackTo[S]]
                       test[JsMounted     ](use(_)).expect[CallbackTo[J]]
                       test[ScalaMountedId](use(_)).expect[CallbackTo[S]]
                       test[ScalaMountedCB](use(_)).expect[CallbackTo[S]]
    }
  }
}
