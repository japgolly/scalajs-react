package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import utest.{test => _, _}

object StateAccessTest extends TestSuite {

  override def tests = Tests {
    import test.InferenceUtil._

    "generic" - {
      "xmap" - test[StateAccessPure[S]](_.xmapState[T](???)(???)).expect_<[StateAccessPure[T]]
    }
    "backendScope" - {
      "is"   - test[ScalaComponent.BackendScope[P, S]].usableAs[StateAccessPure[S]]
      "xmap" - test[ScalaComponent.BackendScope[P, S]](_.xmapState[T](???)(???)).expect_<[StateAccessPure[T]]
    }
  }
}
