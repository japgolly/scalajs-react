package japgolly.scalajs.react

import utest._

object StateAccessTest extends TestSuite {

  override def tests = TestSuite {
    import test.InferenceUtil._

    'generic {
      'xmap - test[StateAccessPure[S]](_.xmapState[T](???)(???)).expect_<[StateAccessPure[T]]
    }
    'backendScope {
      'is   - test[ScalaComponent.BackendScope[P, S]].usableAs[StateAccessPure[S]]
      'xmap - test[ScalaComponent.BackendScope[P, S]](_.xmapState[T](???)(???)).expect_<[StateAccessPure[T]]
    }
  }
}
