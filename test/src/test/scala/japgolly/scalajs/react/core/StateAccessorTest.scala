package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import utest.{test => _, _}

object StateAccessorTest extends TestSuite {
  import test.InferenceUtil._

  type J = JS

  override def tests = Tests {

    "writePure" - {
      def use[I, S](i: I)(implicit t: StateAccessor.WritePure[I, S]): S => Callback = t(i).setState(_)
                       assertType[Render        ](use(_)).is[S => Callback]
                       assertType[Backend       ](use(_)).is[S => Callback]
                       assertType[ScalaMountedCB](use(_)).is[S => Callback]
                       assertType[StateAccessP  ](use(_)).is[S => Callback]
      compileError(""" assertType[JsMounted     ](use(_)) """)
      compileError(""" assertType[ScalaMountedId](use(_)) """)
      compileError(""" assertType[StateAccessI  ](use(_)) """)
    }

    "readIdWritePure" - {
      def use[I, S](i: I)(implicit t: StateAccessor.ReadImpureWritePure[I, S]): CallbackTo[S] = t(i).setState(t.state(i)).ret(t state i)
                       assertType[Render        ](use(_)).is[CallbackTo[S]]
      compileError(""" assertType[StateAccessP  ](use(_)) """)
      compileError(""" assertType[StateAccessI  ](use(_)) """)
      compileError(""" assertType[Backend       ](use(_)) """)
      compileError(""" assertType[ScalaMountedCB](use(_)) """)
      compileError(""" assertType[JsMounted     ](use(_)) """)
      compileError(""" assertType[ScalaMountedId](use(_)) """)
    }

    "readCBWritePure" - {
      def use[I, S](i: I)(implicit sa: StateAccessor.ReadWritePure[I, S]): CallbackTo[S] = sa.state(i)
                       assertType[Backend       ](use(_)).is[CallbackTo[S]]
                       assertType[ScalaMountedCB](use(_)).is[CallbackTo[S]]
                       assertType[Render        ](use(_)).is[CallbackTo[S]] // coercion
                       assertType[StateAccessP  ](use(_)).is[CallbackTo[S]] // coercion
      compileError(""" assertType[JsMounted     ](use(_)) """)
      compileError(""" assertType[ScalaMountedId](use(_)) """)
      compileError(""" assertType[StateAccessI  ](use(_)) """)
    }

    "readIdWriteImpure" - {
      def use[I, S](i: I)(implicit sa: StateAccessor.ReadWriteImpure[I, S]): S = sa.state(i)
      compileError(""" assertType[Backend       ](use(_)) """)
      compileError(""" assertType[ScalaMountedCB](use(_)) """)
      compileError(""" assertType[Render        ](use(_)) """)
      compileError(""" assertType[StateAccessP  ](use(_)) """)
                       assertType[JsMounted     ](use(_)).is[J]
                       assertType[ScalaMountedId](use(_)).is[S]
                       assertType[StateAccessI  ](use(_)).is[S]
    }
  }
}
