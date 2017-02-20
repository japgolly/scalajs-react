package japgolly.scalajs.react

import utest._

object StateAccessorTest extends TestSuite {
  import test.InferenceUtil._

  type Render = ScalaComponent.Lifecycle.RenderScope[P, S, B]
  type Backend = BackendScope[P, S]
  type JsMounted = JsComponent.Mounted[JP, JS]
  type ScalaMountedId = ScalaComponent.Mounted[P, S, B]
  type ScalaMountedCB = ScalaComponent.MountedCB[P, S, B]

  type J = JS

  override def tests = TestSuite {

    'writeCB {
      def use[I, S](i: I)(implicit t: StateAccessor.WriteCB[I, S]): S => Callback = t.setState(i)(_)
      test[Render        ](use(_)).expect[S => Callback]
      test[Backend       ](use(_)).expect[S => Callback]
      test[JsMounted     ](use(_)).expect[J => Callback]
      test[ScalaMountedId](use(_)).expect[S => Callback]
      test[ScalaMountedCB](use(_)).expect[S => Callback]
    }

    'readIdWriteCB {
      def use[I, S](i: I)(implicit t: StateAccessor.ReadIdWriteCB[I, S]): CallbackTo[S] = t.setState(i)(t.state(i)).ret(t state i)
      compileError(""" test[Backend       ](use(_)) """)
      compileError(""" test[ScalaMountedCB](use(_)) """)
                       test[Render        ](use(_)).expect[CallbackTo[S]]
                       test[JsMounted     ](use(_)).expect[CallbackTo[J]]
                       test[ScalaMountedId](use(_)).expect[CallbackTo[S]]
    }

    'readCBWriteCB {
      def use[I, S](i: I)(implicit sa: StateAccessor.ReadCBWriteCB[I, S]): CallbackTo[S] = sa.state(i)
      compileError(""" test[Render        ](use(_)) """)
                       test[Backend       ](use(_)).expect[CallbackTo[S]]
                       test[JsMounted     ](use(_)).expect[CallbackTo[J]]
                       test[ScalaMountedId](use(_)).expect[CallbackTo[S]]
                       test[ScalaMountedCB](use(_)).expect[CallbackTo[S]]
    }
  }
}
