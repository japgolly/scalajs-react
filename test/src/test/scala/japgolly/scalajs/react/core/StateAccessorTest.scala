package japgolly.scalajs.react.core

import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react._
import utest.{test => _, _}

object StateAccessorTest extends TestSuite {
  import test.InferenceHelpers._

  type J = JS

  override def tests = Tests {

    "writePure" - {
      def use[I, S](i: I)(implicit t: StateAccessor.WritePure[I, S]): S => Callback = t(i).setState(_)
                       assertType[Render        ].map(use(_)).is[S => Callback]
                       assertType[Backend       ].map(use(_)).is[S => Callback]
                       assertType[ScalaMountedCB].map(use(_)).is[S => Callback]
                       assertType[StateAccessP  ].map(use(_)).is[S => Callback]
      compileError(""" assertType[JsMounted     ].map(use(_)) """)
      compileError(""" assertType[ScalaMountedId].map(use(_)) """)
      compileError(""" assertType[StateAccessI  ].map(use(_)) """)
    }

    "readIdWritePure" - {
      def use[I, S](i: I)(implicit t: StateAccessor.ReadImpureWritePure[I, S]): CallbackTo[S] = t(i).setState(t.state(i)).ret(t state i)
                       assertType[Render        ].map(use(_)).is[CallbackTo[S]]
      compileError(""" assertType[StateAccessP  ].map(use(_)) """)
      compileError(""" assertType[StateAccessI  ].map(use(_)) """)
      compileError(""" assertType[Backend       ].map(use(_)) """)
      compileError(""" assertType[ScalaMountedCB].map(use(_)) """)
      compileError(""" assertType[JsMounted     ].map(use(_)) """)
      compileError(""" assertType[ScalaMountedId].map(use(_)) """)
    }

    "readCBWritePure" - {
      def use[I, S](i: I)(implicit sa: StateAccessor.ReadWritePure[I, S]): CallbackTo[S] = sa.state(i)
                       assertType[Backend       ].map(use(_)).is[CallbackTo[S]]
                       assertType[ScalaMountedCB].map(use(_)).is[CallbackTo[S]]
                       assertType[Render        ].map(use(_)).is[CallbackTo[S]] // coercion
                       assertType[StateAccessP  ].map(use(_)).is[CallbackTo[S]] // coercion
      compileError(""" assertType[JsMounted     ].map(use(_)) """)
      compileError(""" assertType[ScalaMountedId].map(use(_)) """)
      compileError(""" assertType[StateAccessI  ].map(use(_)) """)
    }

    "readIdWriteImpure" - {
      def use[I, S](i: I)(implicit sa: StateAccessor.ReadWriteImpure[I, S]): S = sa.state(i)
      compileError(""" assertType[Backend       ].map(use(_)) """)
      compileError(""" assertType[ScalaMountedCB].map(use(_)) """)
      compileError(""" assertType[Render        ].map(use(_)) """)
      compileError(""" assertType[StateAccessP  ].map(use(_)) """)
                       assertType[JsMounted     ].map(use(_)).is[J]
                       assertType[ScalaMountedId].map(use(_)).is[S]
                       assertType[StateAccessI  ].map(use(_)).is[S]
    }
  }
}
