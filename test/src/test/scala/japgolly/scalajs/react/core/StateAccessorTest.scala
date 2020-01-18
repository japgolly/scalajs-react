package japgolly.scalajs.react.core

import utest.{test => _, _}
import japgolly.scalajs.react._

object StateAccessorTest extends TestSuite {
  import test.InferenceUtil._

  type J = JS

  override def tests = Tests {

    "writePure" - {
      def use[I, S](i: I)(implicit t: StateAccessor.WritePure[I, S]): S => Callback = t(i).setState(_)
                       test[Render        ](use(_)).expect[S => Callback]
                       test[Backend       ](use(_)).expect[S => Callback]
                       test[ScalaMountedCB](use(_)).expect[S => Callback]
                       test[StateAccessP  ](use(_)).expect[S => Callback]
      compileError(""" test[JsMounted     ](use(_)) """)
      compileError(""" test[ScalaMountedId](use(_)) """)
      compileError(""" test[StateAccessI  ](use(_)) """)
    }

    "readIdWritePure" - {
      def use[I, S](i: I)(implicit t: StateAccessor.ReadImpureWritePure[I, S]): CallbackTo[S] = t(i).setState(t.state(i)).ret(t state i)
                       test[Render        ](use(_)).expect[CallbackTo[S]]
      compileError(""" test[StateAccessP  ](use(_)) """)
      compileError(""" test[StateAccessI  ](use(_)) """)
      compileError(""" test[Backend       ](use(_)) """)
      compileError(""" test[ScalaMountedCB](use(_)) """)
      compileError(""" test[JsMounted     ](use(_)) """)
      compileError(""" test[ScalaMountedId](use(_)) """)
    }

    "readCBWritePure" - {
      def use[I, S](i: I)(implicit sa: StateAccessor.ReadWritePure[I, S]): CallbackTo[S] = sa.state(i)
                       test[Backend       ](use(_)).expect[CallbackTo[S]]
                       test[ScalaMountedCB](use(_)).expect[CallbackTo[S]]
                       test[Render        ](use(_)).expect[CallbackTo[S]] // coercion
                       test[StateAccessP  ](use(_)).expect[CallbackTo[S]] // coercion
      compileError(""" test[JsMounted     ](use(_)) """)
      compileError(""" test[ScalaMountedId](use(_)) """)
      compileError(""" test[StateAccessI  ](use(_)) """)
    }

    "readIdWriteImpure" - {
      def use[I, S](i: I)(implicit sa: StateAccessor.ReadWriteImpure[I, S]): S = sa.state(i)
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
