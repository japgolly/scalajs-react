package downstream

import utest._

object MimaTests extends TestSuite {

  override def tests = Tests {

    "2_0_0" - {
      import mima200._

      "HookUseRef" - HookUseRef.test()
    }
  }
}
