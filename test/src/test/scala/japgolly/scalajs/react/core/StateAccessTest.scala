package japgolly.scalajs.react.core

import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react._
import utest.{test => _, _}

object StateAccessTest extends TestSuite {

  override def tests = Tests {
    import test.InferenceHelpers._

    "generic" - {
      "xmap" - assertType[StateAccessPure[S]](_.xmapState[T](???)(???)).is_<[StateAccessPure[T]]
    }
    "backendScope" - {
      "is"   - assertType[ScalaComponent.BackendScope[P, S]].isImplicitly[StateAccessPure[S]]
      "xmap" - assertType[ScalaComponent.BackendScope[P, S]](_.xmapState[T](???)(???)).is_<[StateAccessPure[T]]
    }
  }
}
