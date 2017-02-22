package japgolly.scalajs.react.core

import utest._
import japgolly.scalajs.react._

object StateAccessorTest extends TestSuite {
  import test.InferenceUtil._

  type J = JS

  override def tests = TestSuite {

    'writeCB {
      def use[I, S](i: I)(implicit t: StateAccessor.WriteCB[I, S]): S => Callback = t.setState(i)(_)
                       test[Render        ](use(_)).expect[S => Callback]
                       test[Backend       ](use(_)).expect[S => Callback]
                       test[ScalaMountedCB](use(_)).expect[S => Callback]
                       test[StateAccessP  ](use(_)).expect[S => Callback]
      compileError(""" test[JsMounted     ](use(_)) """)
      compileError(""" test[ScalaMountedId](use(_)) """)
      compileError(""" test[StateAccessI  ](use(_)) """)
    }

    'readIdWriteCB {
      def use[I, S](i: I)(implicit t: StateAccessor.ReadIdWriteCB[I, S]): CallbackTo[S] = t.setState(i)(t.state(i)).ret(t state i)
                       test[Render        ](use(_)).expect[CallbackTo[S]]
      compileError(""" test[StateAccessP  ](use(_)) """)
      compileError(""" test[StateAccessI  ](use(_)) """)
      compileError(""" test[Backend       ](use(_)) """)
      compileError(""" test[ScalaMountedCB](use(_)) """)
      compileError(""" test[JsMounted     ](use(_)) """)
      compileError(""" test[ScalaMountedId](use(_)) """)
    }

    'readCBWriteCB {
      def use[I, S](i: I)(implicit sa: StateAccessor.ReadCBWriteCB[I, S]): CallbackTo[S] = sa.state(i)
                       test[Backend       ](use(_)).expect[CallbackTo[S]]
                       test[ScalaMountedCB](use(_)).expect[CallbackTo[S]]
                       test[Render        ](use(_)).expect[CallbackTo[S]] // coercion
                       test[StateAccessP  ](use(_)).expect[CallbackTo[S]] // coercion
      compileError(""" test[JsMounted     ](use(_)) """)
      compileError(""" test[ScalaMountedId](use(_)) """)
      compileError(""" test[StateAccessI  ](use(_)) """)
    }

    'readIdWriteId {
      def use[I, S](i: I)(implicit sa: StateAccessor.ReadIdWriteId[I, S]): S = sa.state(i)
      compileError(""" test[Backend       ](use(_)) """)
      compileError(""" test[ScalaMountedCB](use(_)) """)
      compileError(""" test[Render        ](use(_)) """)
      compileError(""" test[StateAccessP  ](use(_)) """)
                       test[JsMounted     ](use(_)).expect[J]
                       test[ScalaMountedId](use(_)).expect[S]
                       test[StateAccessI  ](use(_)).expect[S]
    }
  }
}
